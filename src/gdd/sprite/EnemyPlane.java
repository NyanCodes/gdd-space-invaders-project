package gdd.sprite;

import static gdd.Global.*;
import gdd.ImageUtil;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A voxel warplane, baked from the MagicaVoxel models in src/images/Plane 0*.
 *
 * Planes fly a straight, level line across the whole field and shoot back on
 * the same trigger interval as the player's starting gun. They take
 * PLANE_HULL damage to bring down, which is two default bullets or a single
 * bullet-flower hit, and two planes that run into each other both go down.
 *
 * Like {@link Alien1} and {@link Boss} this defines {@code act(int)} — an
 * overload of {@link Sprite#act()}, not an override — because Scene1 drives
 * enemies with the shared formation direction. Planes ignore that argument and
 * fly their own {@link Heading} instead.
 */
public class EnemyPlane extends Enemy {

    /** Which way a plane flies, and therefore which edge it enters from. */
    public enum Heading {
        /** Enters from the right edge and comes at the player head-on. */
        LEFTWARD(-1),
        /** Enters from the left edge and catches the player from behind. */
        RIGHTWARD(1);

        public final int step; // sign applied to PLANE_SPEED

        Heading(int step) {
            this.step = step;
        }
    }

    /**
     * The models, which edges each may enter from, and how much punishment it
     * takes.
     *
     * {@code tier} is the escalation rung: 0 is the light traffic that has
     * been there from the start, 1 the heavy that joins a minute into stage 1,
     * 2 the elite that only stage 2 fields. It is also what unlocks the
     * matching rung of the player's gun (see {@link gdd.GunTier}).
     */
    public enum Type {
        /** Green fighter: only ever attacks the player's front. */
        PLANE1(IMG_PLANE_1, true, false, 0, PLANE_HULL, PLANE_SIZE, PLANE_SCORE),
        /** Red biplane: only ever sneaks up on the player's back. */
        PLANE2(IMG_PLANE_2, false, true, 0, PLANE_HULL, PLANE_SIZE, PLANE_SCORE),
        /** Twin-boom heavy: comes from either side. */
        PLANE3(IMG_PLANE_3, true, true, 0, PLANE_HULL, PLANE_SIZE, PLANE_SCORE),
        /** Red triplane: the heavy. Two bullet-flower hits, either side. */
        PLANE5(IMG_PLANE_5, true, true, 1,
                HEAVY_PLANE_HULL, HEAVY_PLANE_SIZE, HEAVY_PLANE_SCORE),
        /** Tan fighter: the elite. Four bullet-flower hits, either side. */
        PLANE6(IMG_PLANE_6, true, true, 2,
                ELITE_PLANE_HULL, ELITE_PLANE_SIZE, ELITE_PLANE_SCORE);

        public final String image;
        public final int tier;
        public final int hull;
        public final int size;  // sprite is fitted to this box, px
        public final int score;
        private final boolean fromRight; // may fly LEFTWARD
        private final boolean fromLeft;  // may fly RIGHTWARD

        Type(String image, boolean fromRight, boolean fromLeft,
                int tier, int hull, int size, int score) {
            this.image = image;
            this.fromRight = fromRight;
            this.fromLeft = fromLeft;
            this.tier = tier;
            this.hull = hull;
            this.size = size;
            this.score = score;
        }

        /**
         * A heading this model may fly, picked at random when both are open.
         *
         * allowFromBehind is the stage rule: stage 1 keeps every plane in
         * front of the player (the gun only fires right, so a rear attacker
         * can be dodged but never shot), stage 2 opens the back up. Only
         * models returned by {@link #pick} are asked, so a rear-only model is
         * never handed allowFromBehind = false.
         */
        public Heading pickHeading(Random random, boolean allowFromBehind) {
            boolean rear = fromLeft && allowFromBehind;
            if (fromRight && rear) {
                return random.nextBoolean() ? Heading.LEFTWARD : Heading.RIGHTWARD;
            }
            return fromRight ? Heading.LEFTWARD : Heading.RIGHTWARD;
        }

        /**
         * A random model that may fly under the current rules.
         *
         * maxTier is how far up the escalation this stage has got — models
         * above it simply aren't in the air yet. heavyShare (0..1, normally
         * the stage's pressure) is how strongly the traffic favours the
         * tougher models: they enter the pool on equal footing with the light
         * ones and climb to HEAVY_PLANE_MAX_WEIGHT times their share as the
         * stage tops out, so the mix thickens rather than flipping over.
         *
         * Falls back to the light pool if the rules exclude everything, so
         * this can never return null.
         */
        public static Type pick(Random random, boolean allowFromBehind,
                int maxTier, double heavyShare) {

            List<Type> pool = new ArrayList<>();
            for (Type t : values()) {
                if (t.tier > maxTier) {
                    continue;
                }
                if (!allowFromBehind && !t.fromRight) {
                    continue; // rear-only model, and the rear is closed
                }
                int weight = t.tier == 0 ? 1
                        : 1 + (int) Math.round(
                                Math.max(0, Math.min(1, heavyShare))
                                * (HEAVY_PLANE_MAX_WEIGHT - 1));
                for (int i = 0; i < weight; i++) {
                    pool.add(t);
                }
            }
            if (pool.isEmpty()) {
                pool.add(allowFromBehind ? PLANE3 : PLANE1);
            }
            return pool.get(random.nextInt(pool.size()));
        }
    }

    // Trimming, scaling and mirroring the art is per-pixel work, so each
    // model/heading pair is built once and shared by every plane of that kind.
    private static final Map<String, Image> SPRITES = new HashMap<>();

    private final Type type;
    private final Heading heading;
    private final int width;
    private final int height;
    private int hull;
    private int fireCooldown = PLANE_FIRE_COOLDOWN;

    public EnemyPlane(Type type, Heading heading, int x, int y) {
        super(x, y);

        this.type = type;
        this.heading = heading;
        this.hull = type.hull;

        Image sprite = spriteFor(type, heading);
        setImage(sprite);
        this.width = sprite.getWidth(null);
        this.height = sprite.getHeight(null);
    }

    // The PNGs are all baked nose-left, so only the other heading is mirrored.
    private static Image spriteFor(Type type, Heading heading) {
        String key = type.name() + "-" + heading.name();
        Image sprite = SPRITES.get(key);
        if (sprite == null) {
            sprite = ImageUtil.fit(type.image, type.size, type.size);
            if (heading == Heading.RIGHTWARD) {
                sprite = ImageUtil.mirror(sprite);
            }
            SPRITES.put(key, sprite);
        }
        return sprite;
    }

    public Type getType() {
        return type;
    }

    /** What this model is worth on the scoreboard. */
    public int getScore() {
        return type.score;
    }

    public int getMaxHull() {
        return type.hull;
    }

    @Override
    public void act(int direction) {
        // Straight and level along its own heading; the shared formation
        // direction the aliens use does not apply.
        x += heading.step * PLANE_SPEED;

        if (fireCooldown > 0) {
            fireCooldown--;
        }

        // Clean up once fully past the edge it is heading for. Only that edge
        // counts — planes start just outside the one they enter from, so
        // checking both would kill them on their first frame.
        boolean gone = heading == Heading.LEFTWARD ? x + width < 0 : x > BOARD_WIDTH;
        if (gone) {
            die();
        }
    }

    /** Fully past neither edge — a plane only shoots once the player can see it. */
    public boolean isOnScreen() {
        return x + width > 0 && x < BOARD_WIDTH;
    }

    public boolean readyToFire() {
        return fireCooldown == 0 && isOnScreen();
    }

    /** Restarts the trigger interval after Scene1 spawns the bullet. */
    public void noteFired() {
        fireCooldown = PLANE_FIRE_COOLDOWN;
    }

    /** Where a bullet leaves the plane: the middle of its nose. */
    public int getMuzzleX() {
        return heading == Heading.LEFTWARD ? x : x + width;
    }

    public int getMuzzleY() {
        return y + height / 2;
    }

    // damage is the player's per-bullet damage: 1 normally, 2 from the bullet
    // flower up. A light plane goes down in one flower hit, the heavy takes
    // two and the elite four.
    public void hit(int damage) {
        hull = Math.max(0, hull - damage);
    }

    public boolean isDead() {
        return hull <= 0;
    }

    public int getHull() {
        return hull;
    }

    public Heading getHeading() {
        return heading;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
