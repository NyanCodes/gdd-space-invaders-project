package gdd.powerup;

import static gdd.Global.*;
import gdd.ImageUtil;
import gdd.sprite.Player;

/**
 * Green mushroom: a golden shield that shrugs off enemies for a few seconds.
 */
public class ShieldUp extends PowerUp {

    public ShieldUp(int x, int y) {
        super(x, y);
        setImage(ImageUtil.fit(IMG_POWERUP_SHIELD, POWERUP_SIZE, POWERUP_SIZE));
    }

    @Override
    public void act() {
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
