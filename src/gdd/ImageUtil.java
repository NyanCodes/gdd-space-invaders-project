package gdd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Shared image loading helpers.
 *
 * The pickup and bullet art is exported at a few hundred pixels square with a
 * wide transparent margin around the drawing. Sprite collision uses the image
 * bounds, so scaling those files down as-is would give a pickup a hitbox much
 * larger than what the player sees. Everything that loads padded art goes
 * through {@link #fit} so the drawn sprite and its hitbox are the same thing.
 */
public final class ImageUtil {

    // Alpha at or below this counts as empty when trimming the margin.
    private static final int ALPHA_CUTOFF = 8;

    private ImageUtil() {
        // Prevent instantiation
    }

    /**
     * Loads an image, trims its fully transparent border, and scales what is
     * left down to fit inside maxW x maxH without distorting its aspect ratio.
     * Art that already fits is returned untouched rather than blown up.
     */
    public static Image fit(String path, int maxW, int maxH) {
        BufferedImage trimmed = trim(toBuffered(new ImageIcon(path).getImage()));
        if (trimmed.getWidth() <= maxW && trimmed.getHeight() <= maxH) {
            return trimmed; // already small enough — don't blur it by upscaling
        }

        double scale = Math.min((double) maxW / trimmed.getWidth(),
                (double) maxH / trimmed.getHeight());
        int w = Math.max(1, (int) Math.round(trimmed.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(trimmed.getHeight() * scale));

        return trimmed.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    /**
     * The animation-frame version of {@link #fit}.
     *
     * Trimming each frame on its own would crop every one to its own bounding
     * box, so a projectile whose trail grows and shrinks would jump around and
     * change size as it cycled. Here the frames are trimmed to a single shared
     * box — the union of them all — and scaled by one factor, so the animation
     * plays in place.
     */
    public static Image[] fitAll(String[] paths, int maxW, int maxH) {
        BufferedImage[] raw = new BufferedImage[paths.length];
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;

        for (int i = 0; i < paths.length; i++) {
            raw[i] = toBuffered(new ImageIcon(paths[i]).getImage());
            int[] box = bounds(raw[i]);
            if (box == null) {
                continue; // fully transparent frame, nothing to contribute
            }
            minX = Math.min(minX, box[0]);
            minY = Math.min(minY, box[1]);
            maxX = Math.max(maxX, box[2]);
            maxY = Math.max(maxY, box[3]);
        }
        if (maxX < 0) {
            minX = minY = 0;
            maxX = raw[0].getWidth() - 1;
            maxY = raw[0].getHeight() - 1;
        }

        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        double scale = Math.min(1.0,
                Math.min((double) maxW / w, (double) maxH / h));
        int outW = Math.max(1, (int) Math.round(w * scale));
        int outH = Math.max(1, (int) Math.round(h * scale));

        Image[] out = new Image[paths.length];
        for (int i = 0; i < paths.length; i++) {
            // Clamp the shared box to this frame, in case the files differ in size.
            int cw = Math.min(w, raw[i].getWidth() - minX);
            int ch = Math.min(h, raw[i].getHeight() - minY);
            BufferedImage cropped = raw[i].getSubimage(minX, minY,
                    Math.max(1, cw), Math.max(1, ch));
            out[i] = scale < 1.0
                    ? cropped.getScaledInstance(outW, outH, Image.SCALE_SMOOTH)
                    : cropped;
        }
        return out;
    }

    /**
     * Mirrors an image left to right. The plane sprites are all baked facing
     * left, so the ones flying the other way are flipped here rather than
     * shipped as a second copy of the same art.
     */
    public static Image mirror(Image src) {
        BufferedImage in = toBuffered(src);
        int w = in.getWidth();
        int h = in.getHeight();

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        // Source right edge maps to the destination left edge.
        g2.drawImage(in, 0, 0, w, h, w, 0, 0, h, null);
        g2.dispose();
        return out;
    }

    /**
     * Repaints every pixel of an image in one flat colour, keeping its alpha.
     * Enemy fire reuses the player's bullet art this way instead of needing a
     * second asset just to be red.
     */
    public static Image tint(Image src, Color color) {
        BufferedImage in = toBuffered(src);
        int w = in.getWidth();
        int h = in.getHeight();
        int rgb = color.getRGB() & 0x00FFFFFF;

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = in.getRGB(x, y) >>> 24;
                out.setRGB(x, y, (alpha << 24) | rgb);
            }
        }
        return out;
    }

    // ImageIcon loads synchronously, so the dimensions are known by now.
    // Wrapping in an ImageIcon first also forces a getScaledInstance() result
    // to finish loading, which it does asynchronously otherwise.
    private static BufferedImage toBuffered(Image src) {
        if (src instanceof BufferedImage) {
            return (BufferedImage) src;
        }
        Image loaded = new ImageIcon(src).getImage();
        int w = Math.max(1, loaded.getWidth(null));
        int h = Math.max(1, loaded.getHeight(null));

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(loaded, 0, 0, null);
        g2.dispose();
        return out;
    }

    // Crop away the transparent border. A fully transparent image is returned
    // untouched rather than cropped to nothing.
    private static BufferedImage trim(BufferedImage src) {
        int[] box = bounds(src);
        if (box == null) {
            return src;
        }
        return src.getSubimage(box[0], box[1],
                box[2] - box[0] + 1, box[3] - box[1] + 1);
    }

    // {minX, minY, maxX, maxY} of the non-transparent pixels, or null if the
    // image is empty.
    private static int[] bounds(BufferedImage src) {
        int minX = src.getWidth();
        int minY = src.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                if ((src.getRGB(x, y) >>> 24) > ALPHA_CUTOFF) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        return maxX < 0 ? null : new int[]{minX, minY, maxX, maxY};
    }
}
