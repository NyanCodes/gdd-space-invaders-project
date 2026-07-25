package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Stage 2 hazard: tumbling space debris drifting in from the right at an
 * angle. It isn't an "enemy" you simply out-run - it's an obstacle field
 * you either dodge or shoot through. Shooting it chips away its hit
 * points; at zero it breaks into two smaller pieces (large -> medium ->
 * small -> gone), so clearing a big rock takes sustained fire. Ramming
 * one costs the player a life the same as an alien collision, but the
 * rock itself survives the hit.
 */
public class Obstacle extends Sprite {

    public enum Size {
        LARGE(64, 3, 3),
        MEDIUM(40, 2, 2),
        SMALL(24, 1, 1);

        final int px;
        final int hp;
        final int score;

        Size(int px, int hp, int score) {
            this.px = px;
            this.hp = hp;
            this.score = score;
        }

        public int getScore() {
            return score;
        }
    }

    private static final int FRAME_COUNT = 24;
    private static final Map<Size, Image[]> FRAME_CACHE = new HashMap<>();
    private static final Random RNG = new Random();

    private final Size size;
    private int hp;
    private double xPrecise;
    private double yPrecise;
    private final double vx;
    private final double vy;
    private final double spinRate;
    private double rotAccum;

    public Obstacle(Size size, int x, int y) {
        this.size = size;
        this.hp = size.hp;
        this.xPrecise = x;
        this.yPrecise = y;
        this.x = x;
        this.y = y;

        // Small pieces tumble faster/more erratically than large ones.
        double speedBoost = size == Size.SMALL ? 1.6 : size == Size.MEDIUM ? 1.2 : 1.0;
        this.vx = -(1.8 + RNG.nextDouble() * 1.6) * speedBoost;
        this.vy = (RNG.nextDouble() - 0.5) * 2.2 * speedBoost;
        this.spinRate = 0.15 + RNG.nextDouble() * 0.35;
        this.rotAccum = RNG.nextDouble() * FRAME_COUNT;

        setImage(frames()[0]);
    }

    private Image[] frames() {
        return FRAME_CACHE.computeIfAbsent(size, Obstacle::buildFrames);
    }

    // Procedurally draws a jagged rock (no art asset needed) and
    // pre-rotates it into a short animation loop so it visibly tumbles
    // without regenerating pixels every frame.
    private static Image[] buildFrames(Size size) {
        int d = size.px;
        BufferedImage base = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = base.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Random r = new Random(size.ordinal() * 97L + 13);
        int cx = d / 2;
        int cy = d / 2;
        int points = 9 + r.nextInt(4);
        int[] xs = new int[points];
        int[] ys = new int[points];
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            double radius = d * (0.32 + r.nextDouble() * 0.18);
            xs[i] = (int) (cx + Math.cos(angle) * radius);
            ys[i] = (int) (cy + Math.sin(angle) * radius);
        }
        g.setColor(new Color(92, 80, 68));
        g.fillPolygon(xs, ys, points);
        g.setColor(new Color(155, 138, 116));
        g.drawPolygon(xs, ys, points);
        g.setColor(new Color(58, 48, 40));
        int craters = 2 + r.nextInt(3);
        for (int i = 0; i < craters; i++) {
            int cw = d / (4 + r.nextInt(3));
            int px = cx - cw / 2 + r.nextInt(d / 3) - d / 6;
            int py = cy - cw / 2 + r.nextInt(d / 3) - d / 6;
            g.fillOval(px, py, cw, cw);
        }
        g.dispose();

        Image[] out = new Image[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            double theta = (Math.PI * 2 * i) / FRAME_COUNT;
            BufferedImage rotated = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rotated.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(base, AffineTransform.getRotateInstance(theta, cx, cy), null);
            g2.dispose();
            out[i] = rotated;
        }
        return out;
    }

    @Override
    public void act() {
        xPrecise += vx;
        yPrecise += vy;
        x = (int) xPrecise;
        y = (int) yPrecise;

        rotAccum += spinRate;
        int idx = ((int) rotAccum) % FRAME_COUNT;
        if (idx < 0) {
            idx += FRAME_COUNT;
        }
        setImage(frames()[idx]);

        if (x < -size.px - 20 || y < -size.px - 40 || y > BOARD_HEIGHT + size.px + 40) {
            die();
        }
    }

    /** Registers a hit; returns true once the rock has run out of hit points. */
    public boolean hit() {
        hp--;
        return hp <= 0;
    }

    public Size getSize() {
        return size;
    }

    /** The pieces this rock breaks into when destroyed (empty once already smallest). */
    public List<Obstacle> split() {
        List<Obstacle> pieces = new ArrayList<>();
        Size next = size == Size.LARGE ? Size.MEDIUM : size == Size.MEDIUM ? Size.SMALL : null;
        if (next != null) {
            pieces.add(new Obstacle(next, x, y - next.px / 2));
            pieces.add(new Obstacle(next, x, y + next.px / 2));
        }
        return pieces;
    }
}
