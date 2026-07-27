package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Stage 2 only: grants a mirrored volley out the ship's tail as well as its
 * nose, answering the plane traffic that can attack from behind on that
 * stage (Stage.planesFromBehind). Permanent once picked up, like the gun
 * ladder — see {@link Player#enableRearFire}.
 *
 * No external art: drawn procedurally the same way gdd.sprite.Obstacle draws
 * its rock, as a pulsing badge with two opposing arrowheads rather than a
 * spun 3-D model.
 */
public class RearGunUp extends PowerUp {

    private static final int FRAME_COUNT = 16;
    private static Image[] frames;

    private int animTick = 0;

    public RearGunUp(int x, int y) {
        super(x, y);
        setImage(frames()[0]);
    }

    private static Image[] frames() {
        if (frames == null) {
            frames = buildFrames();
        }
        return frames;
    }

    /** The badge's first frame, rescaled — for the power-up tip box. */
    public static Image previewIcon(int size) {
        return frames()[0].getScaledInstance(size, size, Image.SCALE_SMOOTH);
    }

    private static Image[] buildFrames() {
        int d = POWERUP_SIZE;
        Image[] out = new Image[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            double t = (double) i / FRAME_COUNT;
            BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = d / 2;
            int cy = d / 2;
            int r = d / 2 - 2;

            // Pulsing magenta glow ring — distinct from every gun-ladder
            // colour and the golden shield, so it reads as its own thing.
            float pulse = (float) (0.5 + 0.5 * Math.sin(t * Math.PI * 2));
            g.setColor(new Color(255, 60, 200, (int) (110 + 80 * pulse)));
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
            g.setColor(new Color(255, 150, 230));
            g.drawOval(cx - r, cy - r, r * 2, r * 2);

            // Two opposing arrowheads pointing outward — "fires both ways".
            g.setColor(Color.white);
            int aw = d / 5;
            int ah = d / 3;
            g.fillPolygon(new int[]{cx + 3, cx + 3 + aw, cx + 3},
                    new int[]{cy - ah / 2, cy, cy + ah / 2}, 3);
            g.fillPolygon(new int[]{cx - 3, cx - 3 - aw, cx - 3},
                    new int[]{cy - ah / 2, cy, cy + ah / 2}, 3);

            g.dispose();
            out[i] = img;
        }
        return out;
    }

    @Override
    public void act() {
        animTick++;
        Image[] f = frames();
        setImage(f[(animTick / 4) % f.length]);
        // Drift left from the right edge so the player can fly into it.
        this.x -= POWERUP_DRIFT_SPEED;
        if (this.x < -60) {
            die(); // Off-screen, clean up
        }
    }

    @Override
    public void upgrade(Player player) {
        player.enableRearFire();
        this.die(); // Remove the power-up after use
    }
}
