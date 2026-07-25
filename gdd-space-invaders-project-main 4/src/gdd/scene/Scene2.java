package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.ShieldUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Alien3;
import gdd.legacy.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Obstacle;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.BasicStroke;
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

/**
 * Stage 2: the follow-up to Scene1. Same horizontal side-scroller shell,
 * but with three distinct enemy movement patterns instead of one, a
 * tumbling/splittable asteroid field to dodge or shoot through, and a
 * temporary shield pickup to help survive it. Paces faster and the boss
 * has double HP.
 */
public class Scene2 extends JPanel {

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Obstacle> obstacles;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;
    private Image background;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    private int direction = -1;
    private int deaths = 0;

    private int lives = PLAYER_LIVES;
    private int invincibleFrames = 0; // blink window after a respawn
    private int shieldFrames = 0;     // active ShieldUp countdown
    private int nextAlienSpawnFrame = 90;
    private int nextObstacleSpawnFrame = 240;
    private int nextPowerupFrame = 18 * 60;
    private int bossSpawnIndex = 0;
    private int bossesDefeated = 0;
    private Image lifeIcon;

    private boolean inGame = true;
    private String message = "Game Over";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

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

    public Scene2(Game game) {
        this.game = game;
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
        // Opening burst: introduces obstacles and both new enemy types
        // early so the player learns the patterns before the pace ramps up.
        spawnMap.put(60, new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, 200));
        spawnMap.put(150, new SpawnDetails("Obstacle-Large", BOARD_WIDTH, 150));
        spawnMap.put(220, new SpawnDetails("Alien2", BOARD_WIDTH, 200));
        spawnMap.put(280, new SpawnDetails("Alien2", BOARD_WIDTH, 400));
        spawnMap.put(340, new SpawnDetails("Alien3", BOARD_WIDTH, 300));
        spawnMap.put(420, new SpawnDetails("Obstacle-Large", BOARD_WIDTH, 450));
        spawnMap.put(480, new SpawnDetails("Alien1", BOARD_WIDTH, 120));
        spawnMap.put(481, new SpawnDetails("Alien3", BOARD_WIDTH, 380));
        spawnMap.put(482, new SpawnDetails("Alien2", BOARD_WIDTH, 550));
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
        obstacles = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();

        background = new ImageIcon(IMG_BACKGROUND).getImage();
        player = new Player();
        lifeIcon = createLifeIcon();

        // Fresh run state, in case this scene instance is replayed.
        frame = 0;
        deaths = 0;
        lives = PLAYER_LIVES;
        invincibleFrames = 0;
        shieldFrames = 0;
        nextAlienSpawnFrame = 90;
        nextObstacleSpawnFrame = 240;
        nextPowerupFrame = 18 * 60;
        bossSpawnIndex = 0;
        bossesDefeated = 0;
        direction = -1;
        inGame = true;
        message = "Game Over";
    }

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
        int offset = frame % BOARD_WIDTH;
        g.drawImage(background, -offset, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        g.drawImage(background, BOARD_WIDTH - offset, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // Faint red tint over the field to signal "hazard zone" vs Stage 1.
        g.setColor(new Color(80, 0, 0, 26));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawMap(Graphics g) {
        int scrollOffset = (frame) % BLOCKWIDTH;
        int baseCol = (frame) / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2;

        for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
            int mapCol = (baseCol + screenCol) % MAP[0].length;
            int x = (screenCol * BLOCKWIDTH) - scrollOffset;

            if (x > BOARD_WIDTH || x < -BLOCKWIDTH) {
                continue;
            }

            for (int row = 0; row < MAP.length; row++) {
                if (MAP[row][mapCol] == 1) {
                    int y = row * BLOCKHEIGHT;
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }

    private void drawObstacles(Graphics g) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isVisible()) {
                g.drawImage(obstacle.getImage(), obstacle.getX(), obstacle.getY(), this);
            }
        }
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
        boolean blinkHidden = invincibleFrames > 0 && (invincibleFrames / 4) % 2 == 0;

        if (player.isVisible() && !blinkHidden) {
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);

            // Glowing ring while the shield power-up is active.
            if (shieldFrames > 0) {
                Graphics2D g2 = (Graphics2D) g;
                int pulse = (frame / 3) % 6;
                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(new Color(80, 190, 255, 160 - pulse * 10));
                int pad = 6 + pulse;
                g2.drawOval(player.getX() - pad, player.getY() - pad,
                        player.getWidth() + pad * 2, player.getHeight() + pad * 2);
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
            drawBackground(g);
            drawExplosions(g);
            drawObstacles(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBossHpBars(g);
            drawDashboard(g);
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
                int by = boss.getY() + boss.getHeight() + 6;

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

    private void drawDashboard(Graphics g) {
        int panelW = getWidth() > 0 ? getWidth() : BOARD_WIDTH;
        int top = playfieldBottom();

        g.setColor(new Color(24, 6, 4));
        g.fillRect(0, top, panelW, DASHBOARD_HEIGHT);
        g.setColor(new Color(255, 110, 0));
        g.fillRect(0, top, panelW, 2);

        Font labelFont = new Font("Monospaced", Font.BOLD, 12);
        Font valueFont = new Font("Monospaced", Font.BOLD, 16);
        int labelY = top + 20;
        int valueY = top + 44;

        g.setFont(labelFont);
        g.setColor(new Color(255, 140, 0));
        g.drawString("LIVES", 14, labelY);
        g.drawString("SPEED", 140, labelY);
        g.drawString("BULLET", 250, labelY);
        g.drawString("SCORE", 370, labelY);

        if (lifeIcon != null) {
            int iw = lifeIcon.getWidth(null);
            for (int i = 0; i < lives; i++) {
                g.drawImage(lifeIcon, 14 + i * (iw + 10), labelY + 6, this);
            }
        }

        g.setFont(valueFont);
        g.setColor(Color.white);
        g.drawString(String.valueOf(player.getSpeed()), 140, valueY);
        g.drawString(String.valueOf(player.getShotSpeed()), 250, valueY);
        g.drawString(String.valueOf(deaths), 370, valueY);

        if (shieldFrames > 0 && (frame / 10) % 2 == 0) {
            g.setColor(new Color(80, 190, 255));
            g.drawString("SHIELD", 460, valueY);
        }

        boolean bossAlive = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss && enemy.isVisible() && !enemy.isDying()) {
                bossAlive = true;
                break;
            }
        }
        if (bossAlive && (frame / 20) % 2 == 0) {
            g.setColor(Color.red);
            g.drawString("!! BOSS !!", 460, valueY - 18);
        }

        int seconds = frame / 60;
        String time = String.format("TIME %02d:%02d", seconds / 60, seconds % 60);
        FontMetrics fm = g.getFontMetrics(valueFont);
        g.setColor(new Color(255, 140, 0));
        g.drawString(time, panelW - fm.stringWidth(time) - 14, valueY);

        g.setFont(labelFont);
        g.setColor(new Color(255, 140, 0));
        g.drawString("STAGE 2", panelW - fm.stringWidth(time) - 14, labelY);
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(48, 16, 0));
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);

        var tiny = new Font("Helvetica", Font.PLAIN, 12);
        var tinyMetrics = this.getFontMetrics(tiny);
        String hint = "Press SPACE to continue";
        g.setFont(tiny);
        g.setColor(Color.gray);
        g.drawString(hint, (BOARD_WIDTH - tinyMetrics.stringWidth(hint)) / 2, BOARD_WIDTH / 2 + 24);
    }

    // Weighted toward tougher enemies as the stage goes on.
    private Enemy randomEnemyFor(int elapsedSeconds, int x, int y) {
        int roll = randomizer.nextInt(100);
        int hunterChance = Math.min(45, 15 + elapsedSeconds / 4);
        int weaverChance = Math.min(40, 20 + elapsedSeconds / 6);

        if (roll < hunterChance) {
            return new Alien3(x, y);
        } else if (roll < hunterChance + weaverChance) {
            return new Alien2(x, y);
        }
        return new Alien1(x, y);
    }

    private void update() {

        int elapsedSeconds = frame / 60;

        if (bossSpawnIndex >= STAGE2_BOSS_SPAWN_SECONDS.length
                && bossesDefeated >= STAGE2_BOSS_SPAWN_SECONDS.length) {
            inGame = false;
            timer.stop();
            message = "Boss Defeated! Stage 2 Clear!";
            return;
        }

        if (frame >= STAGE2_DURATION_SECONDS * 60) {
            inGame = false;
            timer.stop();
            message = "Stage 2 Clear!";
            return;
        }

        // Scripted spawns
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            switch (sd.type) {
                case "Alien1":
                    enemies.add(new Alien1(sd.x, sd.y));
                    break;
                case "Alien2":
                    enemies.add(new Alien2(sd.x, sd.y));
                    break;
                case "Alien3":
                    enemies.add(new Alien3(sd.x, sd.y));
                    break;
                case "Obstacle-Large":
                    obstacles.add(new Obstacle(Obstacle.Size.LARGE, sd.x, sd.y));
                    break;
                case "PowerUp-SpeedUp":
                    powerups.add(new SpeedUp(sd.x, sd.y));
                    break;
                case "PowerUp-Shield":
                    powerups.add(new ShieldUp(sd.x, sd.y));
                    break;
                default:
                    System.out.println("Unknown spawn type: " + sd.type);
                    break;
            }
        }

        // Random enemy waves; gap shrinks and difficulty mix shifts harder over time.
        if (frame >= nextAlienSpawnFrame) {
            int count = 1 + randomizer.nextInt(2);
            for (int i = 0; i < count; i++) {
                int ay = 10 + randomizer.nextInt(Math.max(1, playfieldBottom() - 60));
                enemies.add(randomEnemyFor(elapsedSeconds, BOARD_WIDTH + randomizer.nextInt(80), ay));
            }
            int gap = Math.max(22, 85 - (elapsedSeconds / 25) * 10);
            nextAlienSpawnFrame = frame + gap + randomizer.nextInt(35);
        }

        // Obstacle field: large asteroids drift in on their own schedule.
        if (frame >= nextObstacleSpawnFrame) {
            int oy = 20 + randomizer.nextInt(Math.max(1, playfieldBottom() - 80));
            obstacles.add(new Obstacle(Obstacle.Size.LARGE, BOARD_WIDTH + randomizer.nextInt(60), oy));
            int gap = Math.max(150, 320 - elapsedSeconds);
            nextObstacleSpawnFrame = frame + gap + randomizer.nextInt(100);
        }

        // Power-up drop: SpeedUp or the new ShieldUp, roughly evenly.
        if (frame >= nextPowerupFrame) {
            int py = 60 + randomizer.nextInt(Math.max(1, playfieldBottom() - 140));
            if (randomizer.nextBoolean()) {
                powerups.add(new SpeedUp(BOARD_WIDTH, py));
            } else {
                powerups.add(new ShieldUp(BOARD_WIDTH, py));
            }
            nextPowerupFrame = frame + (22 + randomizer.nextInt(18)) * 60;
        }

        // Boss schedule — tougher HP than Stage 1.
        if (bossSpawnIndex < STAGE2_BOSS_SPAWN_SECONDS.length
                && frame == STAGE2_BOSS_SPAWN_SECONDS[bossSpawnIndex] * 60) {
            enemies.add(new Boss(BOARD_WIDTH, playfieldBottom() / 2 - 40, playfieldBottom(),
                    STAGE2_BOSS_HP));
            bossSpawnIndex++;
        }

        // Player
        if (invincibleFrames > 0) {
            invincibleFrames--;
        }
        if (shieldFrames > 0) {
            shieldFrames--;
        }
        player.act();
        int maxPlayerY = playfieldBottom() - player.getHeight() - 2;
        if (player.getY() > maxPlayerY) {
            player.setY(maxPlayerY);
        }

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    if (powerup instanceof ShieldUp) {
                        shieldFrames = SHIELD_DURATION_FRAMES;
                        powerup.die();
                    } else {
                        powerup.upgrade(player);
                    }
                }
            }
        }
        powerups.removeIf(p -> !p.isVisible());

        // Obstacles drift & tumble
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isVisible()) {
                obstacle.act();
            }
        }

        // Enemies: hunters steer toward the player before moving.
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                if (enemy instanceof Alien3) {
                    ((Alien3) enemy).setTargetY(player.getY() + player.getHeight() / 2);
                }
                enemy.act(direction);
            }
        }

        boolean invincible = invincibleFrames > 0 || shieldFrames > 0;
        boolean hitThisFrame = false;

        // Player <-> enemy collision
        if (!invincible && player.isVisible()) {
            for (Enemy enemy : enemies) {
                if (enemy.isVisible() && !enemy.isDying() && player.collidesWith(enemy)) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    if (!(enemy instanceof Boss)) {
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    }
                    handlePlayerHit();
                    hitThisFrame = true;
                    break;
                }
            }
        }

        // Player <-> obstacle collision (ramming doesn't destroy the rock)
        if (!invincible && !hitThisFrame && player.isVisible()) {
            for (Obstacle obstacle : obstacles) {
                if (obstacle.isVisible() && player.collidesWith(obstacle)) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    handlePlayerHit();
                    break;
                }
            }
        }

        // Shots vs enemies and obstacles
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (!shot.isVisible()) {
                continue;
            }

            boolean consumed = false;

            for (Enemy enemy : enemies) {
                if (enemy.isVisible() && !enemy.isDying() && shot.collidesWith(enemy)) {
                    if (enemy instanceof Boss) {
                        Boss boss = (Boss) enemy;
                        boss.hit();
                        if (boss.isDead()) {
                            boss.setDying(true);
                            explosions.add(new Explosion(
                                    boss.getX() + boss.getWidth() / 2 - 18,
                                    boss.getY() + boss.getHeight() / 2 - 18));
                            deaths += 5;
                            bossesDefeated++;
                        }
                    } else {
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths++;
                    }
                    shot.die();
                    shotsToRemove.add(shot);
                    consumed = true;
                    break;
                }
            }

            if (!consumed) {
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.isVisible() && shot.collidesWith(obstacle)) {
                        boolean destroyed = obstacle.hit();
                        if (destroyed) {
                            explosions.add(new Explosion(obstacle.getX(), obstacle.getY()));
                            obstacles.addAll(obstacle.split());
                            obstacle.die();
                            deaths += obstacle.getSize().getScore();
                        }
                        shot.die();
                        shotsToRemove.add(shot);
                        consumed = true;
                        break;
                    }
                }
            }

            if (!consumed) {
                int newX = shot.getX() + player.getShotSpeed();
                if (newX > BOARD_WIDTH) {
                    shot.die();
                    shotsToRemove.add(shot);
                } else {
                    shot.setX(newX);
                }
            }
        }
        shots.removeAll(shotsToRemove);

        enemies.removeIf(e -> !e.isVisible());
        obstacles.removeIf(o -> !o.isVisible());
    }

    private void handlePlayerHit() {
        lives--;
        if (lives <= 0) {
            player.die();
            inGame = false;
            timer.stop();
            message = "Game Over";
        } else {
            player.respawn();
            invincibleFrames = 120;
        }
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

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE) {
                if (inGame) {
                    if (shots.size() < 4) {
                        int tipX = player.getX() + player.getWidth();
                        int tipY = player.getY() + player.getHeight() / 2;
                        shots.add(new Shot(tipX, tipY));
                    }
                } else {
                    // Stage over (won or lost) — return to the title screen.
                    game.loadTitle();
                }
            }
        }
    }
}
