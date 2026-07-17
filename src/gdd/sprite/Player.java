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
    private int currentSpeed = 1;
    private int shotSpeed = 12; // pixels per frame a bullet travels
    private int shotCooldown = 120; // frames between shots (2s to start)
    private int cooldownLeft = 0; // frames until the next shot is allowed
    private int shieldFrames = 0; // golden shield time left, in frames

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
        shotCooldown = Math.max(30, shotCooldown - frames); // floor at 0.5s
        return shotCooldown;
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

    // Reset to the start position after losing a life (keeps upgrades).
    public void respawn() {
        setX(START_X);
        setY(START_Y);
        dx = 0;
        dy = 0;
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
