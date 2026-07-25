package gdd.powerup;

import static gdd.Global.*;
import gdd.ImageUtil;
import gdd.sprite.Player;

/**
 * Blue flower: upgrades the gun to the heavier shot2 bullet.
 *
 * The upgrade is permanent for the rest of the run, so Scene1 stops rolling it
 * as a random drop once the player is holding one.
 */
public class BulletUp extends PowerUp {

    public BulletUp(int x, int y) {
        super(x, y);
        setImage(ImageUtil.fit(IMG_POWERUP_BULLET, POWERUP_SIZE, POWERUP_SIZE));
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
        player.equipBulletFlower();
        this.die(); // Remove the power-up after use
    }
}
