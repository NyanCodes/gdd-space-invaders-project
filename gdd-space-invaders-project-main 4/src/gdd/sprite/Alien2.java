package gdd.sprite;

import static gdd.Global.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Stage 2 enemy: "Weaver". Advances left while swaying through a sine
 * wave whose amplitude, frequency and phase are randomized per-instance,
 * so unlike Alien1's straight line, each one is harder to predict and lead
 * a shot on.
 */
public class Alien2 extends Enemy {

    private final double baseY;
    private final double amplitude;
    private final double frequency;
    private final double phase;
    private final int speed;
    private int tick = 0;

    public Alien2(int x, int y) {
        super(x, y);
        this.baseY = y;
        this.amplitude = 40 + Math.random() * 70;      // 40-110px sway
        this.frequency = 0.025 + Math.random() * 0.035; // wobble rate
        this.phase = Math.random() * Math.PI * 2;
        this.speed = 3 + (int) (Math.random() * 2);      // faster than Alien1

        setImage(tint(IMG_ENEMY, new Color(150, 90, 255), SCALE_FACTOR));
    }

    @Override
    public void act(int direction) {
        tick++;
        this.x -= speed;
        this.y = (int) (baseY + Math.sin(tick * frequency + phase) * amplitude);

        if (this.x < -ALIEN_WIDTH * SCALE_FACTOR) {
            die();
        }
    }

    // Shared helper: tints the base alien sprite so Stage 2's enemy
    // variants are visually distinct at a glance (color-codes threat type).
    static Image tint(String path, Color color, int scale) {
        var ii = new ImageIcon(path);
        int w = ii.getIconWidth() * scale;
        int h = ii.getIconHeight() * scale;
        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buf.createGraphics();
        g2.drawImage(ii.getImage(), 0, 0, w, h, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.55f));
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return buf;
    }
}
