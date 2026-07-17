package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        // Set image
        ImageIcon ii = new ImageIcon(IMG_POWERUP_SPEEDUP);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() ,
                ii.getIconHeight() ,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // Horizontal side-scroller: drift left from the right edge so the
        // player can fly into it.
        this.x -= 2;
        if (this.x < -60) {
            die(); // Off-screen, clean up
        }
    }

    public void upgrade(Player player) {
        // Boost ship movement, bullet speed, and fire rate (all capped in Player).
        player.setSpeed(player.getSpeed() + 1);
        player.setShotSpeed(player.getShotSpeed() + 4);
        player.reduceShotCooldown(25);
        this.die(); // Remove the power-up after use
    }

}
