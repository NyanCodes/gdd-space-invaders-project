package gdd;

/**
 * The player's gun as a ladder of upgrades.
 *
 * Each flower pickup moves the ship exactly one rung up, and a rung is never
 * lost, so the gun only ever goes forward. What a rung buys is <em>a wider
 * volley</em> — 1, 2, 4, 6, 8, then 10 bullets, all fired on the same trigger
 * pull and spread into a forward fan, so a higher rung covers more of the
 * screen at once. Only the first upgrade changes the bullet itself (double
 * damage and a speed bonus); after that the bullet is the same weight and
 * simply arrives in greater numbers. The top two rungs (8X/10X) reuse the
 * charged bolt's art outright — there is nothing left to change but the
 * width of the fan.
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

    /** The starting gun: one plain bullet, one damage, straight ahead. */
    BASE(1, 0, 1, 0, null, null, null, 0, 0, 0),

    /** Blue "2X" flower — the shot2 bullet: double damage, two directions. */
    FLOWER(Global.BULLET_VOLLEY_SHOTS, Global.BULLET_SPREAD_DEGREES,
            Global.BULLET_DAMAGE, Global.BULLET_SHOT_SPEED_BONUS,
            new String[]{Global.IMG_SHOT2}, Global.IMG_POWERUP_BULLET, "PowerUp-Bullet",
            Global.SHOT2_WIDTH, Global.SHOT2_HEIGHT, 0),

    /** Orange "4X" flower — the bolt: a four-way fan. */
    BOLT(Global.BOLT_VOLLEY_SHOTS, Global.BOLT_SPREAD_DEGREES,
            Global.BULLET_DAMAGE, 0,
            Global.IMG_bolt, Global.IMG_POWERUP_BOLT, "PowerUp-Bolt",
            Global.BOLT_WIDTH, Global.BOLT_HEIGHT, 1),

    /** Purple "6X" flower — the charged bolt: a six-way fan. */
    CHARGED(Global.CHARGED_VOLLEY_SHOTS, Global.CHARGED_SPREAD_DEGREES,
            Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_CHARGED, "PowerUp-Charged",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2),

    /** Green "8X" flower — same charged-bolt bullet, an eight-way fan. */
    EIGHT(Global.EIGHT_VOLLEY_SHOTS, Global.EIGHT_SPREAD_DEGREES,
            Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_EIGHT, "PowerUp-Eight",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2),

    /** Cyan "10X" flower — the top of the ladder: a ten-way fan. */
    TEN(Global.TEN_VOLLEY_SHOTS, Global.TEN_SPREAD_DEGREES,
            Global.BULLET_DAMAGE, 0,
            Global.IMG_charge, Global.IMG_POWERUP_TEN, "PowerUp-Ten",
            Global.CHARGED_WIDTH, Global.CHARGED_HEIGHT, 2);

    /** Bullets this rung puts out per trigger pull, all at the same moment. */
    public final int volleyShots;
    /** Angle of the outermost bullet in the fan, in degrees off straight ahead. */
    public final int spreadDegrees;
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

    GunTier(int volleyShots, int spreadDegrees, int damage, int shotSpeedBonus,
            String[] bulletFrames, String[] pickupFrames, String spawnKey,
            int bulletWidth, int bulletHeight, int unlockPlaneTier) {
        this.volleyShots = volleyShots;
        this.spreadDegrees = spreadDegrees;
        this.damage = damage;
        this.shotSpeedBonus = shotSpeedBonus;
        this.bulletFrames = bulletFrames;
        this.pickupFrames = pickupFrames;
        this.spawnKey = spawnKey;
        this.bulletWidth = bulletWidth;
        this.bulletHeight = bulletHeight;
        this.unlockPlaneTier = unlockPlaneTier;
    }

    /**
     * Heading of bullet {@code index} of this rung's volley, in degrees, where
     * 0 is straight ahead and the fan is symmetric about it. A single-shot
     * rung always fires straight.
     */
    public double angleFor(int index) {
        if (volleyShots <= 1 || spreadDegrees == 0) {
            return 0;
        }
        double step = (2.0 * spreadDegrees) / (volleyShots - 1);
        return -spreadDegrees + step * index;
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
