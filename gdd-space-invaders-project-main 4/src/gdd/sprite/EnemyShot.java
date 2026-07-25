package gdd.sprite;

import static gdd.Global.*;
import gdd.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * A bullet fired by an {@link EnemyPlane}, aimed at wherever the ship was
 * standing when the trigger was pulled. It keeps that heading for its whole
 * life — it does not track the player afterwards.
 *
 * The player's {@link Shot} always travels straight right and is moved by
 * Scene1, so it needs no velocity of its own. This one flies at an angle, so
 * it carries its own (vx, vy) and keeps position as a double, rounding only
 * for drawing and collision.
 */
public class EnemyShot extends Sprite {

    // Built once: the player's bullet art, scaled up, turned sideways and
    // repainted hostile red.
    private static Image sprite;

    private double px;
    private double py;
    private final double vx;
    private final double vy;

    /**
     * (muzzleX, muzzleY) is the plane's nose, (targetX, targetY) the middle of
     * the player right now.
     */
    public EnemyShot(int muzzleX, int muzzleY, int targetX, int targetY, int fallbackStep) {

        Image bullet = sprite();
        setImage(bullet);

        double aimX = targetX - muzzleX;
        double aimY = targetY - muzzleY;
        double length = Math.hypot(aimX, aimY);
        if (length < 1e-6) {
            // Firing from inside the player: just carry on straight ahead.
            aimX = fallbackStep;
            aimY = 0;
            length = 1;
        }
        this.vx = aimX / length * PLANE_SHOT_SPEED;
        this.vy = aimY / length * PLANE_SHOT_SPEED;

        // Centre the bullet on the muzzle.
        this.px = muzzleX - bullet.getWidth(null) / 2.0;
        this.py = muzzleY - bullet.getHeight(null) / 2.0;
        setX((int) Math.round(px));
        setY((int) Math.round(py));
    }

    @Override
    public void act() {
        px += vx;
        py += vy;
        setX((int) Math.round(px));
        setY((int) Math.round(py));
    }

    /** Left the playfield entirely — Scene1 drops it. */
    public boolean isOffscreen() {
        int w = getImage().getWidth(null);
        int h = getImage().getHeight(null);
        return x + w < 0 || x > BOARD_WIDTH || y + h < 0 || y > BOARD_HEIGHT;
    }

    // The bullet art is a tiny vertical sprite, so it is scaled up and rotated
    // 90 degrees to point sideways, the same way Shot builds the player's
    // bullet. Enemy fire is then tinted so it never reads as the player's.
    private static Image sprite() {

        if (sprite == null) {
            var ii = new ImageIcon(IMG_SHOT);
            int sw = ii.getIconWidth() * PLANE_SHOT_SCALE;
            int sh = ii.getIconHeight() * PLANE_SHOT_SCALE;

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

            sprite = ImageUtil.tint(rotated, PLANE_SHOT_COLOR);
        }
        return sprite;
    }
}
