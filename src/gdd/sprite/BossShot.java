package gdd.sprite;

import static gdd.Global.*;
import gdd.ImageUtil;
import java.awt.Image;

/**
 * A bullet fired by a {@link Boss}.
 *
 * Faster than plane fire ({@link gdd.Global#BOSS_SHOT_SPEED}) and drawn as the
 * spinning orb in {@code src/images/Boss_shots} — art that reads the same at
 * any heading, which matters because stage 2's boss fires a spread rather than
 * straight down the sight line.
 *
 * Costs a hull point on contact, exactly like plane fire: it is an
 * {@link EnemyShot}, so Scene1's existing bullet handling (shield, hull, cave
 * walls) applies to it unchanged.
 */
public class BossShot extends EnemyShot {

    // One shared, pre-scaled copy of the animation for every boss bullet.
    private static Image[] frames;

    private int animTick = 0;

    /**
     * angleOffset rotates this bullet off the line to the player, so a volley
     * is the same call a few times with different offsets.
     */
    public BossShot(int muzzleX, int muzzleY, int targetX, int targetY, double angleOffset) {
        super(muzzleX, muzzleY, targetX, targetY, -1,
                BOSS_SHOT_SPEED, angleOffset, frames()[0]);
    }

    // fitAll, not fit: one shared bounding box across the frames, so the orb
    // spins in place instead of resizing as it cycles.
    private static Image[] frames() {
        if (frames == null) {
            frames = ImageUtil.fitAll(IMG_BOSS_SHOT, BOSS_SHOT_SIZE, BOSS_SHOT_SIZE);
        }
        return frames;
    }

    @Override
    public void act() {
        super.act(); // travel
        animTick++;
        setImage(frames[(animTick / BULLET_ANIM_FRAMES) % frames.length]);
    }
}
