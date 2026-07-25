package gdd.sprite;

import static gdd.Global.*;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    // Horizontal side-scroller: player flies on the left, centered vertically.
    private static final int START_X = 20;
    private static final int START_Y = BOARD_HEIGHT / 2;
    private int width;
    private int height;
    private int currentSpeed = PLAYER_SPEED; // pixels per frame, before power-ups
    private int shotSpeed = PLAYER_SHOT_SPEED; // pixels per frame a bullet travels
    private int shotCooldown = PLAYER_SHOT_COOLDOWN; // frames between shots
    private int cooldownLeft = 0; // frames until the next shot is allowed
    private int shieldFrames = 0; // golden shield time left, in frames

    // Hull points, spent only by enemy planes and their bullets. Aliens,
    // bosses and cave walls still take a whole life on contact.
    private int hull = PLAYER_HULL;

    // Firing model. Normally one shot per reload; the bullet flower raises
    // shotsPerBurst to 2, so the second shot follows the first after a short
    // burstGap and only then does the full reload start.
    private int shotsPerBurst = 1;
    private int burstLeft = 1; // shots still available before the reload
    private int burstGap = 0; // frames until the next shot inside a burst
    private int shotDamage = 1; // damage a single bullet deals
    private boolean bulletFlower = false; // gun upgraded to the shot2 bullet

    private Rectangle bounds = new Rectangle(175,135,17,32);

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);

        // Store real scaled dimensions so the clamp in act() works.
        this.width = ii.getIconWidth() * SCALE_FACTOR;
        this.height = ii.getIconHeight() * SCALE_FACTOR;

        setX(START_X);
        setY(START_Y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {
        if (speed < 1) {
            speed = 1; // Ensure speed is at least 1
        }
        if (speed > 8) {
            speed = 8; // Cap so stacked power-ups stay controllable
        }
        this.currentSpeed = speed;
        return currentSpeed;
    }

    public int getShotSpeed() {
        return shotSpeed;
    }

    public int setShotSpeed(int speed) {
        if (speed < 8) {
            speed = 8;
        }
        if (speed > 40) {
            speed = 40;
        }
        this.shotSpeed = speed;
        return shotSpeed;
    }

    public boolean canShoot() {
        return cooldownLeft == 0 && burstGap == 0;
    }

    /** Books the shot just fired: either the burst continues, or we reload. */
    public void startShotCooldown() {
        burstLeft--;
        if (burstLeft > 0) {
            burstGap = BULLET_BURST_GAP_FRAMES; // second barrel, right behind the first
        } else {
            burstLeft = shotsPerBurst;
            cooldownLeft = shotCooldown;
        }
    }

    public int getShotCooldown() {
        return shotCooldown;
    }

    public int getCooldownLeft() {
        return cooldownLeft;
    }

    public int reduceShotCooldown(int frames) {
        shotCooldown = Math.max(30, shotCooldown - frames); // floor at 0.5s
        return shotCooldown;
    }

    public int getShotDamage() {
        return shotDamage;
    }

    public int getShotsPerBurst() {
        return shotsPerBurst;
    }

    public boolean hasBulletFlower() {
        return bulletFlower;
    }

    /**
     * Bullet flower pickup: the shot2 bullet. Faster, double damage, and two
     * shots back to back before the reload. Permanent for the rest of the run,
     * so picking up a second one is a no-op beyond the speed cap.
     */
    public void equipBulletFlower() {
        bulletFlower = true;
        shotDamage = BULLET_DAMAGE;
        shotsPerBurst = BULLET_SHOTS_PER_BURST;
        burstLeft = shotsPerBurst;
        burstGap = 0;
        setShotSpeed(shotSpeed + BULLET_SHOT_SPEED_BONUS);
    }

    public void activateShield(int frames) {
        shieldFrames = frames;
    }

    public boolean isShieldActive() {
        return shieldFrames > 0;
    }

    public int getShieldFrames() {
        return shieldFrames;
    }

    /** One enemy bullet costs the whole shield, however much time was left. */
    public void breakShield() {
        shieldFrames = 0;
    }

    public int getHull() {
        return hull;
    }

    public int getMaxHull() {
        return PLAYER_HULL;
    }

    /**
     * Takes one point of plane damage. Returns true when that emptied the
     * hull, which is the scene's cue to spend a life.
     */
    public boolean takeHit() {
        hull = Math.max(0, hull - 1);
        return hull == 0;
    }

    // Reset to the start position after losing a life (keeps upgrades).
    // The hull and the gun are both restored, so a respawn never starts
    // damaged or mid-burst.
    public void respawn() {
        setX(START_X);
        setY(START_Y);
        dx = 0;
        dy = 0;
        hull = PLAYER_HULL;
        cooldownLeft = 0;
        burstGap = 0;
        burstLeft = shotsPerBurst;
    }

    public void act() {
        if (shieldFrames > 0) {
            shieldFrames--;
        }
        if (cooldownLeft > 0) {
            cooldownLeft--;
        }
        if (burstGap > 0) {
            burstGap--;
        }

        x += dx;
        y += dy;

        // Clamp to the left region so the player can't cross the whole screen.
        if (x < 2) {
            x = 2;
        }
        if (x > BOARD_WIDTH / 2) {
            x = BOARD_WIDTH / 2;
        }

        // Clamp vertically so the ship stays fully on-screen.
        if (y < 2) {
            y = 2;
        }
        if (y > BOARD_HEIGHT - height) {
            y = BOARD_HEIGHT - height;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            dx = -currentSpeed;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            dx = currentSpeed;
        }

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            dy = -currentSpeed;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            dy = currentSpeed;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            dx = 0;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            dx = 0;
        }

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            dy = 0;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            dy = 0;
        }
    }
}
