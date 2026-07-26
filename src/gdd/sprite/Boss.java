package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Boss extends Enemy {

    private int hp = BOSS_HP;
    private int width;
    private int height;
    private int vy = 2;
    private final int minY;
    private final int maxY;
    private final int holdX; // x where the boss stops and hovers
    public static int BossNo = 0;

    // playfieldBottom = y of the top of the dashboard; the boss patrols above it.
    public Boss(int x, int y, int playfieldBottom) {
        super(x, y);

        String imagePath = IMG_Boss[BossNo % IMG_Boss.length];
        BossNo++;
        // Bigger than a regular alien: double the normal scale.
        var ii = new ImageIcon(imagePath);
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

    // damage is the player's per-bullet damage: 1 normally, 2 with the
    // bullet flower, so an upgraded gun drops a boss in three hits.
    public void hit(int damage) {
        hp = Math.max(0, hp - damage); // never negative, the HP bar reads it
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return BOSS_HP;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
