package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

/**
 * Red heart: one extra life.
 *
 * Lives live on the scene rather than on the Player, so the grant itself comes
 * through {@link #livesGranted()} and Scene1 applies it (capped at
 * PLAYER_LIVES). Everything else about the pickup — the spin, the leftward
 * drift, the collision — works exactly like the other drops.
 */
public class HeartUp extends PowerUp {

    public HeartUp(int x, int y) {
        super(x, y);
        loadFrames(IMG_POWERUP_HEART, POWERUP_SIZE, POWERUP_SIZE);
    }

    @Override
    public int livesGranted() {
        return 1;
    }

    @Override
    public void act() {
        updateAnimation();
        // Drift left from the right edge so the player can fly into it.
        this.x -= POWERUP_DRIFT_SPEED;
        if (this.x < -60) {
            die(); // Off-screen, clean up
        }
    }

    @Override
    public void upgrade(Player player) {
        // The life is granted by the scene; nothing on the ship changes.
        this.die();
    }
}
