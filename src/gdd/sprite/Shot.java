package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    public Shot() {
    }

    // (tipX, tipY) is the tip of the ship: the right edge, vertically centered.
    public Shot(int tipX, int tipY) {

        initShot(tipX, tipY);
    }

    private void initShot(int tipX, int tipY) {

        var ii = new ImageIcon(IMG_SHOT);
        int sw = ii.getIconWidth() * SCALE_FACTOR;
        int sh = ii.getIconHeight() * SCALE_FACTOR;

        // Scale first (force a fully-loaded image so we can draw its pixels).
        Image scaled = new ImageIcon(
                ii.getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH)).getImage();

        // Rotate 90 degrees clockwise so the bullet points to the right.
        // A 90-degree rotation swaps width and height.
        BufferedImage rotated = new BufferedImage(sh, sw, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.translate(sh, 0);
        g2.rotate(Math.toRadians(90));
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        setImage(rotated);

        // Anchor at the ship's tip: left edge at the tip, centered on it vertically.
        // (rotated image is sh wide and sw tall)
        setX(tipX);
        setY(tipY - sw / 2);
    }
}
