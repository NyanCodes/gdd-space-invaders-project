package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Explosion extends Sprite {

    private Image[] frames;
    private int currentFrame = 0;

    private int animationCounter = 0;
    private int animationSpeed = 3;

    private int holdCounter = 0;
    private int holdFrames = 10;

    private boolean finished = false;


    public Explosion(int x, int y) {

        initExplosion(x, y);
    }

    private void initExplosion(int x, int y) {

        this.x = x;
        this.y = y;

        frames = new Image[IMG_EXPLOSIONS_Frames.length];

        for (int i = 0; i < IMG_EXPLOSIONS_Frames.length; i++) {
            ImageIcon ii = new ImageIcon(IMG_EXPLOSIONS_Frames[i]);

            Image scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                Image.SCALE_SMOOTH
            );

            frames[i] = scaledImage;
        }

        setImage(frames[0]);
    }

    public void act(int direction) {

        // this.x += direction;
        if (finished) {
            return;
        }

        if (currentFrame < frames.length - 1) {
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                animationCounter = 0;
                currentFrame++;
                setImage(frames[currentFrame]);
            }
        }
        else {
            holdCounter++;
            if (holdCounter >= holdFrames) {
                finished = true;
                setVisible(false);
            }
        }
    }

    public Boolean isFinished() {
        return finished;
    }

}
