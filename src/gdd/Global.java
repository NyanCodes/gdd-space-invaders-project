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

    // Hull points. Enemy fire chips away at this — plane collisions, plane
    // bullets and boss bullets — while a cave wall, an alien, or ramming the
    // boss itself still costs a whole life on contact.
    public static final int PLAYER_HULL = 2;
    public static final int HIT_INVINCIBLE_FRAMES = 45; // mercy blink after a plane hit

    // Score values
    public static final int ALIEN_SCORE = 1;
    public static final int PLANE_SCORE = 3;
    public static final int HEAVY_PLANE_SCORE = 6;
    public static final int ELITE_PLANE_SCORE = 10;
    public static final int BOSS_SCORE = 5;

    // Stages. Each one is a terrain map file, a pacing length, and the single
    // moment its boss arrives. A stage is cleared by killing that boss, not by
    // surviving a timer, so the boss second is picked to land in the wide
    // "arena" section of the map — move one and you must move the other.
    // This doubles as the length of the stage's difficulty ramp: pressure
    // climbs from the stage's floor to its ceiling over exactly this long, so
    // the run is at its hardest the moment the boss shows up.
    public static final String MAP_LEVEL_1 = "src/maps/level1.txt";
    public static final String MAP_LEVEL_2 = "src/maps/level2.txt";
    public static final int STAGE_1_BOSS_SECOND = 150; // 2:30
    public static final int STAGE_2_BOSS_SECOND = 180; // 3:00

    // Boss. One per stage, and killing it is what clears the stage — so it is
    // a real fight: 24 default bullets, or 12 with the bullet flower.
    public static final int BOSS_HP = 24;
    // Boss fire. It shoots on a tighter interval than the planes, and a hit
    // costs a hull point exactly like plane fire does. Stage 1's boss lobs one
    // aimed bullet, fast enough that standing still is not an option; stage 2's
    // fires a three-way spread on the same beat, which is three times the
    // volume rather than three times the rate.
    public static final int BOSS_FIRE_COOLDOWN = 60;   // frames between volleys (1s)
    public static final int BOSS_SHOT_SPEED = 11;      // px per frame, vs PLANE_SHOT_SPEED 7
    public static final int BOSS_SPREAD_COUNT = 3;     // bullets in a stage 2 volley
    public static final int BOSS_SPREAD_DEGREES = 45;  // angle between them
    public static final int BOSS_SHOT_SIZE = 30;       // bullet art fits this box, px
    // A beat between the killing blow and the clear screen, so the player sees
    // the boss go up rather than the screen flipping mid-explosion.
    public static final int STAGE_CLEAR_DELAY_FRAMES = 60; // 1s

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

    // The two heavier models (plane5 / plane6). Like PLANE_SIZE these are a
    // maximum box, not a forced size — ImageUtil.fit only ever scales down, so
    // a roomier box just lets the bigger art through at full size and the
    // heavies read as bulkier than the light traffic. Their hulls are set in
    // bullet-flower hits: 2 for the heavy, 4 for the elite.
    // (BULLET_DAMAGE is 2, declared further down — spelled out here because a
    // static field can't be referenced before it is declared.)
    public static final int HEAVY_PLANE_SIZE = 78;
    public static final int HEAVY_PLANE_HULL = 4; // 2 bullet-flower hits
    public static final int ELITE_PLANE_SIZE = 86;
    public static final int ELITE_PLANE_HULL = 8; // 4 bullet-flower hits
    // When each plane tier joins the traffic, indexed by tier: element 0 is the
    // light models, 1 the heavy, 2 the elite. A stage simply doesn't field a
    // tier it has no entry for, so this is both the schedule and the roster.
    //
    // Stage 2 inherits stage 1's ending pressure, so opening it at full tilt
    // made its first minute harder than its boss. It gets a plane-free start,
    // then light traffic, then heavies, then elites.
    public static final int[] STAGE_1_PLANE_TIER_FRAMES = {0, 60 * 60};
    public static final int[] STAGE_2_PLANE_TIER_FRAMES = {0, 30 * 60, 75 * 60};
    public static final int STAGE_2_FIRST_PLANE_FRAME = 15 * 60; // 0:15
    // How much of the plane traffic the heavier models take. They enter the
    // pool on equal footing and climb to this many times the share of a light
    // plane as the stage's pressure ramp tops out.
    public static final int HEAVY_PLANE_MAX_WEIGHT = 3;
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

    // The red heart: one extra life, capped at PLAYER_LIVES. It runs on its
    // own timer rather than joining the drop pool, so adding it doesn't make
    // the speed / shield / gun flowers any rarer.
    public static final int HEART_FIRST_SECONDS = 45;
    public static final int HEART_MIN_SECONDS = 60;
    public static final int HEART_MAX_SECONDS = 90;
    // Lives are hard-capped, so a heart caught at full lives is wasted. At
    // full health the drop is usually held back — 1 in this many still comes
    // through, which keeps them part of the scenery and gives the player
    // something to run for if they get hit while one is drifting past.
    public static final int HEART_FULL_LIVES_CHANCE = 4;
    // How soon to reconsider after holding one back. Short, so a heart turns
    // up quickly once the player actually loses a life.
    public static final int HEART_RETRY_SECONDS = 12;
    public static final int TIPBOX_SECONDS = 12; // how long the power-up tip box shows at start
    public static final int TIP_ICON_SIZE = 22; // power-up icons inside that box

    // The gun ladder (see gdd.GunTier). Every rung above the first fires the
    // same double-damage bullet; what a rung buys is more shots before the
    // reload. Bullets get a little bigger each rung so the upgrade reads on
    // screen as well as on the HUD.
    public static final int BULLET_DAMAGE = 2;
    public static final int BULLET_SHOTS_PER_BURST = 2;  // bullet flower
    public static final int BOLT_SHOTS_PER_BURST = 4;    // bolt flower
    public static final int CHARGED_SHOTS_PER_BURST = 6; // charged flower
    public static final int BULLET_BURST_GAP_FRAMES = 12; // pause between burst shots
    public static final int BULLET_SHOT_SPEED_BONUS = 8;
    public static final int BULLET_ANIM_FRAMES = 4; // game frames per bullet art frame
    public static final int SHOT2_WIDTH = 26;  // powered bullet is drawn to fit this box
    public static final int SHOT2_HEIGHT = 14;
    public static final int BOLT_WIDTH = 30;
    public static final int BOLT_HEIGHT = 20;
    public static final int CHARGED_WIDTH = 36;
    public static final int CHARGED_HEIGHT = 28;

    // How hard an enemy may pull to stay inside the corridor, px per frame.
    // Enemies fly around the rock rather than dying in it, but only this fast,
    // so a wall closing in reads as the plane banking away from it instead of
    // the sprite snapping to a new height.
    public static final int ENEMY_DODGE_SPEED = 3;
    // Clearance an enemy tries to hold off the rock face, px. A little margin
    // keeps them from grazing the surface pixel-perfectly all the way along.
    public static final int ENEMY_DODGE_MARGIN = 6;
    // How far along the corridor an enemy watches. Without this it would only
    // react once the rock was already on top of it; a tile of warning is what
    // lets the dodge start early enough to work.
    public static final int ENEMY_DODGE_LOOKAHEAD = 32;

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
    // The two heavier models, baked from src/images/Boss_BackUp_Plane. Named
    // for their source files (Plane05 / Plane_06), so there is no plane4.
    public static final String IMG_PLANE_5 = "src/images/plane5.png";
    public static final String IMG_PLANE_6 = "src/images/plane6.png";
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

    // Boss fire: a spinning orb, drawn symmetrically so it reads correctly at
    // any heading (stage 2's boss fires a spread, not a straight line).
    public static final String IMG_BOSS_SHOT[] = {
        "src/images/Boss_shots/crossed1.png",
        "src/images/Boss_shots/crossed2.png",
        "src/images/Boss_shots/crossed3.png",
        "src/images/Boss_shots/crossed4.png",
        "src/images/Boss_shots/crossed5.png",
        "src/images/Boss_shots/crossed6.png",
    };

    // The extra-life heart, spun about its vertical axis (tools/make_heart_pickup.py).
    public static final String IMG_POWERUP_HEART[] = {
        "src/images/powerUps/heart/heart1.png",
        "src/images/powerUps/heart/heart2.png",
        "src/images/powerUps/heart/heart3.png",
        "src/images/powerUps/heart/heart4.png",
        "src/images/powerUps/heart/heart5.png",
        "src/images/powerUps/heart/heart6.png",
        "src/images/powerUps/heart/heart7.png",
        "src/images/powerUps/heart/heart8.png",
        "src/images/powerUps/heart/heart9.png",
        "src/images/powerUps/heart/heart10.png",
        "src/images/powerUps/heart/heart11.png",
        "src/images/powerUps/heart/heart12.png",
        "src/images/powerUps/heart/heart13.png",
        "src/images/powerUps/heart/heart14.png",
        "src/images/powerUps/heart/heart15.png",
        "src/images/powerUps/heart/heart16.png",
    };

    // Gun-ladder pickups. The blue "2X" flower above is rung one; these two are
    // hue-rotated copies of it relabelled 4X and 6X (see tools/make_gun_pickups.py).
    public static final String IMG_POWERUP_BOLT[] = {
        "src/images/powerUps/bolt/bolt1.png",
        "src/images/powerUps/bolt/bolt2.png",
        "src/images/powerUps/bolt/bolt3.png",
        "src/images/powerUps/bolt/bolt4.png",
        "src/images/powerUps/bolt/bolt5.png",
        "src/images/powerUps/bolt/bolt6.png",
        "src/images/powerUps/bolt/bolt7.png",
        "src/images/powerUps/bolt/bolt8.png",
        "src/images/powerUps/bolt/bolt9.png",
        "src/images/powerUps/bolt/bolt10.png",
        "src/images/powerUps/bolt/bolt11.png",
        "src/images/powerUps/bolt/bolt12.png",
        "src/images/powerUps/bolt/bolt13.png",
        "src/images/powerUps/bolt/bolt14.png",
        "src/images/powerUps/bolt/bolt15.png",
        "src/images/powerUps/bolt/bolt16.png",
    };

    public static final String IMG_POWERUP_CHARGED[] = {
        "src/images/powerUps/charged/charged1.png",
        "src/images/powerUps/charged/charged2.png",
        "src/images/powerUps/charged/charged3.png",
        "src/images/powerUps/charged/charged4.png",
        "src/images/powerUps/charged/charged5.png",
        "src/images/powerUps/charged/charged6.png",
        "src/images/powerUps/charged/charged7.png",
        "src/images/powerUps/charged/charged8.png",
        "src/images/powerUps/charged/charged9.png",
        "src/images/powerUps/charged/charged10.png",
        "src/images/powerUps/charged/charged11.png",
        "src/images/powerUps/charged/charged12.png",
        "src/images/powerUps/charged/charged13.png",
        "src/images/powerUps/charged/charged14.png",
        "src/images/powerUps/charged/charged15.png",
        "src/images/powerUps/charged/charged16.png",
    };

    // Bullet art for the top two rungs of the gun ladder — animated projectile
    // trails, already drawn pointing right the way the player fires.
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
