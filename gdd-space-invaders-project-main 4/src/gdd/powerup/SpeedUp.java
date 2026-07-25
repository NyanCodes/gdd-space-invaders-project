package gdd.powerup;

import static gdd.Global.*;
import gdd.ImageUtil;
import gdd.sprite.Player;

/**
 * Red mushroom: makes the ship quicker in every way.
 */
public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        setImage(ImageUtil.fit(IMG_POWERUP_SPEEDUP, POWERUP_SIZE, POWERUP_SIZE));
    }

    @Override
    public void act() {
        // Horizontal side-scroller: drift left from the right edge so the
        // player can fly into it.
        this.x -= POWERUP_DRIFT_SPEED;
        if (this.x < -60) {
            die(); // Off-screen, clean up
        }
    }

    @Override
    public void upgrade(Player player) {
        // Boost ship movement, bullet speed, and fire rate (all capped in Player).
        player.setSpeed(player.getSpeed() + 1);
        player.setShotSpeed(player.getShotSpeed() + 4);
        player.reduceShotCooldown(25);
        this.die(); // Remove the power-up after use
    }
}
