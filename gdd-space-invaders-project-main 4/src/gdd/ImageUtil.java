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

        if (maxX < 0) {
            return src;
        }
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
