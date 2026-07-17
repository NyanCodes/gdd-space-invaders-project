package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.ShieldUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
    private Player player;
    private Image background;
    // private Shot shot;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0; // score: aliens +1, bosses +5

    private int lives = PLAYER_LIVES;
    private int invincibleFrames = 0; // blink window after a respawn
    private int nextAlienSpawnFrame = 120;
    private int nextPowerupFrame = 12 * 60;
    private int bossSpawnIndex = 0;
    private Image lifeIcon; // small vertical (nose-up) ship for the HUD
    private Image tipIconSpeed; // small power-up icons for the start-of-game tip box
    private Image tipIconShield;

    private boolean inGame = true;
    private String message = "Game Over";
    // When the end screen appeared. A SPACE still held down from shooting would
    // otherwise exit the game the instant the player dies.
    private long gameOverAt = 0;
    private static final long GAMEOVER_INPUT_LOCKOUT_MS = 700;

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

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
        this.game = game;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
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
        spawnMap.put(50, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 200));
        spawnMap.put(150, new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, 320));
        spawnMap.put(200, new SpawnDetails("Alien1", BOARD_WIDTH, 200));
        spawnMap.put(300, new SpawnDetails("Alien1", BOARD_WIDTH, 300));
        spawnMap.put(450, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 420));
        spawnMap.put(600, new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, 160));

        spawnMap.put(400, new SpawnDetails("Alien1", BOARD_WIDTH, 120));
        spawnMap.put(401, new SpawnDetails("Alien1", BOARD_WIDTH, 240));
        spawnMap.put(402, new SpawnDetails("Alien1", BOARD_WIDTH, 360));
        spawnMap.put(403, new SpawnDetails("Alien1", BOARD_WIDTH, 480));

        spawnMap.put(500, new SpawnDetails("Alien1", BOARD_WIDTH, 100));
        spawnMap.put(501, new SpawnDetails("Alien1", BOARD_WIDTH, 250));
        spawnMap.put(502, new SpawnDetails("Alien1", BOARD_WIDTH, 400));
        spawnMap.put(503, new SpawnDetails("Alien1", BOARD_WIDTH, 550));
    }

    private void initBoard() {

    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {
        timer.stop();
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

        background = new ImageIcon(IMG_BACKGROUND).getImage();

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
        tipIconSpeed = new ImageIcon(IMG_POWERUP_SPEEDUP).getImage()
                .getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
        tipIconShield = new ImageIcon(IMG_POWERUP_SHIELD).getImage()
                .getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
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

    // Top edge of the bottom dashboard; gameplay stays above this line.
    // Uses the real panel height so the bar is never cut off by window insets.
    private int playfieldBottom() {
        int h = getHeight();
        if (h <= 0) {
            h = BOARD_HEIGHT;
        }
        return h - DASHBOARD_HEIGHT;
    }

    private void drawBackground(Graphics g) {
        if (background == null) {
            return;
        }
        // Scroll the background image right -> left, wrapping seamlessly by
        // drawing a second copy immediately after the first.
        int offset = frame % BOARD_WIDTH;
        g.drawImage(background, -offset, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        g.drawImage(background, BOARD_WIDTH - offset, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
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

            drawBackground(g);  // Draw the scrolling background image first
            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
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
        int w = 140;
        int h = 88;
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
        g.drawImage(tipIconSpeed, x + 10, row1, this);
        g.drawImage(tipIconShield, x + 10, row2, this);

        g.setColor(Color.white);
        g.drawString("SPEED UP", x + 40, row1 + 15);
        g.drawString("SHIELD", x + 40, row2 + 15);
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
        g.drawString("SPEED", 140, labelY);
        g.drawString("BULLET", 250, labelY);
        g.drawString("SCORE", 370, labelY);

        // Remaining lives as small vertical ship icons.
        if (lifeIcon != null) {
            int iw = lifeIcon.getWidth(null);
            for (int i = 0; i < lives; i++) {
                g.drawImage(lifeIcon, 14 + i * (iw + 10), labelY + 6, this);
            }
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

        drawExitButton(g);
    }

    // EXIT on the end screen. The timer is stopped by now, so this is drawn
    // once and can't blink like the title menu — it stays highlighted instead.
    private void drawExitButton(Graphics g) {

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
        String text = "> EXIT <";
        g.setFont(label);
        g.setColor(Color.white);
        g.drawString(text, bx + (bw - labelMetrics.stringWidth(text)) / 2, by + 30);

        var hintFont = new Font("Helvetica", Font.BOLD, 12);
        var hintMetrics = this.getFontMetrics(hintFont);
        String hint = "ENTER to exit";
        g.setFont(hintFont);
        g.setColor(Color.gray);
        g.drawString(hint, (BOARD_WIDTH - hintMetrics.stringWidth(hint)) / 2, by + bh + 22);
    }

    private void endGame(String msg) {
        inGame = false;
        timer.stop();
        message = msg;
        gameOverAt = System.currentTimeMillis();
    }

    private void update() {

        int elapsedSeconds = frame / 60;

        // Stage clear once the full run is survived (5 minutes for now).
        if (frame >= GAME_DURATION_SECONDS * 60) {
            endGame("Stage Clear!");
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
                    powerups.add(speedUp);
                    break;
                case "PowerUp-Shield":
                    powerups.add(new ShieldUp(sd.x, sd.y));
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }

        // Random aliens from the front (right edge), 1-2 at a time at random
        // heights; the gap between waves shrinks every 30s to ramp difficulty.
        if (frame >= nextAlienSpawnFrame) {
            int count = 1 + randomizer.nextInt(2);
            for (int i = 0; i < count; i++) {
                int ay = 10 + randomizer.nextInt(Math.max(1, playfieldBottom() - 60));
                enemies.add(new Alien1(BOARD_WIDTH + randomizer.nextInt(80), ay));
            }
            int gap = Math.max(30, 100 - (elapsedSeconds / 30) * 10);
            nextAlienSpawnFrame = frame + gap + randomizer.nextInt(50);
        }

        // A power-up drop every 12-25 seconds, randomly speed or shield.
        if (frame >= nextPowerupFrame) {
            int py = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
            if (randomizer.nextBoolean()) {
                powerups.add(new SpeedUp(BOARD_WIDTH, py));
            } else {
                powerups.add(new ShieldUp(BOARD_WIDTH, py));
            }
            nextPowerupFrame = frame + (12 + randomizer.nextInt(14)) * 60;
        }

        // Boss schedule — extend by adding entries to BOSS_SPAWN_SECONDS.
        if (bossSpawnIndex < BOSS_SPAWN_SECONDS.length
                && frame == BOSS_SPAWN_SECONDS[bossSpawnIndex] * 60) {
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

        // Player <-> enemy collision: with the golden shield up, ramming an
        // enemy kills it (and scores it); otherwise getting hit costs a life.
        if (player.isVisible()) {
            for (Enemy enemy : enemies) {
                if (enemy.isVisible() && !enemy.isDying() && player.collidesWith(enemy)) {
                    if (player.isShieldActive()) {
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths += (enemy instanceof Boss) ? 5 : 1;
                        continue;
                    }
                    if (invincibleFrames > 0) {
                        continue; // still blinking after a respawn
                    }
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    if (!(enemy instanceof Boss)) {
                        // The alien is destroyed in the crash (no score for it).
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    }
                    lives--;
                    if (lives <= 0) {
                        player.die();
                        endGame("Game Over");
                    } else {
                        player.respawn();
                        invincibleFrames = 120; // 2s of blinking safety
                    }
                    break;
                }
            }
        }

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
                            boss.hit();
                            if (boss.isDead()) {
                                boss.setDying(true);
                                explosions.add(new Explosion(
                                        boss.getX() + boss.getWidth() / 2 - 18,
                                        boss.getY() + boss.getHeight() / 2 - 18));
                                deaths += 5; // bosses are worth 5
                            }
                        } else {
                            enemy.setDying(true);
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                            deaths++;
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
                    }
                }
            }
        }
        shots.removeAll(shotsToRemove);

        // Drop fully dead enemies so the list stays small over a 5-minute run.
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
                    Shot shot = new Shot(tipX, tipY);
                    shots.add(shot);
                    player.startShotCooldown();
                }
            }

        }
    }
}
