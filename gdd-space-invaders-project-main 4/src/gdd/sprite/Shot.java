package gdd.sprite;

import static gdd.Global.*;
import gdd.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    public Shot() {
    }

    // (tipX, tipY) is the tip of the ship: the right edge, vertically centered.
    public Shot(int tipX, int tipY) {

        this(tipX, tipY, false);
    }

    /**
     * powered = the bullet-flower shot: the heavier shot2 sprite. Damage and
     * travel speed live on the Player, this only picks the artwork.
     */
    public Shot(int tipX, int tipY, boolean powered) {

        initShot(tipX, tipY, powered);
    }

    private void initShot(int tipX, int tipY, boolean powered) {

        Image bullet = powered ? poweredImage() : plainImage();
        setImage(bullet);

        // Anchor at the ship's tip: left edge at the tip, centered on it vertically.
        setX(tipX);
        setY(tipY - bullet.getHeight(null) / 2);
    }

    // The default bullet is a tiny vertical sprite, so it is scaled up and
    // then rotated 90 degrees clockwise to point to the right.
    private Image plainImage() {

        var ii = new ImageIcon(IMG_SHOT);
        int sw = ii.getIconWidth() * SCALE_FACTOR;
        int sh = ii.getIconHeight() * SCALE_FACTOR;

        // Scale first (force a fully-loaded image so we can draw its pixels).
        Image scaled = new ImageIcon(
                ii.getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH)).getImage();

        // A 90-degree rotation swaps width and height.
        BufferedImage rotated = new BufferedImage(sh, sw, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.translate(sh, 0);
        g2.rotate(Math.toRadians(90));
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return rotated;
    }

    // shot2 already points sideways, so it only needs trimming and scaling.
    private Image poweredImage() {

        return ImageUtil.fit(IMG_SHOT2, SHOT2_WIDTH, SHOT2_HEIGHT);
    }
}
