package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class ShieldUp extends PowerUp {

    public ShieldUp(int x, int y) {
        super(x, y);
        loadFrames(IMG_POWERUP_SHIELD, POWERUP_SIZE, POWERUP_SIZE);
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
        player.activateShield(SHIELD_DURATION_SECONDS * 60);
        this.die(); // Remove the power-up after use
    }
}
