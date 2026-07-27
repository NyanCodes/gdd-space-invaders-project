package gdd.sprite;

import static gdd.Global.*;
import gdd.GunTier;
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

    // Firing model. One trigger pull sends the whole volley at once, fanned
    // out around straight ahead, and then the gun reloads. How many bullets a
    // volley holds — and how wide the fan is — is the gun's rung on the
    // upgrade ladder: 1 → 2 → 4 → 6.
    private GunTier gunTier = GunTier.BASE;
    private int volleyShots = GunTier.BASE.volleyShots;
    private int shotDamage = GunTier.BASE.damage; // damage a single bullet deals

    // Granted by the stage-2 RearGunUp pickup: fires the same volley out the
    // tail as well as the front. Off by default and, like the gun ladder,
    // never lost once picked up.
    private boolean rearFire = false;

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
        return cooldownLeft == 0;
    }

    /** Books the volley just fired: the gun is now reloading. */
    public void startShotCooldown() {
        cooldownLeft = shotCooldown;
    }

    public int getShotCooldown() {
        return shotCooldown;
    }

    public int getCooldownLeft() {
        return cooldownLeft;
    }

    public int reduceShotCooldown(int frames) {
        shotCooldown = Math.max(PLAYER_SHOT_COOLDOWN_FLOOR, shotCooldown - frames);
        return shotCooldown;
    }

    public int getShotDamage() {
        return shotDamage;
    }

    /** Bullets the current rung fires per trigger pull, all at once. */
    public int getVolleyShots() {
        return volleyShots;
    }

    public GunTier getGunTier() {
        return gunTier;
    }

    /** True once the gun is off its starting rung, whichever rung it is on. */
    public boolean hasBulletFlower() {
        return gunTier != GunTier.BASE;
    }

    /**
     * Fits a gun-upgrade flower. Upgrades are permanent and only ever move
     * forward, so a pickup for a rung the player already has (or has passed)
     * does nothing — a stale drop can't downgrade the gun.
     */
    public void equipGun(GunTier tier) {
        if (tier == null || tier.ordinal() <= gunTier.ordinal()) {
            return;
        }
        gunTier = tier;
        shotDamage = tier.damage;
        volleyShots = tier.volleyShots;
        if (tier.shotSpeedBonus != 0) {
            setShotSpeed(shotSpeed + tier.shotSpeedBonus);
        }
    }

    /** Grants the rear-gun pickup. Permanent, like the gun ladder. */
    public void enableRearFire() {
        rearFire = true;
    }

    public boolean hasRearFire() {
        return rearFire;
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
    // damaged or mid-reload.
    public void respawn() {
        setX(START_X);
        setY(START_Y);
        dx = 0;
        dy = 0;
        hull = PLAYER_HULL;
        cooldownLeft = 0;
    }

    public void act() {
        if (shieldFrames > 0) {
            shieldFrames--;
        }
        if (cooldownLeft > 0) {
            cooldownLeft--;
        }

        x += dx;
        y += dy;

        // Clamp horizontally so the ship stays fully on-screen, free to roam
        // the whole board rather than just the left half.
        if (x < 2) {
            x = 2;
        }
        if (x > BOARD_WIDTH - width) {
            x = BOARD_WIDTH - width;
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
