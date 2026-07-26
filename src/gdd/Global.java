package gdd;

import java.awt.Color;

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
    public static final int PLAYER_LIVES = 3;
    public static final int DASHBOARD_HEIGHT = 64; // bottom status bar

    // The ship's starting gun and engine. Enemy planes fire on the same
    // interval, so these are the single source for "the player's default".
    public static final int PLAYER_SPEED = 5; // px per frame; ~120 px/s
    public static final int PLAYER_SHOT_SPEED = 20; // px per frame a bullet travels
    public static final int PLAYER_SHOT_COOLDOWN = 90; // frames between shots (1.5s)

    // Hull points. Only enemy planes and their bullets chip away at this — a
    // cave wall, an alien or a boss still costs a whole life on contact.
    public static final int PLAYER_HULL = 2;
    public static final int HIT_INVINCIBLE_FRAMES = 45; // mercy blink after a plane hit

    // Score values
    public static final int ALIEN_SCORE = 1;
    public static final int PLANE_SCORE = 3;
    public static final int BOSS_SCORE = 5;

    // Stages. Each one is a terrain map file plus its own length and boss
    // schedule; the boss times are picked to land in the wide "arena" sections
    // of the map, so moving one means moving the other.
    public static final String MAP_LEVEL_1 = "src/maps/level1.txt";
    public static final String MAP_LEVEL_2 = "src/maps/level2.txt";
    public static final int STAGE_1_SECONDS = 180; // 3 minutes
    public static final int STAGE_2_SECONDS = 240; // 4 minutes  (7 minutes per full run)
    public static final int[] STAGE_1_BOSS_SECONDS = {55, 100, 155};
    public static final int[] STAGE_2_BOSS_SECONDS = {40, 95, 155, 215};

    // Boss
    public static final int BOSS_HP = 5; // damage needed to kill a boss

    // Enemy pressure ramp. Waves start small and far apart and tighten as the
    // run goes on; Scene1 interpolates between these with Stage.pressure*.
    public static final int ALIEN_FIRST_WAVE_FRAME = 240; // first random wave (4s in)
    public static final int ALIEN_WAVE_GAP_START = 135; // frames between waves at zero pressure
    public static final int ALIEN_WAVE_GAP_END = 58;    // ...and at full pressure
    public static final int ALIEN_WAVE_GAP_JITTER = 35; // random slack added to every gap

    // Enemy planes. Voxel warplanes that fly a straight line across the field
    // and shoot back, on their own slower cadence than the alien waves.
    public static final int PLANE_SIZE = 64; // sprite fits in this box, px
    public static final int PLANE_HULL = 2;  // two default bullets, or one bullet-flower hit
    public static final int PLANE_SPEED = 2; // px per frame
    // Same trigger rhythm as the player's starting gun, and a bullet slow
    // enough to dodge — plane fire is aimed, so it does not need the player's
    // 20 px/frame to be dangerous.
    public static final int PLANE_FIRE_COOLDOWN = PLAYER_SHOT_COOLDOWN;
    public static final int PLANE_SHOT_SPEED = 7; // px per frame along the aim line
    public static final int PLANE_SHOT_SCALE = SCALE_FACTOR * 2; // bullet art scale
    public static final Color PLANE_SHOT_COLOR = new Color(255, 80, 40); // hostile red
    // Plane cadence rides the same pressure ramp as the alien waves. Planes
    // are the main source of pressure once they start: the alien gaps only
    // tightened a little, these a lot.
    // Stage 1 holds them back for its first PLANE_FIRST_WAVE_FRAME so the
    // opening stays a warm-up; stage 2 starts them immediately (Stage.firstPlaneFrame).
    public static final int PLANE_FIRST_WAVE_FRAME = 1800; // first plane of the run (0:30)
    public static final int PLANE_GAP_START = 150; // frames between planes at zero pressure
    public static final int PLANE_GAP_END = 90;    // ...and at full pressure
    public static final int PLANE_GAP_JITTER = 50; // random slack added to every gap

    // Power-ups
    public static final int POWERUP_SIZE = 40; // pickup sprite box, px
    public static final int POWERUP_DRIFT_SPEED = 2; // px per frame, right to left
    // Rare, but on a steady rhythm — there is always a drop worth waiting for.
    public static final int POWERUP_FIRST_SECONDS = 40; // first random drop
    public static final int POWERUP_MIN_SECONDS = 35;   // gap between random drops
    public static final int POWERUP_MAX_SECONDS = 55;
    public static final int SHIELD_DURATION_SECONDS = 10; // golden shield lifetime
    public static final int TIPBOX_SECONDS = 12; // how long the power-up tip box shows at start
    public static final int TIP_ICON_SIZE = 22; // power-up icons inside that box

    // Bullet flower: swaps the bullet for the heavier shot2, which travels
    // faster, does double damage, and can be fired twice back to back before
    // the normal reload kicks in.
    public static final int BULLET_DAMAGE = 2;
    public static final int BULLET_SHOTS_PER_BURST = 2;
    public static final int BULLET_BURST_GAP_FRAMES = 12; // pause between the two burst shots
    public static final int BULLET_SHOT_SPEED_BONUS = 8;
    public static final int SHOT2_WIDTH = 26;  // powered bullet is drawn to fit this box
    public static final int SHOT2_HEIGHT = 14;

    // Terrain (scrolling cave walls at the top and bottom of the playfield)
    public static final int TERRAIN_TILE = 32; // wall block size in px
    public static final int TERRAIN_MIN_GAP = 288; // guaranteed flyable corridor height in px
    public static final int TERRAIN_MAX_WALL_TILES = 8; // tallest a single wall can grow

    // Audio
    public static final float DEFAULT_VOLUME = 0.50f; // slider position on first launch (0.0 - 1.0)
    public static final float VOLUME_STEP = 0.05f; // options menu slider increment
    // Slider position is raised to this power to get amplitude, the way a real volume
    // knob tapers. log(0.2)/log(0.5), so the middle of the bar lands on 20% amplitude.
    public static final float VOLUME_TAPER = 2.3219f;

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_Boss[] = {
        "src/images/Boss1.png", 
        "src/images/Boss2.png"
    };
    
    // Baked from the MagicaVoxel models in src/images/Plane 0*. All three are
    // drawn nose-left; ImageUtil.mirror flips the ones flying the other way.
    public static final String IMG_PLANE_1 = "src/images/plane1.png";
    public static final String IMG_PLANE_2 = "src/images/plane2.png";
    public static final String IMG_PLANE_3 = "src/images/plane3.png";
    public static final String IMG_PLAYER = "src/images/ship1.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_SHOT2 = "src/images/shot2.png"; // bullet-flower bullet
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";

    //powerups spinning frames
    public static final String IMG_POWERUP_SPEEDUP[] = {
        "src/images/powerUps/speed/speed1.png",
        "src/images/powerUps/speed/speed2.png",
        "src/images/powerUps/speed/speed3.png",
        "src/images/powerUps/speed/speed4.png",
        "src/images/powerUps/speed/speed5.png",
        "src/images/powerUps/speed/speed6.png",
        "src/images/powerUps/speed/speed7.png",
        "src/images/powerUps/speed/speed8.png",
        "src/images/powerUps/speed/speed9.png",
        "src/images/powerUps/speed/speed10.png",
        "src/images/powerUps/speed/speed11.png",
        "src/images/powerUps/speed/speed12.png",
        "src/images/powerUps/speed/speed13.png",
        "src/images/powerUps/speed/speed14.png",
        "src/images/powerUps/speed/speed15.png",
        "src/images/powerUps/speed/speed16.png",
    };

    public static final String IMG_POWERUP_SHIELD[] = {
        "src/images/powerUps/shield/shield1.png",
        "src/images/powerUps/shield/shield2.png",
        "src/images/powerUps/shield/shield3.png",
        "src/images/powerUps/shield/shield4.png",
        "src/images/powerUps/shield/shield5.png",
        "src/images/powerUps/shield/shield6.png",
        "src/images/powerUps/shield/shield7.png",
        "src/images/powerUps/shield/shield8.png",
        "src/images/powerUps/shield/shield9.png",
        "src/images/powerUps/shield/shield10.png",
        "src/images/powerUps/shield/shield11.png",
        "src/images/powerUps/shield/shield12.png",
        "src/images/powerUps/shield/shield13.png",
        "src/images/powerUps/shield/shield14.png",
        "src/images/powerUps/shield/shield15.png",
        "src/images/powerUps/shield/shield16.png",
    };

    public static final String IMG_POWERUP_BULLET[] = {
        "src/images/powerUps/shots/shot1.png",
        "src/images/powerUps/shots/shot2.png",
        "src/images/powerUps/shots/shot3.png",
        "src/images/powerUps/shots/shot4.png",
        "src/images/powerUps/shots/shot5.png",
        "src/images/powerUps/shots/shot6.png",
        "src/images/powerUps/shots/shot7.png",
        "src/images/powerUps/shots/shot8.png",
        "src/images/powerUps/shots/shot9.png",
        "src/images/powerUps/shots/shot10.png",
        "src/images/powerUps/shots/shot11.png",
        "src/images/powerUps/shots/shot12.png",
        "src/images/powerUps/shots/shot13.png",
        "src/images/powerUps/shots/shot14.png",
        "src/images/powerUps/shots/shot15.png",
        "src/images/powerUps/shots/shot16.png",
    };

    //new Shots
    public static final String IMG_bolt[] = {
        "src/images/shots/bolt1.png",
        "src/images/shots/bolt2.png",
        "src/images/shots/bolt3.png",
        "src/images/shots/bolt4.png",
    };

    public static final String IMG_charge[] = {
        "src/images/shots/charged1.png",
        "src/images/shots/charged2.png",
        "src/images/shots/charged3.png",
        "src/images/shots/charged4.png",
        "src/images/shots/charged5.png",
        "src/images/shots/charged6.png",
    };
    //Added Sprites
    public static final String[] IMG_EXPLOSIONS_Frames = {
        "src/images/Explosion_sprites/hits-2-1.png",
        "src/images/Explosion_sprites/hits-2-2.png",
        "src/images/Explosion_sprites/hits-2-3.png",
        "src/images/Explosion_sprites/hits-2-4.png",
        "src/images/Explosion_sprites/hits-2-5.png",
        "src/images/Explosion_sprites/hits-2-6.png",
        "src/images/Explosion_sprites/hits-2-7.png",
    };

    //Audiio Paths
    public static final String Explosion_sfx = "src/audio/Explosion.wav";
    public static final String GameOver_sfx = "src/audio/Fallen in Battle.wav";
    public static final String Victory_sfx = "src/audio/Victory! All Clear.wav";
    public static final String Title_sfx = "src/audio/Title Theme.wav";
    public static final String Stage1_sfx = "src/audio/Fields of Ice.wav";
    public static final String Stage2_sfx = "src/audio/Zero Respect.wav";
}
