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
    public final int durationSeconds; // survive this long to clear it
    public final int[] bossSeconds;   // when bosses enter
    public final boolean last;        // last stage? clearing it wins the game
    // Frame this stage's first enemy plane arrives. Only stage 1 holds them
    // back as a warm-up — later stages start them straight away, or the run
    // goes quiet for half a minute every time a stage begins.
    public final int firstPlaneFrame;

    // Where this stage sits on the run-long enemy pressure ramp (0 = a couple
    // of aliens drifting in, 1 = full pressure). Each stage picks up where the
    // previous one stopped, so the whole 7-minute run ramps continuously
    // instead of resetting to easy at the start of stage 2.
    public final double pressureStart;
    public final double pressureEnd;

    public Stage(int number, String mapPath, int durationSeconds,
            int[] bossSeconds, boolean last,
            double pressureStart, double pressureEnd, int firstPlaneFrame) {
        this.number = number;
        this.mapPath = mapPath;
        this.durationSeconds = durationSeconds;
        this.bossSeconds = bossSeconds;
        this.last = last;
        this.pressureStart = pressureStart;
        this.pressureEnd = pressureEnd;
        this.firstPlaneFrame = firstPlaneFrame;
    }

    public static final Stage ONE = new Stage(1, Global.MAP_LEVEL_1,
            Global.STAGE_1_SECONDS, Global.STAGE_1_BOSS_SECONDS, false,
            0.0, 0.45, Global.PLANE_FIRST_WAVE_FRAME);

    public static final Stage TWO = new Stage(2, Global.MAP_LEVEL_2,
            Global.STAGE_2_SECONDS, Global.STAGE_2_BOSS_SECONDS, true,
            0.45, 1.0, 0);
}
