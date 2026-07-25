package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;

/**
 * Stage 2 enemy: "Hunter". Steers toward the player's current vertical
 * position every frame (capped so it can't teleport), instead of moving
 * in a straight line or fixed pattern - it actively closes the gap,
 * which makes it the most dangerous of the three enemy types.
 */
public class Alien3 extends Enemy {

    private static final int VSPEED = 3; // max vertical pixels/frame
    private static final int HSPEED = 2; // horizontal pixels/frame

    private int targetY;

    public Alien3(int x, int y) {
        super(x, y);
        this.targetY = y;
        setImage(Alien2.tint(IMG_ENEMY, new Color(255, 70, 60), SCALE_FACTOR));
    }

    /** The scene calls this each frame with the player's current y before act(). */
    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    @Override
    public void act(int direction) {
        this.x -= HSPEED;

        int dy = targetY - this.y;
        if (dy > VSPEED) {
            dy = VSPEED;
        }
        if (dy < -VSPEED) {
            dy = -VSPEED;
        }
        this.y += dy;

        if (this.x < -ALIEN_WIDTH * SCALE_FACTOR) {
            die();
        }
    }
}
