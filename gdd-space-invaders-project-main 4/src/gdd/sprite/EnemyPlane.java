package gdd.sprite;

import static gdd.Global.*;
import gdd.ImageUtil;
import java.awt.Image;
import java.util.HashMap;
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

    /** The three models, and which edges each is allowed to enter from. */
    public enum Type {
        /** Green fighter: only ever attacks the player's front. */
        PLANE1(IMG_PLANE_1, true, false),
        /** Red biplane: only ever sneaks up on the player's back. */
        PLANE2(IMG_PLANE_2, false, true),
        /** Twin-boom heavy: comes from either side. */
        PLANE3(IMG_PLANE_3, true, true);

        public final String image;
        private final boolean fromRight; // may fly LEFTWARD
        private final boolean fromLeft;  // may fly RIGHTWARD

        Type(String image, boolean fromRight, boolean fromLeft) {
            this.image = image;
            this.fromRight = fromRight;
            this.fromLeft = fromLeft;
        }

        /** A heading this model may fly, picked at random when it allows both. */
        public Heading pickHeading(Random random) {
            if (fromRight && fromLeft) {
                return random.nextBoolean() ? Heading.LEFTWARD : Heading.RIGHTWARD;
            }
            return fromRight ? Heading.LEFTWARD : Heading.RIGHTWARD;
        }
    }

    // Trimming, scaling and mirroring the art is per-pixel work, so each
    // model/heading pair is built once and shared by every plane of that kind.
    private static final Map<String, Image> SPRITES = new HashMap<>();

    private final Heading heading;
    private final int width;
    private final int height;
    private int hull = PLANE_HULL;
    private int fireCooldown = PLANE_FIRE_COOLDOWN;

    public EnemyPlane(Type type, Heading heading, int x, int y) {
        super(x, y);

        this.heading = heading;

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
            sprite = ImageUtil.fit(type.image, PLANE_SIZE, PLANE_SIZE);
            if (heading == Heading.RIGHTWARD) {
                sprite = ImageUtil.mirror(sprite);
            }
            SPRITES.put(key, sprite);
        }
        return sprite;
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

    // damage is the player's per-bullet damage: 1 normally, 2 with the bullet
    // flower, so an upgraded gun drops a plane in a single hit.
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
