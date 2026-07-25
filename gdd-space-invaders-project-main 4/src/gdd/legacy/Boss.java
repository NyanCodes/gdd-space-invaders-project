package gdd.legacy;

import static gdd.Global.*;
import gdd.sprite.Enemy;
import javax.swing.ImageIcon;

/**
 * Legacy Boss used only by the old (Stage2-zip) Scene2 code path. Kept
 * separate from gdd.sprite.Boss because that class now backs the current
 * Scene1-driven stages and no longer supports a custom HP override or a
 * no-arg hit(); this preserves the original Stage 2 behaviour (double HP,
 * one-damage-per-shot hits) without touching the shared Boss class.
 */
public class Boss extends Enemy {

    private int hp;
    private final int maxHp;
    private int width;
    private int height;
    private int vy = 2;
    private final int minY;
    private final int maxY;
    private final int holdX; // x where the boss stops and hovers

    // playfieldBottom = y of the top of the dashboard; the boss patrols above it.
    public Boss(int x, int y, int playfieldBottom) {
        this(x, y, playfieldBottom, BOSS_HP);
    }

    // Overload for tougher bosses (e.g. Stage 2's STAGE2_BOSS_HP).
    public Boss(int x, int y, int playfieldBottom, int hp) {
        super(x, y);

        // Bigger than a regular alien: double the normal scale.
        var ii = new ImageIcon(IMG_ENEMY);
        int scale = SCALE_FACTOR * 2;
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * scale,
                ii.getIconHeight() * scale,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);

        this.width = ii.getIconWidth() * scale;
        this.height = ii.getIconHeight() * scale;
        this.minY = 10;
        this.maxY = playfieldBottom - height - 20; // leave room for the HP bar
        this.holdX = BOARD_WIDTH - width - 60;
        this.hp = hp;
        this.maxHp = hp;
    }

    @Override
    public void act(int direction) {
        if (x > holdX) {
            // Slow entrance from the right edge.
            x -= 1;
        } else {
            // Patrol up and down in front of the player.
            y += vy;
            if (y < minY) {
                y = minY;
                vy = -vy;
            }
            if (y > maxY) {
                y = maxY;
                vy = -vy;
            }
        }
    }

    public void hit() {
        hp--;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
