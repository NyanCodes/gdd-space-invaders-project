package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3; // Scaling factor for sprites

    public static final int BOARD_WIDTH = 716; // Doubled from 358
    public static final int BOARD_HEIGHT = 700; // Doubled from 350
    public static final int BORDER_RIGHT = 60; // Doubled from 30
    public static final int BORDER_LEFT = 10; // Doubled from 5

    public static final int GROUND = 580; // Doubled from 290
    public static final int BOMB_HEIGHT = 10; // Doubled from 5

    public static final int ALIEN_HEIGHT = 24; // Doubled from 12
    public static final int ALIEN_WIDTH = 24; // Doubled from 12
    public static final int ALIEN_INIT_X = 300; // Doubled from 150
    public static final int ALIEN_INIT_Y = 10; // Doubled from 5
    public static final int ALIEN_GAP = 30; // Gap between aliens

    public static final int GO_DOWN = 30; // Doubled from 15
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 30; // Doubled from 15
    public static final int PLAYER_HEIGHT = 20; // Doubled from 10

    // Game flow / HUD
    public static final int GAME_DURATION_SECONDS = 300; // stage length: 5 minutes
    public static final int PLAYER_LIVES = 3;
    public static final int DASHBOARD_HEIGHT = 64; // bottom status bar

    // Boss
    public static final int BOSS_HP = 5; // shots needed to kill a boss
    public static final int[] BOSS_SPAWN_SECONDS = {30, 60}; // add entries to extend

    // Power-ups
    public static final int SHIELD_DURATION_SECONDS = 10; // golden shield lifetime
    public static final int TIPBOX_SECONDS = 12; // how long the power-up tip box shows at start

    // Audio
    public static final float DEFAULT_VOLUME = 0.50f; // slider position on first launch (0.0 - 1.0)
    public static final float VOLUME_STEP = 0.05f; // options menu slider increment
    // Slider position is raised to this power to get amplitude, the way a real volume
    // knob tapers. log(0.2)/log(0.5), so the middle of the bar lands on 20% amplitude.
    public static final float VOLUME_TAPER = 2.3219f;

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_PLAYER = "src/images/ship1.png";
    public static final String IMG_BACKGROUND = "src/images/game_background.jpg";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_POWERUP_SHIELD = "src/images/shield-mushroom.png";
}
