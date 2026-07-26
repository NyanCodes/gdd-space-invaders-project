package gdd.sprite;

import static gdd.Global.*;
import gdd.GunTier;
import gdd.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * A bullet from the player's gun.
 *
 * The artwork comes from the gun's {@link GunTier}; the top two rungs are drawn
 * as several frames, so those bullets animate as they fly. Damage and travel
 * speed live on the {@link Player}, not here — Scene1 reads them when it moves
 * the shot, which is also why {@link #act()} only advances the animation.
 */
public class Shot extends Sprite {

    // Trimming and scaling the art is per-pixel work, so each rung's frames
    // are built once and shared by every bullet fired from that rung.
    private static final Map<GunTier, Image[]> FRAMES = new EnumMap<>(GunTier.class);

    private final Image[] frames;
    private int animTick = 0;

    public Shot() {
        this.frames = null;
    }

    // (tipX, tipY) is the tip of the ship: the right edge, vertically centered.
    public Shot(int tipX, int tipY) {
        this(tipX, tipY, GunTier.BASE);
    }

    public Shot(int tipX, int tipY, GunTier tier) {

        this.frames = framesFor(tier == null ? GunTier.BASE : tier);
        Image bullet = frames[0];
        setImage(bullet);

        // Anchor at the ship's tip: left edge at the tip, centered on it vertically.
        setX(tipX);
        setY(tipY - bullet.getHeight(null) / 2);
    }

    private static Image[] framesFor(GunTier tier) {
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
