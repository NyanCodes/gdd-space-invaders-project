package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame  {

    TitleScene titleScene;
    Scene1 scene1;

    // Legacy Stage 2, restored verbatim from gdd-space-invaders-project-stage2_1.zip
    // (aliens/obstacles/its own Boss). It's a standalone JPanel with its own
    // start()/stop() rather than a Scene1 subclass, so it's wired in directly
    // here instead of through Scene1's shared stage array.
    Scene2 scene2;

    private JPanel currentScene;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
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
        stopCurrent();
        getContentPane().removeAll();
        // add(new Title(this));
        add(titleScene);
        currentScene = titleScene;
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
        if (number < 1 || number > 2) {
            loadTitle();
            return;
        }
        loadStage(number, carriedScore);
    }

    private void loadStage(int number, int carriedScore) {
        stopCurrent();
        getContentPane().removeAll();

        if (number == 1) {
            scene1.setCarriedScore(carriedScore);
            add(scene1);
            currentScene = scene1;
            scene1.start();
        } else {
            // Legacy stage: self-contained, doesn't take a carried-in score
            // and returns straight to the title screen when it ends.
            add(scene2);
            currentScene = scene2;
            scene2.start();
        }

        revalidate();
        repaint();
    }

    private void stopCurrent() {
        if (currentScene == titleScene) {
            titleScene.stop();
        } else if (currentScene == scene1) {
            scene1.stop();
        } else if (currentScene == scene2) {
            scene2.stop();
        }
    }
}
