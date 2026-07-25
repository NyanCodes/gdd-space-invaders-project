package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.ImageUtil;
import gdd.SpawnDetails;
import gdd.Stage;
import gdd.powerup.BulletUp;
import gdd.powerup.PowerUp;
import gdd.powerup.ShieldUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyPlane;
import gdd.sprite.EnemyShot;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import gdd.sprite.Sprite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<EnemyShot> enemyShots; // bullets fired by enemy planes
    private Player player;
    private Terrain terrain; // scrolling cave walls, top and bottom
    // private Shot shot;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0; // score: aliens +1, bosses +5
    private int carriedScore = 0; // score brought in from the previous stage

    private int lives = PLAYER_LIVES;
    private int invincibleFrames = 0; // blink window after a respawn
    private int nextAlienSpawnFrame = ALIEN_FIRST_WAVE_FRAME;
    private int nextPlaneSpawnFrame; // set from the stage in gameInit()
    private int nextPowerupFrame = POWERUP_FIRST_SECONDS * 60;
    private int bossSpawnIndex = 0;
    private Image lifeIcon; // small vertical (nose-up) ship for the HUD
    private Image tipIconSpeed; // small power-up icons for the start-of-game tip box
    private Image tipIconShield;
    private Image tipIconBullet;

    private boolean inGame = true;
    // Stage cleared but the game continues — the end screen offers NEXT STAGE
    // instead of EXIT.
    private boolean awaitingNextStage = false;
    private String message = "Game Over";
    // When the end screen appeared. A SPACE still held down from shooting would
    // otherwise exit the game the instant the player dies.
    private long gameOverAt = 0;
    private static final long GAMEOVER_INPUT_LOCKOUT_MS = 700;

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;
    private final Stage stage; // map file, length and boss schedule for this level

    private int currentRow = -1;
    // TODO load this map from a file
    private int mapOffset = 0;
    private final int[][] MAP = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;
    private int lastRowToShow;
    private int firstRowToShow;

    public Scene1(Game game) {
        this(game, Stage.ONE);
    }

    protected Scene1(Game game, Stage stage) {
        this.game = game;
        this.stage = stage;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
    }

    /** Score to start this stage with, carried over from the previous one. */
    public void setCarriedScore(int score) {
        this.carriedScore = score;
    }

    public int getScore() {
        return deaths;
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        // TODO load this from a file
        // Horizontal side-scroller: enemies enter from the right edge (x = BOARD_WIDTH)
        // and are spread out vertically by varying y.
        //
        // A calm scripted opening — a handful of aliens to get a feel for
        // shooting, plus the two power-ups that change how the ship plays.
        // Everything after this comes from the pressure ramp in update().
        spawnMap.put(180, new SpawnDetails("Alien1", BOARD_WIDTH, 220));   // 0:03
        spawnMap.put(420, new SpawnDetails("Alien1", BOARD_WIDTH, 380));   // 0:07
        spawnMap.put(600, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 260)); // 0:10
        spawnMap.put(900, new SpawnDetails("Alien1", BOARD_WIDTH, 160));   // 0:15
        spawnMap.put(1200, new SpawnDetails("Alien1", BOARD_WIDTH, 420));  // 0:20
        spawnMap.put(1500, new SpawnDetails("PowerUp-Bullet", BOARD_WIDTH, 320)); // 0:25
    }

    private void initBoard() {

    }

    public void start() {
        // Guard against double-registering if a scene is ever started twice.
        if (getKeyListeners().length == 0) {
            addKeyListener(new TAdapter());
        }
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        // Build the world (player, terrain, sprite lists) BEFORE the game-loop
        // timer can fire — otherwise the first tick runs update() on a null
        // player/terrain and the whole loop dies on an NPE.
        gameInit();

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        initAudio();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void gameInit() {

        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        enemyShots = new ArrayList<>();

        // Reset run state so a scene can be started more than once.
        frame = 0;
        lives = PLAYER_LIVES;
        deaths = carriedScore;
        invincibleFrames = 0;
        bossSpawnIndex = 0;
        nextAlienSpawnFrame = ALIEN_FIRST_WAVE_FRAME;
        // Only stage 1 delays the first plane; stage 2 opens with them.
        nextPlaneSpawnFrame = stage.firstPlaneFrame;
        nextPowerupFrame = POWERUP_FIRST_SECONDS * 60;
        inGame = true;
        awaitingNextStage = false;

        // Walls come from this stage's map file; the seed only matters if the
        // file is missing (procedural fallback) and for plant placement.
        terrain = new Terrain(stage.mapPath, randomizer.nextLong());

        // for (int i = 0; i < 4; i++) {
        // for (int j = 0; j < 6; j++) {
        // var enemy = new Enemy(ALIEN_INIT_X + (ALIEN_WIDTH + ALIEN_GAP) * j,
        // ALIEN_INIT_Y + (ALIEN_HEIGHT + ALIEN_GAP) * i);
        // enemies.add(enemy);
        // }
        // }
        player = new Player();
        // shot = new Shot();

        lifeIcon = createLifeIcon();

        // Small icons for the start-of-game power-up tip box.
        tipIconSpeed = ImageUtil.fit(IMG_POWERUP_SPEEDUP, TIP_ICON_SIZE, TIP_ICON_SIZE);
        tipIconShield = ImageUtil.fit(IMG_POWERUP_SHIELD, TIP_ICON_SIZE, TIP_ICON_SIZE);
        tipIconBullet = ImageUtil.fit(IMG_POWERUP_BULLET, TIP_ICON_SIZE, TIP_ICON_SIZE);
    }

    /**
     * How hard the game is pushing right now, 0 (a lone alien drifting past)
     * to 1 (full pressure). Each stage covers its own slice of the run-long
     * ramp, so difficulty climbs steadily across all seven minutes instead of
     * resetting when stage 2 starts.
     */
    private double pressure() {
        double stageProgress = Math.min(1.0,
                (double) frame / (stage.durationSeconds * 60));
        return stage.pressureStart
                + (stage.pressureEnd - stage.pressureStart) * stageProgress;
    }

    // Small vertical ship icon (nose up) for the lives display: the ship
    // sprite faces right, so rotate it 90 degrees counter-clockwise.
    private Image createLifeIcon() {
        var ii = new ImageIcon(IMG_PLAYER);
        int w = ii.getIconWidth();
        int h = ii.getIconHeight();

        BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.rotate(-Math.PI / 2);
        g2.translate(-w, 0);
        g2.drawImage(ii.getImage(), 0, 0, null);
        g2.dispose();
        return rotated;
    }

    // A sprite's bounding box shrunk by a margin on every side — used for
    // wall collisions so brushing a block with transparent pixels is fair.
    private Rectangle spriteRect(Sprite s, int margin) {
        return new Rectangle(s.getX() + margin, s.getY() + margin,
                s.getImage().getWidth(null) - margin * 2,
                s.getImage().getHeight(null) - margin * 2);
    }

    // Move a freshly spawned sprite's y into the open corridor at its x,
    // so nothing materializes inside a cave wall.
    private void clampIntoGap(Sprite s) {
        s.setY(terrain.clampToGap(s.getX(), frame, s.getY(),
                s.getImage().getHeight(null), playfieldBottom()));
    }

    // Top edge of the bottom dashboard; gameplay stays above this line.
    // Uses the real panel height so the bar is never cut off by window insets.
    private int playfieldBottom() {
        int h = getHeight();
        if (h <= 0) {
            h = BOARD_HEIGHT;
        }
        return h - DASHBOARD_HEIGHT;
    }

    private void drawTerrain(Graphics g) {
        int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
        terrain.draw(g, frame, panelW, playfieldBottom());
    }

    private void drawMap(Graphics g) {
        // Draw horizontally scrolling starfield background (right -> left).

        // Smooth scrolling offset (1 pixel per frame).
        int scrollOffset = (frame) % BLOCKWIDTH;

        // Which MAP columns are currently visible, based on how far we've scrolled.
        int baseCol = (frame) / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2; // +2 for smooth scrolling

        // Loop through columns that should be visible on screen.
        for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
            // Which MAP column to use (with wrapping).
            int mapCol = (baseCol + screenCol) % MAP[0].length;

            // X position for this column; content marches leftward.
            int x = (screenCol * BLOCKWIDTH) - scrollOffset;

            // Skip if column is completely off-screen.
            if (x > BOARD_WIDTH || x < -BLOCKWIDTH) {
                continue;
            }

            // Draw each row in this column.
            for (int row = 0; row < MAP.length; row++) {
                if (MAP[row][mapCol] == 1) {
                    // Y position for this row.
                    int y = row * BLOCKHEIGHT;

                    // Draw a cluster of stars.
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }

    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        // Set star color to white
        g.setColor(Color.WHITE);

        // Draw multiple stars in a cluster pattern
        // Main star (larger)
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);

        // Smaller surrounding stars
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);

        // Tiny stars for more detail
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }

    private void drawAliens(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }

            if (enemy.isDying()) {

                enemy.die();
            }
        }
    }

    private void drawPowreUps(Graphics g) {

        for (PowerUp p : powerups) {

            if (p.isVisible()) {

                g.drawImage(p.getImage(), p.getX(), p.getY(), this);
            }

            if (p.isDying()) {

                p.die();
            }
        }
    }

    private void drawPlayer(Graphics g) {

        // Blink while invincible right after a respawn.
        boolean blinkHidden = invincibleFrames > 0 && (invincibleFrames / 4) % 2 == 0;

        if (player.isVisible() && !blinkHidden) {

            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }

        // Golden shield circle while the shield power-up is active; blinks
        // during the last 2 seconds as a running-out warning.
        if (player.isVisible() && player.isShieldActive()) {
            int left = player.getShieldFrames();
            boolean warnHidden = left < 120 && (left / 6) % 2 == 0;
            if (!warnHidden) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = player.getX() + player.getWidth() / 2;
                int cy = player.getY() + player.getHeight() / 2;
                int r = Math.max(player.getWidth(), player.getHeight()) / 2 + 10;

                g2.setColor(new Color(255, 215, 0, 40)); // soft golden glow
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                var oldStroke = g2.getStroke();
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.setColor(new Color(255, 200, 40, 220));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                g2.setStroke(oldStroke);

                // Seconds remaining, just above the circle.
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString((left / 60 + 1) + "s", cx - 6, cy - r - 4);
            }
        }
    }

    private void drawShot(Graphics g) {

        for (Shot shot : shots) {

            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawEnemyShots(Graphics g) {

        for (EnemyShot bullet : enemyShots) {

            if (bullet.isVisible()) {
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
            }
        }
    }

    private void drawBombing(Graphics g) {

        // for (Enemy e : enemies) {
        //     Enemy.Bomb b = e.getBomb();
        //     if (!b.isDestroyed()) {
        //         g.drawImage(b.getImage(), b.getX(), b.getY(), this);
        //     }
        // }
    }

    private void drawExplosions(Graphics g) {

        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {

            if (explosion.isVisible()) {
                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.visibleCountDown();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {

            drawMap(g);         // scrolling starfield on black, behind everything
            drawTerrain(g);     // cave walls along the top and bottom
            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawEnemyShots(g);
            drawBossHpBars(g);
            drawTipBox(g);      // power-up guide, shown for the first few seconds
            drawDashboard(g);   // all game status lives in the bottom bar

        } else {

            if (timer.isRunning()) {
                timer.stop();
            }

            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBossHpBars(Graphics g) {

        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss && enemy.isVisible() && !enemy.isDying()) {
                Boss boss = (Boss) enemy;

                int barW = boss.getWidth();
                int barH = 7;
                int bx = boss.getX();
                int by = boss.getY() + boss.getHeight() + 6; // just below the boss

                g.setColor(new Color(40, 0, 0));
                g.fillRect(bx, by, barW, barH);
                g.setColor(Color.red);
                g.fillRect(bx + 1, by + 1,
                        (barW - 2) * boss.getHp() / boss.getMaxHp(), barH - 2);
                g.setColor(Color.white);
                g.drawRect(bx, by, barW, barH);
            }
        }
    }

    // Power-up guide in the top-right corner, visible for the first
    // TIPBOX_SECONDS of the run so new players know what the drops do.
    private void drawTipBox(Graphics g) {

        if (frame > TIPBOX_SECONDS * 60) {
            return;
        }

        int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
        int w = 150;
        int h = 116;
        int x = panelW - w - 12;
        int y = 12;

        g.setColor(new Color(4, 12, 24, 210));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(255, 200, 40));
        g.drawRect(x, y, w, h);

        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(new Color(0, 255, 120));
        g.drawString("POWER-UPS", x + 10, y + 18);

        int row1 = y + 26;
        int row2 = y + 54;
        int row3 = y + 82;
        g.drawImage(tipIconSpeed, x + 10, row1, this);
        g.drawImage(tipIconShield, x + 10, row2, this);
        g.drawImage(tipIconBullet, x + 10, row3, this);

        g.setColor(Color.white);
        g.drawString("SPEED UP", x + 40, row1 + 15);
        g.drawString("SHIELD", x + 40, row2 + 15);
        g.drawString("BULLET x2", x + 40, row3 + 15);
    }

    private void drawDashboard(Graphics g) {

        int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
        int top = playfieldBottom();

        // Dark space panel with a green scanline on top — matches the
        // black/green/white arcade look of the rest of the game.
        g.setColor(new Color(4, 12, 24));
        g.fillRect(0, top, panelW, DASHBOARD_HEIGHT);
        g.setColor(new Color(0, 255, 120));
        g.fillRect(0, top, panelW, 2);

        Font labelFont = new Font("Monospaced", Font.BOLD, 12);
        Font valueFont = new Font("Monospaced", Font.BOLD, 16);
        int labelY = top + 20;
        int valueY = top + 44;

        g.setFont(labelFont);
        g.setColor(new Color(0, 255, 120));
        g.drawString("LIVES", 14, labelY);
        g.drawString("HULL", 95, labelY);
        g.drawString("SPEED", 140, labelY);
        g.drawString("BULLET", 250, labelY);
        g.drawString("SCORE", 370, labelY);

        // Bullet-flower badge next to the BULLET label: x2 shots per reload,
        // x2 damage.
        if (player.hasBulletFlower()) {
            g.setColor(new Color(255, 200, 40));
            g.drawString("x" + player.getShotsPerBurst() + " D" + player.getShotDamage(),
                    306, labelY);
            g.setColor(new Color(0, 255, 120)); // back to the label colour
        }

        // Stage number, sitting above the timer in the right corner.
        String stageLabel = "STAGE " + stage.number;
        FontMetrics lfm = g.getFontMetrics(labelFont);
        g.drawString(stageLabel, panelW - lfm.stringWidth(stageLabel) - 14, labelY);

        // Remaining lives as small vertical ship icons.
        if (lifeIcon != null) {
            int iw = lifeIcon.getWidth(null);
            for (int i = 0; i < lives; i++) {
                g.drawImage(lifeIcon, 14 + i * (iw + 10), labelY + 6, this);
            }
        }

        // Hull pips: only enemy planes and their bullets drain these, and
        // emptying them costs one of the lives above.
        for (int i = 0; i < player.getMaxHull(); i++) {
            int hx = 95 + i * 18;
            int hy = valueY - 13;
            g.setColor(i < player.getHull() ? new Color(0, 255, 120) : new Color(30, 45, 60));
            g.fillRect(hx, hy, 14, 14);
            g.setColor(new Color(4, 12, 24));
            g.drawRect(hx, hy, 14, 14);
        }

        g.setFont(valueFont);
        g.setColor(Color.white);
        g.drawString(String.valueOf(player.getSpeed()), 140, valueY);
        g.drawString(String.format("%d %.1fs", player.getShotSpeed(),
                player.getShotCooldown() / 60.0), 250, valueY);
        g.drawString(String.valueOf(deaths), 370, valueY);

        // Reload bar under the BULLET value: fills back up between shots.
        int rbW = 90;
        int rbH = 4;
        int rbY = valueY + 6;
        g.setColor(new Color(30, 45, 60));
        g.fillRect(250, rbY, rbW, rbH);
        int ready = rbW * (player.getShotCooldown() - player.getCooldownLeft())
                / Math.max(1, player.getShotCooldown());
        g.setColor(player.canShoot() ? new Color(0, 255, 120) : Color.orange);
        g.fillRect(250, rbY, ready, rbH);

        // Flashing boss alert while a boss is on screen.
        boolean bossAlive = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss && enemy.isVisible() && !enemy.isDying()) {
                bossAlive = true;
                break;
            }
        }
        if (bossAlive && (frame / 20) % 2 == 0) {
            g.setColor(Color.red);
            g.drawString("!! BOSS !!", 460, valueY);
        }

        // Game timer, bottom-right corner.
        int seconds = frame / 60;
        String time = String.format("TIME %02d:%02d", seconds / 60, seconds % 60);
        FontMetrics fm = g.getFontMetrics(valueFont);
        g.setColor(new Color(0, 255, 120));
        g.drawString(time, panelW - fm.stringWidth(time) - 14, valueY);
    }

    private void gameOver(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);

        // Running score, so a stage transition shows what carries over.
        String score = "SCORE " + deaths;
        g.setColor(new Color(0, 255, 120));
        g.drawString(score, (BOARD_WIDTH - fontMetrics.stringWidth(score)) / 2,
                BOARD_WIDTH / 2 + 34);

        drawEndButton(g);
    }

    // The single button on the end screen: NEXT STAGE between levels, EXIT
    // once the run is over. The timer is stopped by now, so this is drawn once
    // and can't blink like the title menu — it stays highlighted instead.
    private void drawEndButton(Graphics g) {

        int bw = 200;
        int bh = 44;
        int bx = (BOARD_WIDTH - bw) / 2;
        int by = BOARD_WIDTH / 2 + 50;

        g.setColor(new Color(0, 32, 48));
        g.fillRect(bx, by, bw, bh);
        g.setColor(new Color(255, 200, 40));
        g.drawRect(bx, by, bw, bh);

        var label = new Font("Helvetica", Font.BOLD, 22);
        var labelMetrics = this.getFontMetrics(label);
        String text = awaitingNextStage ? "> NEXT STAGE <" : "> EXIT <";
        g.setFont(label);
        g.setColor(Color.white);
        g.drawString(text, bx + (bw - labelMetrics.stringWidth(text)) / 2, by + 30);

        var hintFont = new Font("Helvetica", Font.BOLD, 12);
        var hintMetrics = this.getFontMetrics(hintFont);
        String hint = awaitingNextStage ? "ENTER to continue" : "ENTER to exit";
        g.setFont(hintFont);
        g.setColor(Color.gray);
        g.drawString(hint, (BOARD_WIDTH - hintMetrics.stringWidth(hint)) / 2, by + bh + 22);
    }

    // Lost a life but the run continues: back to the start position, nudged
    // into the open corridor so the ship never reappears inside a wall.
    private void respawnPlayer() {
        player.respawn();
        player.setY(terrain.clampToGap(player.getX(), frame, player.getY(),
                player.getHeight(), playfieldBottom()));
        invincibleFrames = 120; // 2s of blinking safety
    }

    private void endGame(String msg) {
        inGame = false;
        timer.stop();
        message = msg;
        gameOverAt = System.currentTimeMillis();
    }

    // One life gone: either the run is over or the ship comes back at the
    // start position with a full hull and a blink of safety.
    private void loseLife() {
        lives--;
        if (lives <= 0) {
            player.die();
            endGame("Game Over");
        } else {
            respawnPlayer();
        }
    }

    // A hit from a plane or one of its bullets costs a hull point instead of a
    // whole life. Emptying the hull is what spends the life.
    private void absorbPlaneHit() {
        if (player.takeHit()) {
            loseLife();
        } else {
            invincibleFrames = HIT_INVINCIBLE_FRAMES;
        }
    }

    // What killing this enemy is worth on the scoreboard.
    private int scoreFor(Enemy enemy) {
        if (enemy instanceof Boss) {
            return BOSS_SCORE;
        }
        if (enemy instanceof EnemyPlane) {
            return PLANE_SCORE;
        }
        return ALIEN_SCORE;
    }

    // Explosions are drawn from their top-left corner, so centre one on a point.
    private void explodeAt(int cx, int cy) {
        Explosion boom = new Explosion(cx, cy);
        explosions.add(boom);
        boom.setX(cx - boom.getImage().getWidth(null) / 2);
        boom.setY(cy - boom.getImage().getHeight(null) / 2);
    }

    /**
     * One enemy plane from the pool. Plane 1 only ever attacks the player's
     * front (in from the right edge), plane 2 only the player's back (in from
     * the left), and plane 3 does either.
     */
    private void spawnPlane() {

        EnemyPlane.Type[] types = EnemyPlane.Type.values();
        EnemyPlane.Type type = types[randomizer.nextInt(types.length)];
        EnemyPlane.Heading heading = type.pickHeading(randomizer);

        int py = 10 + randomizer.nextInt(Math.max(1, playfieldBottom() - PLANE_SIZE - 20));
        int px = heading == EnemyPlane.Heading.LEFTWARD
                ? BOARD_WIDTH + randomizer.nextInt(80)
                : -PLANE_SIZE - randomizer.nextInt(80);

        EnemyPlane plane = new EnemyPlane(type, heading, px, py);
        clampIntoGap(plane);
        enemies.add(plane);
    }

    private void update() {

        // Stage clear once this stage's full length is survived.
        if (frame >= stage.durationSeconds * 60) {
            awaitingNextStage = !stage.last;
            endGame(stage.last
                    ? "All Stages Clear!"
                    : "Stage " + stage.number + " Clear!");
            return;
        }

        // Check enemy spawn (scripted spawns)
        // TODO this approach can only spawn one enemy at a frame
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            // Create a new enemy based on the spawn details
            switch (sd.type) {
                case "Alien1":
                    Enemy enemy = new Alien1(sd.x, sd.y);
                    clampIntoGap(enemy);
                    enemies.add(enemy);
                    break;
                // Add more cases for different enemy types if needed
                case "Alien2":
                    // Enemy enemy2 = new Alien2(sd.x, sd.y);
                    // enemies.add(enemy2);
                    break;
                case "PowerUp-SpeedUp":
                    // Handle speed up item spawn
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    clampIntoGap(speedUp);
                    powerups.add(speedUp);
                    break;
                case "PowerUp-Shield":
                    PowerUp shield = new ShieldUp(sd.x, sd.y);
                    clampIntoGap(shield);
                    powerups.add(shield);
                    break;
                case "PowerUp-Bullet":
                    PowerUp bullet = new BulletUp(sd.x, sd.y);
                    clampIntoGap(bullet);
                    powerups.add(bullet);
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }

        // Random aliens from the front (right edge) at random heights. Both the
        // size of a wave and how often waves arrive follow the run-long
        // pressure ramp: one lonely alien every few seconds early on, packs of
        // two or three barely a second apart by the end of stage 2.
        if (frame >= nextAlienSpawnFrame) {
            double p = pressure();

            int count;
            if (p < 0.25) {
                count = 1;
            } else if (p < 0.60) {
                count = 1 + randomizer.nextInt(2);
            } else {
                count = 2 + randomizer.nextInt(2);
            }

            for (int i = 0; i < count; i++) {
                int ay = 10 + randomizer.nextInt(Math.max(1, playfieldBottom() - 60));
                Alien1 alien = new Alien1(BOARD_WIDTH + randomizer.nextInt(80), ay);
                clampIntoGap(alien);
                enemies.add(alien);
            }

            int gap = (int) Math.round(ALIEN_WAVE_GAP_START
                    - (ALIEN_WAVE_GAP_START - ALIEN_WAVE_GAP_END) * p);
            nextAlienSpawnFrame = frame + gap
                    + randomizer.nextInt(ALIEN_WAVE_GAP_JITTER);
        }

        // Enemy planes arrive alongside the alien waves but on their own,
        // slower cadence — they take twice the bullets and shoot back, so one
        // at a time is plenty. The gap follows the same pressure ramp.
        if (frame >= nextPlaneSpawnFrame) {
            spawnPlane();

            int gap = (int) Math.round(PLANE_GAP_START
                    - (PLANE_GAP_START - PLANE_GAP_END) * pressure());
            nextPlaneSpawnFrame = frame + gap
                    + randomizer.nextInt(PLANE_GAP_JITTER);
        }

        // A power-up drop every POWERUP_MIN..MAX seconds — rare enough to feel
        // like a find, regular enough that there is always one on the way. The
        // bullet flower is permanent, so it drops out of the pool once held.
        if (frame >= nextPowerupFrame) {
            int py = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
            PowerUp drop;
            switch (randomizer.nextInt(player.hasBulletFlower() ? 2 : 3)) {
                case 0:
                    drop = new SpeedUp(BOARD_WIDTH, py);
                    break;
                case 1:
                    drop = new ShieldUp(BOARD_WIDTH, py);
                    break;
                default:
                    drop = new BulletUp(BOARD_WIDTH, py);
                    break;
            }
            clampIntoGap(drop);
            powerups.add(drop);
            nextPowerupFrame = frame + (POWERUP_MIN_SECONDS
                    + randomizer.nextInt(POWERUP_MAX_SECONDS - POWERUP_MIN_SECONDS + 1)) * 60;
        }

        // Boss schedule — per stage, timed to land in the map's wide sections.
        if (bossSpawnIndex < stage.bossSeconds.length
                && frame == stage.bossSeconds[bossSpawnIndex] * 60) {
            enemies.add(new Boss(BOARD_WIDTH, playfieldBottom() / 2 - 40, playfieldBottom()));
            bossSpawnIndex++;
        }

        // player
        if (invincibleFrames > 0) {
            invincibleFrames--;
        }
        player.act();
        // Keep the ship above the dashboard.
        int maxPlayerY = playfieldBottom() - player.getHeight() - 2;
        if (player.getY() > maxPlayerY) {
            player.setY(maxPlayerY);
        }

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    powerup.upgrade(player);
                }
            }
        }
        powerups.removeIf(p -> !p.isVisible());

        // Enemies
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
            }
        }

        // Planes shoot on the same interval as the player's starting gun,
        // aimed at wherever the ship is at the moment the trigger is pulled.
        if (player.isVisible()) {
            int aimX = player.getX() + player.getWidth() / 2;
            int aimY = player.getY() + player.getHeight() / 2;

            for (Enemy enemy : enemies) {
                if (!(enemy instanceof EnemyPlane) || !enemy.isVisible() || enemy.isDying()) {
                    continue;
                }
                EnemyPlane plane = (EnemyPlane) enemy;
                if (plane.readyToFire()) {
                    enemyShots.add(new EnemyShot(plane.getMuzzleX(), plane.getMuzzleY(),
                            aimX, aimY, plane.getHeading().step));
                    plane.noteFired();
                }
            }
        }

        // Two planes flying into each other — traffic crossing from opposite
        // edges — bring each other down. Nobody scores for it.
        for (int i = 0; i < enemies.size(); i++) {
            Enemy first = enemies.get(i);
            if (!(first instanceof EnemyPlane) || !first.isVisible() || first.isDying()) {
                continue;
            }
            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy second = enemies.get(j);
                if (!(second instanceof EnemyPlane) || !second.isVisible() || second.isDying()) {
                    continue;
                }
                if (first.collidesWith(second)) {
                    first.setDying(true);
                    second.setDying(true);
                    explosions.add(new Explosion(first.getX(), first.getY()));
                    explosions.add(new Explosion(second.getX(), second.getY()));
                    break;
                }
            }
        }

        // Walls destroy regular aliens and planes that drift into them (no
        // score); the boss hovers over the terrain and is exempt.
        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && !enemy.isDying() && !(enemy instanceof Boss)
                    && terrain.collides(spriteRect(enemy, 6), frame, playfieldBottom())) {
                enemy.setDying(true);
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
            }
        }

        // Player <-> enemy collision: with the golden shield up, ramming an
        // enemy kills it (and scores it). Otherwise a plane costs a hull point
        // and anything else costs a whole life.
        if (player.isVisible()) {
            for (Enemy enemy : enemies) {
                if (enemy.isVisible() && !enemy.isDying() && player.collidesWith(enemy)) {
                    if (player.isShieldActive()) {
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths += scoreFor(enemy);
                        continue;
                    }
                    if (invincibleFrames > 0) {
                        continue; // still blinking after a respawn or a hit
                    }
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    if (!(enemy instanceof Boss)) {
                        // The alien or plane is destroyed in the crash (no score).
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    }
                    if (enemy instanceof EnemyPlane) {
                        absorbPlaneHit();
                    } else {
                        loseLife();
                    }
                    break;
                }
            }
        }

        // Player <-> wall: crashing into the cave costs a life. The
        // post-respawn blink protects here too; the golden shield does not.
        if (inGame && player.isVisible() && invincibleFrames == 0
                && terrain.collides(spriteRect(player, 8), frame, playfieldBottom())) {
            explosions.add(new Explosion(player.getX(), player.getY()));
            loseLife();
        }

        // Enemy bullets. The golden shield swallows one and then breaks;
        // otherwise the hit comes off the hull. They splash on the cave walls
        // the same way the player's shots do.
        List<EnemyShot> enemyShotsToRemove = new ArrayList<>();
        for (EnemyShot bullet : enemyShots) {

            if (!bullet.isVisible()) {
                enemyShotsToRemove.add(bullet);
                continue;
            }

            bullet.act();

            if (bullet.isOffscreen()
                    || terrain.collides(spriteRect(bullet, 1), frame, playfieldBottom())) {
                bullet.die();
                enemyShotsToRemove.add(bullet);
                continue;
            }

            if (inGame && player.isVisible() && bullet.collidesWith(player)) {
                bullet.die();
                enemyShotsToRemove.add(bullet);

                if (player.isShieldActive()) {
                    player.breakShield();
                    explodeAt(player.getX() + player.getWidth() / 2,
                            player.getY() + player.getHeight() / 2);
                    continue;
                }
                if (invincibleFrames > 0) {
                    continue;
                }
                explosions.add(new Explosion(player.getX(), player.getY()));
                absorbPlaneHit();
            }
        }
        enemyShots.removeAll(enemyShotsToRemove);

        // shot
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {

            if (shot.isVisible()) {

                for (Enemy enemy : enemies) {
                    // Collision detection: shot and enemy
                    if (enemy.isVisible() && !enemy.isDying() && shot.isVisible()
                            && shot.collidesWith(enemy)) {

                        if (enemy instanceof Boss) {
                            Boss boss = (Boss) enemy;
                            boss.hit(player.getShotDamage());
                            if (boss.isDead()) {
                                boss.setDying(true);
                                explodeAt(boss.getX() + boss.getWidth() / 2,
                                        boss.getY() + boss.getHeight() / 2);
                                deaths += scoreFor(boss);
                            }
                        } else if (enemy instanceof EnemyPlane) {
                            // Two default bullets bring a plane down; the
                            // bullet flower's double damage does it in one.
                            EnemyPlane plane = (EnemyPlane) enemy;
                            plane.hit(player.getShotDamage());
                            if (plane.isDead()) {
                                plane.setDying(true);
                                explodeAt(plane.getX() + plane.getWidth() / 2,
                                        plane.getY() + plane.getHeight() / 2);
                                deaths += scoreFor(plane);
                            } else {
                                // Still flying — a spark at the impact so the
                                // player can tell the first bullet landed.
                                explodeAt(shot.getX(), shot.getY());
                            }
                        } else {
                            enemy.setDying(true);
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                            deaths += scoreFor(enemy);
                        }
                        shot.die();
                        shotsToRemove.add(shot);
                        break;
                    }
                }

                // Shots travel to the right at the player's bullet speed;
                // remove once off the right edge.
                if (shot.isVisible()) {
                    int newX = shot.getX() + player.getShotSpeed();

                    if (newX > BOARD_WIDTH) {
                        shot.die();
                        shotsToRemove.add(shot);
                    } else {
                        shot.setX(newX);
                        // Bullets splash against the cave walls.
                        if (terrain.collides(spriteRect(shot, 2), frame, playfieldBottom())) {
                            shot.die();
                            shotsToRemove.add(shot);
                        }
                    }
                }
            }
        }
        shots.removeAll(shotsToRemove);

        // Drop fully dead enemies so the list stays small over a 7-minute run.
        enemies.removeIf(e -> !e.isVisible());

        // enemies
        // for (Enemy enemy : enemies) {
        //     int x = enemy.getX();
        //     if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
        //         direction = -1;
        //         for (Enemy e2 : enemies) {
        //             e2.setY(e2.getY() + GO_DOWN);
        //         }
        //     }
        //     if (x <= BORDER_LEFT && direction != 1) {
        //         direction = 1;
        //         for (Enemy e : enemies) {
        //             e.setY(e.getY() + GO_DOWN);
        //         }
        //     }
        // }
        // for (Enemy enemy : enemies) {
        //     if (enemy.isVisible()) {
        //         int y = enemy.getY();
        //         if (y > GROUND - ALIEN_HEIGHT) {
        //             inGame = false;
        //             message = "Invasion!";
        //         }
        //         enemy.act(direction);
        //     }
        // }
        // bombs - collision detection
        // Bomb is with enemy, so it loops over enemies
        /*
        for (Enemy enemy : enemies) {

            int chance = randomizer.nextInt(15);
            Enemy.Bomb bomb = enemy.getBomb();

            if (chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                bomb.setDestroyed(false);
                bomb.setX(enemy.getX());
                bomb.setY(enemy.getY());
            }

            int bombX = bomb.getX();
            int bombY = bomb.getY();
            int playerX = player.getX();
            int playerY = player.getY();

            if (player.isVisible() && !bomb.isDestroyed()
                    && bombX >= (playerX)
                    && bombX <= (playerX + PLAYER_WIDTH)
                    && bombY >= (playerY)
                    && bombY <= (playerY + PLAYER_HEIGHT)) {

                var ii = new ImageIcon(IMG_EXPLOSION);
                player.setImage(ii.getImage());
                player.setDying(true);
                bomb.setDestroyed(true);
            }

            if (!bomb.isDestroyed()) {
                bomb.setY(bomb.getY() + 1);
                if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                    bomb.setDestroyed(true);
                }
            }
        }
         */
    }

    private void doGameCycle() {
        frame++;
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void handleGameOverKey(int key) {

        if (System.currentTimeMillis() - gameOverAt < GAMEOVER_INPUT_LOCKOUT_MS) {
            return;
        }

        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
            if (awaitingNextStage) {
                game.loadNextStage(stage.number + 1, deaths);
                return;
            }
            stop(); // stops the timer and the music
            System.exit(0);
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("Scene2.keyPressed: " + e.getKeyCode());

            if (!inGame) {
                handleGameOverKey(e.getKeyCode());
                return;
            }

            player.keyPressed(e);

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE && inGame) {
                System.out.println("Shots: " + shots.size());
                if (shots.size() < 4 && player.canShoot()) {
                    // Fire from the tip of the ship: right edge, vertically centered.
                    int tipX = player.getX() + player.getWidth();
                    int tipY = player.getY() + player.getHeight() / 2;
                    Shot shot = new Shot(tipX, tipY, player.hasBulletFlower());
                    shots.add(shot);
                    player.startShotCooldown();
                }
            }

        }
    }
}
