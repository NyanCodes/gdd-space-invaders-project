package gdd.sprite;

import static gdd.Global.*;
import gdd.GunTier;
import gdd.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * A bullet from the player's gun.
 *
 * The artwork comes from the gun's {@link GunTier}; the top two rungs are drawn
 * as several frames, so those bullets animate as they fly. Damage and travel
 * speed live on the {@link Player}, not here — Scene1 reads the speed when it
 * calls {@link #advance}, which is also why {@link #act()} only advances the
 * animation.
 *
 * Every rung above the first fires its whole volley at once as a fan, so a
 * bullet carries its own heading rather than simply flying right.
 */
public class Shot extends Sprite {

    // Trimming and scaling the art is per-pixel work, so each rung's frames
    // are built once and shared by every bullet fired from that rung. Angled
    // bullets are drawn turned to face where they fly, and those turned
    // frames are cached the same way, per rung and angle.
    private static final Map<GunTier, Image[]> FRAMES = new EnumMap<>(GunTier.class);
    private static final Map<String, Image[]> TURNED_FRAMES = new HashMap<>();

    private final Image[] frames;
    private final double dirX;
    private final double dirY;
    // Sub-pixel position. An angled bullet moves a fractional number of pixels
    // per axis each frame, so rounding Sprite's int x/y every frame would bend
    // its path; the exact position is kept here and only rounded for drawing.
    private double preciseX;
    private double preciseY;
    private int animTick = 0;

    // (tipX, tipY) is the tip of the ship: the right edge, vertically centered.
    public Shot(int tipX, int tipY) {
        this(tipX, tipY, GunTier.BASE, 0);
    }

    public Shot(int tipX, int tipY, GunTier tier, double angleDegrees) {

        this.frames = framesFor(tier == null ? GunTier.BASE : tier, angleDegrees);
        Image bullet = frames[0];
        setImage(bullet);

        // Screen y grows downward, so a positive angle sends the bullet down.
        // The fan is symmetric, so which way that points does not matter.
        double radians = Math.toRadians(angleDegrees);
        this.dirX = Math.cos(radians);
        this.dirY = Math.sin(radians);

        // Anchor at the ship's tip: left edge at the tip, centered on it vertically.
        setX(tipX);
        setY(tipY - bullet.getHeight(null) / 2);
        this.preciseX = getX();
        this.preciseY = getY();
    }

    /** Moves the bullet one frame along its own heading. */
    public void advance(int speed) {
        preciseX += dirX * speed;
        preciseY += dirY * speed;
        setX((int) Math.round(preciseX));
        setY((int) Math.round(preciseY));
    }

    private static Image[] framesFor(GunTier tier, double angleDegrees) {
        Image[] straight = straightFramesFor(tier);

        int angle = (int) Math.round(angleDegrees);
        if (angle == 0) {
            return straight;
        }

        String key = tier.name() + "@" + angle;
        Image[] cached = TURNED_FRAMES.get(key);
        if (cached != null) {
            return cached;
        }
        Image[] built = new Image[straight.length];
        for (int i = 0; i < straight.length; i++) {
            built[i] = turned(straight[i], angle);
        }
        TURNED_FRAMES.put(key, built);
        return built;
    }

    private static Image[] straightFramesFor(GunTier tier) {
        Image[] cached = FRAMES.get(tier);
        if (cached != null) {
            return cached;
        }

        // fitAll, not fit: the frames share one bounding box, so an animated
        // bullet stays put instead of resizing as its trail grows.
        Image[] built = tier.bulletFrames == null
                ? new Image[]{plainImage()} // starting bullet, no art on the ladder
                : ImageUtil.fitAll(tier.bulletFrames,
                        tier.bulletWidth, tier.bulletHeight);
        FRAMES.put(tier, built);
        return built;
    }

    /**
     * Advances the bullet's animation. Movement is not done here: the shot
     * travels at the player's current bullet speed, so Scene1 drives it.
     */
    @Override
    public void act() {
        if (frames == null || frames.length < 2) {
            return;
        }
        animTick++;
        setImage(frames[(animTick / BULLET_ANIM_FRAMES) % frames.length]);
    }

    // Turns a bullet frame to face its heading. The canvas grows to the
    // rotated bounding box so no corner of the art is clipped.
    private static Image turned(Image src, int angleDegrees) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));
        int tw = (int) Math.round(w * cos + h * sin);
        int th = (int) Math.round(w * sin + h * cos);

        BufferedImage out = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.rotate(radians, tw / 2.0, th / 2.0);
        g2.drawImage(src, (tw - w) / 2, (th - h) / 2, null);
        g2.dispose();
        return out;
    }

    // The default bullet is a tiny vertical sprite, so it is scaled up and
    // then rotated 90 degrees clockwise to point to the right.
    private static Image plainImage() {

        var ii = new ImageIcon(IMG_SHOT);
        int sw = ii.getIconWidth() * SCALE_FACTOR;
        int sh = ii.getIconHeight() * SCALE_FACTOR;

        // Scale first (force a fully-loaded image so we can draw its pixels).
        Image scaled = new ImageIcon(
                ii.getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH)).getImage();

        // A 90-degree rotation swaps width and height.
        BufferedImage rotated = new BufferedImage(sh, sw, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.translate(sh, 0);
        g2.rotate(Math.toRadians(90));
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return rotated;
    }
}
