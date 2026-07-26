package gdd;

/**
 * Everything that differs between one playable stage and the next.
 *
 * The scene logic is identical across stages, so rather than duplicating it
 * the scene reads its stage from here. Add a stage by adding a constant, a map
 * file, and a Scene subclass that passes it up.
 */
public class Stage {

    public final int number;          // 1-based, shown in the HUD
    public final String mapPath;      // terrain map file
    // When this stage's one boss enters, and therefore how long the stage's
    // approach runs. There is no timeout clear: the stage ends when the boss
    // dies, however long that takes.
    public final int bossSecond;
    public final boolean last;        // last stage? clearing it wins the game
    // Frame this stage's first enemy plane arrives. Only stage 1 holds them
    // back as a warm-up — later stages start them straight away, or the run
    // goes quiet for half a minute every time a stage begins.
    public final int firstPlaneFrame;
    // May planes enter from the left edge and catch the player from behind?
    // The gun only fires right, so a rear attacker can be dodged but not shot;
    // stage 1 keeps every plane in front of the player and stage 2 opens both
    // edges. This gates the model pool too — the rear-only plane 2 simply does
    // not appear in stage 1.
    public final boolean planesFromBehind;
    // The toughest plane model this stage fields (see EnemyPlane.Type.tier):
    // stage 1 tops out at the heavy, stage 2 adds the elite. A stage's own new
    // model joins one minute in (Global.HEAVY_PLANE_FRAME); everything an
    // earlier stage already unlocked is in the air from the first frame.
    public final int maxPlaneTier;

    // Where this stage sits on the run-long enemy pressure ramp (0 = a couple
    // of aliens drifting in, 1 = full pressure). Each stage picks up where the
    // previous one stopped, so the whole run ramps continuously instead of
    // resetting to easy at the start of stage 2. The ramp is spread over the
    // approach to the boss, so pressure peaks exactly as the boss arrives.
    public final double pressureStart;
    public final double pressureEnd;

    public Stage(int number, String mapPath,
            int bossSecond, boolean last,
            double pressureStart, double pressureEnd, int firstPlaneFrame,
            boolean planesFromBehind, int maxPlaneTier) {
        this.number = number;
        this.mapPath = mapPath;
        this.bossSecond = bossSecond;
        this.last = last;
        this.pressureStart = pressureStart;
        this.pressureEnd = pressureEnd;
        this.firstPlaneFrame = firstPlaneFrame;
        this.planesFromBehind = planesFromBehind;
        this.maxPlaneTier = maxPlaneTier;
    }

    public static final Stage ONE = new Stage(1, Global.MAP_LEVEL_1,
            Global.STAGE_1_BOSS_SECOND, false,
            0.0, 0.45, Global.PLANE_FIRST_WAVE_FRAME, false, 1);

    public static final Stage TWO = new Stage(2, Global.MAP_LEVEL_2,
            Global.STAGE_2_BOSS_SECOND, true,
            0.45, 1.0, 0, true, 2);
}
