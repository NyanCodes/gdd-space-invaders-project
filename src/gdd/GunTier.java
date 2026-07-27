package gdd;

/**
 * The player's gun as a ladder of upgrades.
 *
 * Each flower pickup moves the ship exactly one rung up, and a rung is never
 * lost, so the gun only ever goes forward. What a rung buys is <em>shots
 * before the reload</em> — 1, 2, 4, 6, 8, then 10. Only the first upgrade
 * changes the bullet itself (double damage and a speed bonus); after that the
 * bullet is the same weight and simply comes more often. The top two rungs
 * (8X/10X) reuse the charged bolt's art outright — there is nothing left to
 * change but the burst size.
 *
 * Each rung is unlocked by the enemy it exists to answer: the bolt flower only
 * starts dropping once the player has met a heavy plane, and the charged
 * flower (and everything above it) once they have met an elite — see
 * {@link #unlockPlaneTier}. On top of that, {@code Scene1.dueGunTier} runs the
 * ladder on a fixed clock (first rung at a set time, then one every
 * {@code Global.GUN_UPGRADE_INTERVAL_SECONDS}) so the climb is predictable
 * rather than left to the random drop pool; the plane-tier requirement still
 * applies underneath as a floor, capped at the toughest tier the current
 * stage actually fields (a stage with no elite can't be blocked waiting for
 * one — see {@code Stage.planeTierFrames}), and {@code Stage.gunTierCeiling}
 * is how far a given stage's ladder is allowed to reach at all.
 */
public enum GunTier {

    /** The starting gun: one plain bullet, one damage, per reload. */
    BASE(1, 1, 0, null, null, null, 0, 0, 0),

    /** Blue "2X" flower — the shot2 bullet: double damage, two shots. */
    FLOWER(Global.BULLET_SHOTS_PER_BURST, Global.BULLET_DAMAGE,
            Global.BULLET_SHOT_SPEED_BONUS,
            new String[]{Global.IMG_SHOT2}, Global.IMG_POWERUP_BULLET, "PowerUp-Bullet",
            Global.SHOT2_WIDTH, Global.SHOT2_HEIGHT, 0),

    /** Orange "4X" flower — the bolt: four shots before the reload. */
    BOLT(Global.BOLT_SHOTS_PER_BURST, Global.BULLET_DAMAGE, 0,
            Global.IMG_bolt, Global.IMG_POWERUP_BOLT, "PowerUp-Bolt",
            Global.BOLT_WIDTH, Global.BOLT_HEIGHT, 1),

    /** Purple "6X" flower — the charged bolt: six shots before the reload. */
    CHARGED(Global.CHARGED_SHOTS_PER_BURST, Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_CHARGED, "PowerUp-Charged",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2),

    /** Green "8X" flower — same charged-bolt bullet, eight shots before the reload. */
    EIGHT(Global.EIGHT_SHOTS_PER_BURST, Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_EIGHT, "PowerUp-Eight",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2),

    /** Cyan "10X" flower — the top of the ladder: ten shots before the reload. */
    TEN(Global.TEN_SHOTS_PER_BURST, Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_TEN, "PowerUp-Ten",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2);

    public final int shotsPerBurst;
    public final int damage;
    public final int shotSpeedBonus;
    /** Bullet art. More than one entry means the bullet animates. */
    public final String[] bulletFrames;
    /** The spinning pickup that grants this rung. */
    public final String[] pickupFrames;
    /** Name this rung answers to in the scripted spawn map. */
    public final String spawnKey;
    public final int bulletWidth;
    public final int bulletHeight;
    /**
     * The plane tier the player must have seen before this rung starts
     * dropping — 0 = from the start, 1 = after a heavy plane, 2 = after an
     * elite. Keeps the upgrade an answer to a problem rather than a freebie.
     */
    public final int unlockPlaneTier;

    GunTier(int shotsPerBurst, int damage, int shotSpeedBonus,
            String[] bulletFrames, String[] pickupFrames, String spawnKey,
            int bulletWidth, int bulletHeight, int unlockPlaneTier) {
        this.shotsPerBurst = shotsPerBurst;
        this.damage = damage;
        this.shotSpeedBonus = shotSpeedBonus;
        this.bulletFrames = bulletFrames;
        this.pickupFrames = pickupFrames;
        this.spawnKey = spawnKey;
        this.bulletWidth = bulletWidth;
        this.bulletHeight = bulletHeight;
        this.unlockPlaneTier = unlockPlaneTier;
    }

    /** The next rung up, or null if the gun is already fully upgraded. */
    public GunTier next() {
        GunTier[] all = values();
        return ordinal() + 1 < all.length ? all[ordinal() + 1] : null;
    }

    /** The rung a spawn-map entry names, or null if it names something else. */
    public static GunTier forSpawnKey(String key) {
        for (GunTier tier : values()) {
            if (key.equals(tier.spawnKey)) {
                return tier;
            }
        }
        return null;
    }
}
