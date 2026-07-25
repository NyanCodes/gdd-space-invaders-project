package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import javax.swing.JFrame;

public class Game extends JFrame  {

    TitleScene titleScene;
    Scene1 scene1;
    Scene2 scene2;

    // Playable stages in order — loadNextStage() indexes into this, so adding
    // a level means adding its scene here and a Stage constant.
    private Scene1[] stages;
    private Scene1 currentScene;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
        stages = new Scene1[]{scene1, scene2};
        initUI();
        loadTitle();
    }

    private void initUI() {

        setTitle("Space Invaders");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

    }

    public void loadTitle() {
        getContentPane().removeAll();
        // add(new Title(this));
        add(titleScene);
        titleScene.start();
        revalidate();
        repaint();
    }

    /** START on the title menu: begin a fresh run at stage 1. */
    public void startGame() {
        titleScene.stop();
        loadStage(1, 0);
    }

    /**
     * Called by a scene that has just been cleared. Falls back to the title
     * screen if there is no such stage, so a missing level can't hard-lock.
     */
    public void loadNextStage(int number, int carriedScore) {
        if (number < 1 || number > stages.length) {
            if (currentScene != null) {
                currentScene.stop();
            }
            loadTitle();
            return;
        }
        loadStage(number, carriedScore);
    }

    private void loadStage(int number, int carriedScore) {
        Scene1 next = stages[number - 1];
        next.setCarriedScore(carriedScore);

        if (currentScene != null) {
            currentScene.stop();
        }
        getContentPane().removeAll();
        add(next);
        currentScene = next;
        next.start();
        revalidate();
        repaint();
    }
}
