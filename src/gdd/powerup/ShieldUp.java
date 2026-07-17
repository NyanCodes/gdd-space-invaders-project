package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class ShieldUp extends PowerUp {

    // The source PNG is huge (2048px); draw the pickup at this size instead.
    private static final int SIZE = 40;

    public ShieldUp(int x, int y) {
        super(x, y);
        ImageIcon ii = new ImageIcon(IMG_POWERUP_SHIELD);
        var scaledImage = ii.getImage().getScaledInstance(SIZE, SIZE,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // Drift left from the right edge so the player can fly into it.
        this.x -= 2;
        if (this.x < -60) {
            die(); // Off-screen, clean up
        }
    }

    public void upgrade(Player player) {
        player.activateShield(SHIELD_DURATION_SECONDS * 60);
        this.die(); // Remove the power-up after use
    }
}
