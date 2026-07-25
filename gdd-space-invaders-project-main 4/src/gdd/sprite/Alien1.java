package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private Bomb bomb;

    public Alien1(int x, int y) {
        super(x, y);
        // initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        var ii = new ImageIcon(IMG_ENEMY);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {
        // Horizontal side-scroller: aliens enter from the right and move left.
        this.x -= 2;

        // Clean up once fully off the left edge.
        if (this.x < -ALIEN_WIDTH) {
            die();
        }
    }

    public Bomb getBomb() {

        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {

            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) {

            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {

            return destroyed;
        }
    }
}
