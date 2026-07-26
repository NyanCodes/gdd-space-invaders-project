package gdd.powerup;

import static gdd.Global.*;
import gdd.GunTier;
import gdd.sprite.Player;

/**
 * A gun-upgrade flower. Which rung of the ladder it grants is the tier it was
 * built with, and the tier also picks the artwork — blue "2X", orange "4X",
 * purple "6X".
 *
 * The upgrade is permanent, so Scene1 only ever drops the one rung above the
 * gun the player is currently holding.
 */
public class BulletUp extends PowerUp {

    private final GunTier tier;

    /** The original blue flower — the first rung. */
    public BulletUp(int x, int y) {
        this(x, y, GunTier.FLOWER);
    }

    public BulletUp(int x, int y, GunTier tier) {
        super(x, y);
        this.tier = tier == null ? GunTier.FLOWER : tier;
        loadFrames(this.tier.pickupFrames, POWERUP_SIZE, POWERUP_SIZE);
    }

    public GunTier getTier() {
        return tier;
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
        player.equipGun(tier);
        this.die(); // Remove the power-up after use
    }
}
