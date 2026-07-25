package gdd.scene;

import gdd.Game;
import gdd.Stage;

/**
 * Level 2: four minutes, a tighter cave, four bosses.
 *
 * All the behaviour lives in Scene1 — this only picks the stage data
 * (Stage.TWO), so a fix to the game loop applies to both levels.
 */
public class Scene2 extends Scene1 {

    public Scene2(Game game) {
        super(game, Stage.TWO);
    }
}
