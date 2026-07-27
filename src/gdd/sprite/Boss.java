package gdd.sprite;

import static gdd.Global.*;
import gdd.Stage;
import javax.swing.ImageIcon;

public class Boss extends Enemy {

    private int hp;
    private final int maxHp;
    private int width;
    private int height;
    private int vy;
    // Patrol limits in screen pixels. Not fixed at spawn: Scene1 refreshes
    // them from the cave walls every frame (setPatrolBounds), so the boss
    // turns around at the rock instead of flying through it.
    private int minY;
    private int maxY;
    private final int holdX; // x where the boss stops and hovers
    private final int spreadCount; // bullets per volley
    private final int fireCooldownMax; // frames between volleys, this stage's boss
    private int fireCooldown;

    // playfieldBottom = y of the top of the dashboard; the boss patrols above
    // it. The stage picks the artwork, HP, fire rate and patrol speed — so a
    // stage always shows the same boss however many times it is replayed, and
    // a later stage's boss can outright outgun an earlier one.
    public Boss(int x, int y, int playfieldBottom, Stage stage) {
        super(x, y);

        this.maxHp = stage.bossHp;
        this.hp = maxHp;
        this.vy = stage.bossPatrolSpeed;
        this.fireCooldownMax = stage.bossFireCooldown;
        this.fireCooldown = fireCooldownMax;
        this.spreadCount = Math.max(1, stage.bossSpreadCount);
        String imagePath = IMG_Boss[Math.floorMod(stage.number - 1, IMG_Boss.length)];
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

    /**
     * The band of open air the boss may patrol, as top/bottom screen y for its
     * own top-left corner. Scene1 recomputes this from the terrain each frame;
     * a corridor narrower than the boss collapses to a single safe y rather
     * than inverting the range.
     */
    public void setPatrolBounds(int top, int bottom) {
        this.minY = top;
        this.maxY = Math.max(top, bottom);
    }

    @Override
    public void act(int direction) {
        if (x > holdX) {
            // Entrance from the right edge.
            x -= 2;
        } else {
            // Patrol up and down in front of the player.
            y += vy;
        }

        // Bounce off the rock. Checked during the entrance too, so a boss that
        // slides in under a low ceiling is pushed clear instead of clipping it.
        if (y <= minY) {
            y = minY;
            vy = Math.abs(vy);
        } else if (y >= maxY) {
            y = maxY;
            vy = -Math.abs(vy);
        }

        if (fireCooldown > 0) {
            fireCooldown--;
        }
    }

    /** Ready to fire — but not until it has finished sliding into the arena. */
    public boolean readyToFire() {
        return fireCooldown == 0 && x <= holdX;
    }

    /** Restarts the trigger interval after Scene1 spawns the volley. */
    public void noteFired() {
        fireCooldown = fireCooldownMax;
    }

    /** How many bullets one volley holds — 1, or a spread on later stages. */
    public int getSpreadCount() {
        return spreadCount;
    }

    /**
     * The angle offsets of one volley, in radians, centred on the line to the
     * player: a single 0 for one bullet, or BOSS_SPREAD_DEGREES apart fanned
     * symmetrically about the aim for a spread.
     */
    public double[] volleyAngles() {
        double[] angles = new double[spreadCount];
        double step = Math.toRadians(BOSS_SPREAD_DEGREES);
        double first = -step * (spreadCount - 1) / 2.0;
        for (int i = 0; i < spreadCount; i++) {
            angles[i] = first + step * i;
        }
        return angles;
    }

    /** Where a bullet leaves the boss: the middle of its left-facing side. */
    public int getMuzzleX() {
        return x;
    }

    public int getMuzzleY() {
        return y + height / 2;
    }

    // damage is the player's per-bullet damage: 1 normally, 2 with the bullet
    // flower — BOSS_HP is sized so even the upgraded gun needs a dozen hits.
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
        return maxHp;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
