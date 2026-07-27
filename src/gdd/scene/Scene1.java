package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.GunTier;
import gdd.ImageUtil;
import gdd.SpawnDetails;
import gdd.Stage;
import gdd.powerup.BulletUp;
import gdd.powerup.HeartUp;
import gdd.powerup.PowerUp;
import gdd.powerup.ShieldUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Boss;
import gdd.sprite.BossShot;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private int frame = 0;
    // How far the world has scrolled, in pixels. Normally this tracks frame
    // 1:1, but it stops while a boss is on the field so the fight always takes
    // place in the wide arena the map author put there — the fight lasts as
    // long as it lasts, and the cave must not grind the player into a wall
    // meanwhile. frame stays the master clock for spawns and the HUD timer.
    private int terrainScroll = 0;
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
    private int nextHeartFrame = HEART_FIRST_SECONDS * 60; // hearts run their own clock
    private boolean bossSpawned = false; // one boss per stage, and only once
    // Counts down from the killing blow to the clear screen; -1 when idle.
    private int clearDelayFrames = -1;
    // Toughest plane model the player has actually met. The gun ladder is
    // gated on this, so an upgrade always arrives as the answer to an enemy
    // the player has already had to deal with.
    private int seenPlaneTier = 0;
    // The gun ladder's own clock — see Stage.gunUpgradeFirstSeconds and
    // dueGunTier(). Starts at 0 so the very first rung is only held back by
    // its own scheduled time; advances by GUN_DROP_RETRY_SECONDS each time a
    // rung is dropped, so a miss gets retried rather than waiting on the next
    // rung's schedule.
    private int nextGunDropFrame = 0;
    private Image lifeIcon; // small vertical (nose-up) ship for the HUD
    private Image tipIconSpeed; // small power-up icons for the start-of-game tip box
    private Image tipIconShield;
    private Image tipIconBullet;
    private Image tipIconHeart;

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
            String filePath = (stage.number == 1) ? Stage1_sfx : Stage2_sfx;
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
        terrainScroll = 0;
        lives = PLAYER_LIVES;
        deaths = carriedScore;
        invincibleFrames = 0;
        bossSpawned = false;
        clearDelayFrames = -1;
        seenPlaneTier = 0;
        nextGunDropFrame = 0;
        nextAlienSpawnFrame = ALIEN_FIRST_WAVE_FRAME;
        // Only stage 1 delays the first plane; stage 2 opens with them.
        nextPlaneSpawnFrame = stage.firstPlaneFrame;
        nextPowerupFrame = POWERUP_FIRST_SECONDS * 60;
        nextHeartFrame = HEART_FIRST_SECONDS * 60;
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
        // Each stage is entered on a known footing rather than inheriting the
        // gun: stage 2 starts on the 2x flower, which is what its heavy planes
        // are balanced against.
        player.equipGun(stage.startingGun);
        // Anything the starting gun already answers counts as met, or the
        // ladder would offer a rung the player is holding.
        seenPlaneTier = stage.startingGun.unlockPlaneTier;
        // shot = new Shot();

        lifeIcon = createLifeIcon();

        // Small icons for the start-of-game power-up tip box.
        tipIconSpeed = ImageUtil.fit(IMG_POWERUP_SPEEDUP[0], TIP_ICON_SIZE, TIP_ICON_SIZE);
        tipIconShield = ImageUtil.fit(IMG_POWERUP_SHIELD[0], TIP_ICON_SIZE, TIP_ICON_SIZE);
        tipIconBullet = ImageUtil.fit(IMG_POWERUP_BULLET[0], TIP_ICON_SIZE, TIP_ICON_SIZE);
        tipIconHeart = ImageUtil.fit(IMG_POWERUP_HEART[0], TIP_ICON_SIZE, TIP_ICON_SIZE);
    }

    /**
     * How hard the game is pushing right now, 0 (a lone alien drifting past)
     * to 1 (full pressure). Each stage covers its own slice of the run-long
     * ramp, so difficulty climbs steadily across the whole run instead of
     * resetting when stage 2 starts. The ramp is spread over the approach to
     * the boss, so the stage is at its hardest just as the boss arrives.
     */
    private double pressure() {
        double stageProgress = Math.min(1.0,
                (double) frame / (stage.bossSecond * 60));
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

    // Move a freshly spawned sprite's y into the open corridor it spans,
    // so nothing materializes inside a cave wall.
    private void clampIntoGap(Sprite s) {
        s.setY(terrain.clampToGap(s.getX(), s.getImage().getWidth(null),
                terrainScroll, s.getY(),
                s.getImage().getHeight(null), playfieldBottom()));
    }

    // Same as clampIntoGap, but also keeps power-ups POWERUP_EDGE_MARGIN
    // clear of the top of the playfield and the dashboard — a wide-open
    // corridor would otherwise let clampIntoGap push one right up against
    // either edge.
    private void clampPowerUpIntoGap(Sprite s) {
        clampIntoGap(s);
        int h = s.getImage().getHeight(null);
        int minY = POWERUP_EDGE_MARGIN;
        int maxY = Math.max(minY, playfieldBottom() - POWERUP_EDGE_MARGIN - h);
        s.setY(Math.max(minY, Math.min(s.getY(), maxY)));
    }

    /**
     * Steers a sprite back toward the open corridor it is flying through,
     * moving at most ENEMY_DODGE_SPEED this frame so it banks around the rock
     * instead of snapping to a new height.
     *
     * It holds ENEMY_DODGE_MARGIN of open air off each rock face, which is what
     * keeps enemies shootable: the gun only fires straight right, so an enemy
     * grazing a wall could only be answered by parking against that wall.
     *
     * It looks ENEMY_DODGE_LOOKAHEAD either side of itself and obeys the
     * tightest column in that span, which is what makes it start climbing
     * before the wall arrives rather than once it is already inside it.
     *
     * Returns true if the corridor is genuinely narrower than the sprite, i.e.
     * there is no height that would keep it clear.
     */
    private boolean dodgeTerrain(Sprite s) {
        int w = s.getImage().getWidth(null);
        int h = s.getImage().getHeight(null);
        int[] gap = terrain.gapBounds(s.getX() - ENEMY_DODGE_LOOKAHEAD,
                w + ENEMY_DODGE_LOOKAHEAD * 2, terrainScroll, playfieldBottom());

        int top = gap[0] + ENEMY_DODGE_MARGIN;
        int bottom = gap[1] - ENEMY_DODGE_MARGIN - h;

        // Too tight even for the margins: aim for the middle and take what we
        // can get. Only a corridor shorter than the sprite is truly hopeless.
        int target = bottom < top
                ? (gap[0] + gap[1] - h) / 2
                : Math.max(top, Math.min(s.getY(), bottom));

        int dy = target - s.getY();
        if (dy != 0) {
            s.setY(s.getY() + Math.max(-ENEMY_DODGE_SPEED,
                    Math.min(ENEMY_DODGE_SPEED, dy)));
        }
        return gap[1] - gap[0] < h;
    }

    /**
     * Should the cave hold still? It does from the moment the boss enters
     * until the clear screen, including the beat after the killing blow — the
     * world should not lurch back into motion under the death explosion.
     */
    private boolean scrollFrozen() {
        if (clearDelayFrames >= 0) {
            return true;
        }
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss && enemy.isVisible() && !enemy.isDying()) {
                return true;
            }
        }
        return false;
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
        terrain.draw(g, terrainScroll, panelW, playfieldBottom());
    }

    private void drawMap(Graphics g) {
        // Draw horizontally scrolling starfield background (right -> left).
        // Shares terrainScroll with the cave walls so the whole world freezes
        // together during a boss fight instead of the stars drifting past
        // stationary rock.

        // Smooth scrolling offset (1 pixel per scrolled frame).
        int scrollOffset = (terrainScroll) % BLOCKWIDTH;

        // Which MAP columns are currently visible, based on how far we've scrolled.
        int baseCol = (terrainScroll) / BLOCKWIDTH;
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
                explodeAt(enemy.getX() + 2, enemy.getY() + 2);
                enemy.die();
                try {
                    audioPlayer = new AudioPlayer(Explosion_sfx);
                    audioPlayer.playOnce();
                } catch (Exception e) {
                    System.err.println("Error initializing audio player: " + e.getMessage());
                }
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

                explosion.act(0);

                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.visibleCountDown();
                if (explosion.isFinished()) {
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

        // One row per pickup, so the box grows if another is ever added.
        Image[] icons = {tipIconSpeed, tipIconShield, tipIconBullet, tipIconHeart};
        String[] labels = {"SPEED UP", "SHIELD", "BULLET x2", "EXTRA LIFE"};
        int rowH = 28;

        int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
        int w = 150;
        int h = 32 + rowH * icons.length;
        int x = panelW - w - 12;
        int y = 12;

        g.setColor(new Color(4, 12, 24, 210));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(255, 200, 40));
        g.drawRect(x, y, w, h);

        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(new Color(0, 255, 120));
        g.drawString("POWER-UPS", x + 10, y + 18);

        for (int i = 0; i < icons.length; i++) {
            int row = y + 26 + i * rowH;
            g.drawImage(icons[i], x + 10, row, this);
            g.setColor(Color.white);
            g.drawString(labels[i], x + 40, row + 15);
        }
    }

    // HUD colour for a gun rung, matched to its pickup flower.
    private Color gunTierColor(GunTier tier) {
        switch (tier) {
            case BOLT:
                return new Color(255, 140, 30);   // orange 4X
            case CHARGED:
                return new Color(200, 110, 255);  // purple 6X
            case EIGHT:
                return new Color(90, 220, 110);   // green 8X
            case TEN:
                return new Color(80, 220, 220);   // cyan 10X
            default:
                return new Color(255, 200, 40);   // blue 2X keeps the old gold
        }
    }

private void drawDashboard(Graphics g) {

		int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
		int top = playfieldBottom();

		// Dark space panel with a green scanline on top
		g.setColor(new Color(4, 12, 24));
		g.fillRect(0, top, panelW, DASHBOARD_HEIGHT);
		g.setColor(new Color(0, 255, 120));
		g.fillRect(0, top, panelW, 2);

		Font labelFont = new Font("Monospaced", Font.BOLD, 12);
		Font valueFont = new Font("Monospaced", Font.BOLD, 16);
		int labelY = top + 18;
		int valueY = top + 42;

		// --- HUD LABELS ---
		g.setFont(labelFont);
		g.setColor(new Color(0, 255, 120));
		g.drawString("LIVES", 14, labelY);
		g.drawString("HULL", 95, labelY);
		g.drawString("SPEED", 185, labelY);
		g.drawString("RELOAD", 260, labelY); // Renamed from BULLET for clarity
		g.drawString("SCORE", 370, labelY);

		// Gun-ladder badge next to the RELOAD label, coloured to match the
		// flower that granted the rung so the HUD and the pickup agree.
		if (player.hasBulletFlower()) {
			g.setColor(gunTierColor(player.getGunTier()));
			g.drawString("x" + player.getVolleyShots() + " D" + player.getShotDamage(), 315, labelY);
			g.setColor(new Color(0, 255, 120));
		}

		// Stage number, sitting above the timer in the right corner
		String stageLabel = "STAGE " + stage.number;
		FontMetrics lfm = g.getFontMetrics(labelFont);
		g.drawString(stageLabel, panelW - lfm.stringWidth(stageLabel) - 14, labelY);

		// --- LIVES ---
		if (lifeIcon != null) {
			int iw = lifeIcon.getWidth(null);
			for (int i = 0; i < lives; i++) {
				g.drawImage(lifeIcon, 14 + i * (iw + 8), labelY + 6, this);
			}
		}

		// --- HULL PIPS ---
		for (int i = 0; i < player.getMaxHull(); i++) {
			int hx = 95 + i * 16;
			int hy = valueY - 12;
			g.setColor(i < player.getHull() ? new Color(0, 255, 120) : new Color(20, 35, 50));
			g.fillRect(hx, hy, 12, 12);
			g.setColor(new Color(4, 12, 24));
			g.drawRect(hx, hy, 12, 12);
		}

		// --- SPEED & SCORE ---
		g.setFont(valueFont);
		g.setColor(Color.white);
		g.drawString(String.valueOf(player.getSpeed()), 185, valueY);
		g.drawString(String.valueOf(deaths), 370, valueY);

		// --- ENHANCED RELOAD BAR ---
		int rbW = 95;
		int rbH = 10; // Taller bar for better presentation
		int rbX = 260;
		int rbY = valueY - 10;

		// Background track & Border
		g.setColor(new Color(15, 25, 40));
		g.fillRect(rbX, rbY, rbW, rbH);
		g.setColor(new Color(0, 180, 80));
		g.drawRect(rbX, rbY, rbW, rbH);

		// Progress Calculation
		int cdTotal = Math.max(1, player.getShotCooldown());
		int cdLeft = player.getCooldownLeft();
		int readyWidth = (int) ((rbW - 2) * ((double) (cdTotal - cdLeft) / cdTotal));
		readyWidth = Math.max(0, Math.min(rbW - 2, readyWidth));

		// Fill Color: Green when ready to shoot, orange/yellow while recharging
		if (player.canShoot()) {
			g.setColor(new Color(0, 255, 120));
		} else {
			g.setColor(new Color(255, 170, 0));
		}
		g.fillRect(rbX + 1, rbY + 1, readyWidth, rbH - 1);

		// --- FLASHING BOSS ALERT ---
		boolean bossAlive = false;
		for (Enemy enemy : enemies) {
			if (enemy instanceof Boss && enemy.isVisible() && !enemy.isDying()) {
				bossAlive = true;
				break;
			}
		}
		if (bossAlive && (frame / 20) % 2 == 0) {
			g.setColor(Color.red);
			g.drawString("!! BOSS !!", 470, valueY);
		}

		// --- GAME TIMER ---
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
        player.setY(terrain.clampToGap(player.getX(), player.getWidth(),
                terrainScroll, player.getY(),
                player.getHeight(), playfieldBottom()));
        invincibleFrames = 120; // 2s of blinking safety
    }

    // Ends the run and shows the end screen. sfx is the sting that plays over
    // it — clearing a stage is a win and must not share the death music.
    private void endGame(String msg, String sfx) {
        try {
            audioPlayer = new AudioPlayer(sfx);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
        inGame = false;
        timer.stop();
        message = msg;
        gameOverAt = System.currentTimeMillis();
    }

    // A heart picked up. Lives are hard-capped at PLAYER_LIVES, so this tops
    // the player back up rather than stacking a reserve.
    private void gainLives(int count) {
        if (count > 0) {
            lives = Math.min(PLAYER_LIVES, lives + count);
        }
    }

    // One life gone: either the run is over or the ship comes back at the
    // start position with a full hull and a blink of safety.
    private void loseLife() {
        lives--;
        // Bring the next heart forward. Its schedule was set while the player
        // was healthy and the drop was worth nothing to them; now it is worth
        // something, so it should turn up soon rather than a minute later.
        nextHeartFrame = Math.min(nextHeartFrame, frame + HEART_RETRY_SECONDS * 60);
        if (lives <= 0) {
            player.die();
            endGame("Game Over", GameOver_sfx);
        } else {
            respawnPlayer();
        }
        try {
            audioPlayer = new AudioPlayer(Explosion_sfx);
            audioPlayer.playOnce();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    // Enemy fire — a plane, its bullets, or a boss bullet — costs a hull point
    // instead of a whole life. Emptying the hull is what spends the life.
    // Ramming an alien, a boss or a wall still costs a life outright.
    private void absorbHullHit() {
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
            return ((EnemyPlane) enemy).getScore(); // heavier models pay more
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
     *
     * The stage narrows that further: stage 1 is front-only, so the rear-only
     * plane 2 never appears there and plane 3 always comes head-on. Stage 2 is
     * where the player first has to worry about their back.
     */
    private void spawnPlane() {

        EnemyPlane.Type type = EnemyPlane.Type.pick(randomizer,
                stage.planesFromBehind, planeTierCap(), pressure());
        EnemyPlane.Heading heading = type.pickHeading(randomizer, stage.planesFromBehind);

        int py = 10 + randomizer.nextInt(Math.max(1, playfieldBottom() - type.size - 20));
        int px = heading == EnemyPlane.Heading.LEFTWARD
                ? BOARD_WIDTH + randomizer.nextInt(80)
                : -type.size - randomizer.nextInt(80);

        EnemyPlane plane = new EnemyPlane(type, heading, px, py);
        clampIntoGap(plane);
        enemies.add(plane);

        // Meeting a tougher model is what puts the matching gun upgrade into
        // the drop pool.
        seenPlaneTier = Math.max(seenPlaneTier, type.tier);
    }

    /**
     * How far up the plane escalation this stage has got right now — the
     * highest tier whose unlock frame has passed. A stage never fields a tier
     * its schedule has no entry for.
     */
    private int planeTierCap() {
        int cap = 0;
        for (int tier = 1; tier < stage.planeTierFrames.length; tier++) {
            if (frame >= stage.planeTierFrames[tier]) {
                cap = tier;
            }
        }
        return cap;
    }

    /**
     * The gun rung due to drop right now, or null if nothing should appear
     * yet. Runs on its own clock rather than the shared random pool: the
     * single rung above the one the player holds is scheduled for
     * {@code stage.gunUpgradeFirstSeconds} in, then one more every
     * {@code stage.gunUpgradeIntervalSeconds} — see Stage and GunTier.
     *
     * The plane-tier requirement still applies underneath as a floor, so the
     * schedule can guarantee a rung by a given time but never hands one out
     * before its enemy has actually been met. That floor is capped at the
     * toughest tier this stage's roster ever fields
     * ({@code stage.planeTierFrames.length - 1}), so a stage with no elite
     * (stage 1) isn't blocked forever waiting for one to unlock 6X/8X.
     * {@code stage.gunTierCeiling} is the separate cap on how far a stage's
     * ladder is allowed to reach at all — stage 1 stops offering at 8X even
     * though the clock and floor alone would let it reach further.
     */
    private GunTier dueGunTier() {
        GunTier next = player.getGunTier().next();
        if (next == null || next.ordinal() > stage.gunTierCeiling.ordinal()
                || frame < nextGunDropFrame) {
            return null;
        }

        int rung = next.ordinal() - stage.startingGun.ordinal();
        int scheduledFrame = (stage.gunUpgradeFirstSeconds
                + (rung - 1) * stage.gunUpgradeIntervalSeconds) * 60;
        int requiredTier = Math.min(next.unlockPlaneTier, stage.planeTierFrames.length - 1);

        return frame >= scheduledFrame && seenPlaneTier >= requiredTier ? next : null;
    }

    // Killing this stage's boss is what clears it — there is no timeout win,
    // so the run stays in the arena until the fight is finished.
    private void clearStage() {
        awaitingNextStage = !stage.last;
        endGame(stage.last
                ? "All Stages Clear!"
                : "Stage " + stage.number + " Clear!",
                Victory_sfx);
    }

    private void update() {

        // The boss is down and its explosion is playing — hold the run open
        // for a beat, then show the clear screen.
        if (clearDelayFrames >= 0) {
            if (clearDelayFrames == 0) {
                clearStage();
                return;
            }
            clearDelayFrames--;
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
                    clampPowerUpIntoGap(speedUp);
                    powerups.add(speedUp);
                    break;
                case "PowerUp-Shield":
                    PowerUp shield = new ShieldUp(sd.x, sd.y);
                    clampPowerUpIntoGap(shield);
                    powerups.add(shield);
                    break;
                default:
                    // Any rung of the gun ladder, named by its spawn key.
                    GunTier scripted = GunTier.forSpawnKey(sd.type);
                    if (scripted != null) {
                        PowerUp bullet = new BulletUp(sd.x, sd.y, scripted);
                        clampPowerUpIntoGap(bullet);
                        powerups.add(bullet);
                    } else {
                        System.out.println("Unknown enemy type: " + sd.type);
                    }
                    break;
            }
        }

        // Once the boss is on the field the waves stop and the fight is a
        // duel. It runs until the boss dies, and full-pressure waves on top of
        // that — in a cave that has stopped scrolling — is not a fight anyone
        // finishes. Power-up drops keep coming; a shield mid-fight is welcome.
        boolean bossFight = scrollFrozen();

        // Random aliens from the front (right edge) at random heights. Both the
        // size of a wave and how often waves arrive follow the run-long
        // pressure ramp: one lonely alien every few seconds early on, packs of
        // two or three barely a second apart by the end of stage 2.
        if (!bossFight && frame >= nextAlienSpawnFrame) {
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
        if (!bossFight && frame >= nextPlaneSpawnFrame) {
            spawnPlane();

            int gap = (int) Math.round(PLANE_GAP_START
                    - (PLANE_GAP_START - PLANE_GAP_END) * pressure());
            nextPlaneSpawnFrame = frame + gap
                    + randomizer.nextInt(PLANE_GAP_JITTER);
        }

        // A power-up drop every POWERUP_MIN..MAX seconds — rare enough to feel
        // like a find, regular enough that there is always one on the way.
        // The gun flower has its own clock (dueGunTier, right below) instead
        // of competing for this slot, so speed and shield are the only two
        // in this pool.
        if (frame >= nextPowerupFrame) {
            int py = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
            // Shield takes two of the three slots — it is the one drop worth
            // seeing more often, so it lands about twice as often as speed.
            PowerUp drop = randomizer.nextInt(3) == 0
                    ? new SpeedUp(BOARD_WIDTH, py)
                    : new ShieldUp(BOARD_WIDTH, py);
            clampPowerUpIntoGap(drop);
            powerups.add(drop);
            nextPowerupFrame = frame + (POWERUP_MIN_SECONDS
                    + randomizer.nextInt(POWERUP_MAX_SECONDS - POWERUP_MIN_SECONDS + 1)) * 60;
        }

        // The gun ladder, on its own scheduled clock rather than the random
        // pool above — see dueGunTier() for the full rule (fixed time, floor
        // gated by the plane it answers, capped by the stage's ceiling).
        GunTier dueGun = dueGunTier();
        if (dueGun != null) {
            int gy = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
            PowerUp drop = new BulletUp(BOARD_WIDTH, gy, dueGun);
            clampPowerUpIntoGap(drop);
            powerups.add(drop);
            // Gate the next attempt rather than spawning again next frame;
            // if this one is missed, retry sooner than the next rung's own
            // schedule so a drifted-off pickup doesn't stall the ladder.
            nextGunDropFrame = frame + GUN_DROP_RETRY_SECONDS * 60;
        }

        // The extra-life heart, on its own slower clock so it doesn't crowd
        // the other drops out. Lives are hard-capped, so at full health the
        // heart is usually held back and reconsidered shortly — the odd one
        // still comes through, and a life can always be lost while it drifts.
        if (frame >= nextHeartFrame) {
            if (lives < PLAYER_LIVES
                    || randomizer.nextInt(HEART_FULL_LIVES_CHANCE) == 0) {
                int hy = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
                PowerUp heart = new HeartUp(BOARD_WIDTH, hy);
                clampPowerUpIntoGap(heart);
                powerups.add(heart);
                nextHeartFrame = frame + (HEART_MIN_SECONDS
                        + randomizer.nextInt(HEART_MAX_SECONDS - HEART_MIN_SECONDS + 1)) * 60;
            } else {
                nextHeartFrame = frame + HEART_RETRY_SECONDS * 60;
            }
        }

        // The stage's one boss, timed to land in the map's wide arena. From
        // here the cave stops scrolling and the stage ends when the boss dies.
        if (!bossSpawned && frame >= stage.bossSecond * 60) {
            enemies.add(new Boss(BOARD_WIDTH, playfieldBottom() / 2 - 40,
                    playfieldBottom(), stage));
            bossSpawned = true;
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
                    gainLives(powerup.livesGranted()); // hearts; 0 for everything else
                    powerup.upgrade(player);
                }
            }
        }
        powerups.removeIf(p -> !p.isVisible());

        // Enemies. Nothing flies into the rock any more: the boss hovers, so it
        // is given the corridor as patrol limits and turns around at the wall,
        // and everything else steers back into the corridor after it moves.
        for (Enemy enemy : enemies) {
            if (!enemy.isVisible()) {
                continue;
            }
            if (enemy instanceof Boss) {
                Boss boss = (Boss) enemy;
                int[] gap = terrain.gapBounds(boss.getX(), boss.getWidth(),
                        terrainScroll, playfieldBottom());
                // Leave room under the boss for its HP bar, as before.
                boss.setPatrolBounds(gap[0] + 8, gap[1] - boss.getHeight() - 20);
                enemy.act(direction);
            } else {
                enemy.act(direction);
                dodgeTerrain(enemy); // fly around the cave, not into it
            }
        }

        // Planes shoot on the same interval as the player's starting gun,
        // aimed at wherever the ship is at the moment the trigger is pulled.
        if (player.isVisible()) {
            int aimX = player.getX() + player.getWidth() / 2;
            int aimY = player.getY() + player.getHeight() / 2;

            for (Enemy enemy : enemies) {
                if (!enemy.isVisible() || enemy.isDying()) {
                    continue;
                }
                if (enemy instanceof EnemyPlane) {
                    EnemyPlane plane = (EnemyPlane) enemy;
                    if (plane.readyToFire()) {
                        enemyShots.add(new EnemyShot(plane.getMuzzleX(), plane.getMuzzleY(),
                                aimX, aimY, plane.getHeading().step));
                        plane.noteFired();
                    }
                } else if (enemy instanceof Boss) {
                    // One volley, aimed at the ship: a single fast bullet in
                    // stage 1, a fan of them in stage 2. Every bullet of a
                    // volley shares the aim point taken at the trigger pull.
                    Boss boss = (Boss) enemy;
                    if (boss.readyToFire()) {
                        for (double offset : boss.volleyAngles()) {
                            enemyShots.add(new BossShot(boss.getMuzzleX(), boss.getMuzzleY(),
                                    aimX, aimY, offset));
                        }
                        boss.noteFired();
                    }
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

        // Safety net only. Enemies steer around the cave (dodgeTerrain above),
        // so in a legal map none of them should ever die here — but a corridor
        // genuinely shorter than a sprite leaves it nowhere to go, and it
        // should go up rather than sit embedded in rock. No score either way.
        for (Enemy enemy : enemies) {
            if (!enemy.isVisible() || enemy.isDying() || enemy instanceof Boss) {
                continue;
            }
            int[] gap = terrain.gapBounds(enemy.getX(),
                    enemy.getImage().getWidth(null), terrainScroll, playfieldBottom());
            boolean trapped = gap[1] - gap[0] < enemy.getImage().getHeight(null);
            if (trapped
                    && terrain.collides(spriteRect(enemy, 6), terrainScroll, playfieldBottom())) {
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
                        if (enemy instanceof Boss) {
                            // A boss is too big to ram down. The shield takes
                            // the hit and breaks — otherwise a single pickup
                            // would skip the whole fight.
                            player.breakShield();
                            explodeAt(player.getX() + player.getWidth() / 2,
                                    player.getY() + player.getHeight() / 2);
                            invincibleFrames = HIT_INVINCIBLE_FRAMES;
                            break;
                        }
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
                        absorbHullHit();
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
                && terrain.collides(spriteRect(player, 8), terrainScroll, playfieldBottom())) {
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
                    || terrain.collides(spriteRect(bullet, 1), terrainScroll, playfieldBottom())) {
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
                absorbHullHit();
            }
        }
        enemyShots.removeAll(enemyShotsToRemove);

        // shot
        boolean bossKilled = false;
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {

            if (shot.isVisible()) {

                shot.act(); // animates the bolt/charged bullets; movement is below

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
                                bossKilled = true;
                            } else {
                                // A spark at the impact — with BOSS_HP this
                                // high the player needs to see hits landing.
                                explodeAt(shot.getX(), shot.getY());
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

                // Shots travel along their own heading at the player's bullet
                // speed; remove once off the right edge, or — for the angled
                // bullets in a fan — off the top or bottom of the playfield.
                if (shot.isVisible()) {
                    shot.advance(player.getShotSpeed());

                    int shotH = shot.getImage().getHeight(null);
                    if (shot.getX() > BOARD_WIDTH
                            || shot.getY() + shotH < 0
                            || shot.getY() > playfieldBottom()) {
                        shot.die();
                        shotsToRemove.add(shot);
                    } else if (terrain.collides(spriteRect(shot, 2), terrainScroll,
                            playfieldBottom())) {
                        // Bullets splash against the cave walls.
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }
            }
        }
        shots.removeAll(shotsToRemove);

        // Drop fully dead enemies so the list stays small over a long run.
        enemies.removeIf(e -> !e.isVisible());

        // The boss was this stage's win condition. Start the beat rather than
        // clearing straight away, so its explosion gets a second on screen.
        if (bossKilled) {
            clearDelayFrames = STAGE_CLEAR_DELAY_FRAMES;
        }

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
        // The world holds still for the boss fight: it runs until the boss
        // dies rather than for a fixed time, so letting the cave keep scrolling
        // would eventually drag the player out of the arena and into rock.
        if (!scrollFrozen()) {
            terrainScroll++;
        }
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
                // The concurrent cap has to leave room for several whole
                // volleys, or the top rungs could never get their full fan
                // on screen at once.
                GunTier tier = player.getGunTier();
                int volley = player.getVolleyShots();
                int maxShots = Math.max(8, volley * 3);
                if (shots.size() + volley <= maxShots && player.canShoot()) {
                    // Fire from the tip of the ship: right edge, vertically
                    // centered. The whole volley leaves at the same moment,
                    // fanned out around straight ahead.
                    int tipX = player.getX() + player.getWidth();
                    int tipY = player.getY() + player.getHeight() / 2;
                    for (int i = 0; i < volley; i++) {
                        shots.add(new Shot(tipX, tipY, tier, tier.angleFor(i)));
                    }
                    player.startShotCooldown();
                }
            }

        }
    }
}
