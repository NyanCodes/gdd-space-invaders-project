package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TitleScene extends JPanel {

    private static final String[] MENU_ITEMS = {"START", "OPTIONS", "EXIT"};
    private static final int MENU_START = 0;
    private static final int MENU_OPTIONS = 1;
    private static final int MENU_EXIT = 2;

    private static final int MENU_TOP_Y = 520;
    private static final int MENU_SPACING = 40;

    private int frame = 0;
    private Image image;
    private AudioPlayer audioPlayer;
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private Timer timer;
    private Game game;

    private int selectedIndex = MENU_START;
    private boolean showingOptions = false;

    public TitleScene(Game game) {
        this.game = game;
        // initBoard();
        // initTitle();
    }

    private void initBoard() {

    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        initTitle();
        initAudio();
    }

    public void stop() {
        try {
            if (timer != null) {
                timer.stop();
            }

            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void initTitle() {
        var ii = new ImageIcon(IMG_TITLE);
        image = ii.getImage();

    }

    private void initAudio() {
        try {
            String filePath = "src/audio/title.wav";
            audioPlayer = new AudioPlayer(filePath);

            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error with playing sound.");
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        final Font baseFont = g.getFont();

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        g.drawImage(image, 0, -80, d.width, d.height, this);

        if (showingOptions) {
            drawOptions(g, baseFont);
        } else {
            drawMenu(g, baseFont);
        }

        g.setColor(Color.gray);
        g.setFont(baseFont.deriveFont(10f));
        g.drawString("Game by Chayapol", 10, 650);

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawMenu(Graphics g, Font baseFont) {

        g.setFont(baseFont.deriveFont(Font.BOLD, 28f));

        for (int i = 0; i < MENU_ITEMS.length; i++) {

            String text = MENU_ITEMS[i];

            if (i == selectedIndex) {
                text = "> " + text + " <";
                g.setColor(frame % 60 < 30 ? Color.red : Color.white);
            } else {
                g.setColor(Color.gray);
            }

            drawCentered(g, text, MENU_TOP_Y + i * MENU_SPACING);
        }

        g.setColor(Color.darkGray);
        g.setFont(baseFont.deriveFont(12f));
        drawCentered(g, "UP / DOWN to select    ENTER to confirm",
                MENU_TOP_Y + MENU_ITEMS.length * MENU_SPACING);
    }

    private void drawOptions(Graphics g, Font baseFont) {

        final int panelWidth = 440;
        final int panelHeight = 180;
        final int panelX = (d.width - panelWidth) / 2;
        final int panelY = 440;

        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(panelX, panelY, panelWidth, panelHeight);
        g.setColor(Color.white);
        g.drawRect(panelX, panelY, panelWidth, panelHeight);

        g.setFont(baseFont.deriveFont(Font.BOLD, 24f));
        drawCentered(g, "OPTIONS", panelY + 40);

        float volume = AudioPlayer.getMasterVolume();

        g.setFont(baseFont.deriveFont(16f));
        g.drawString("VOLUME", panelX + 30, panelY + 85);
        String percent = Math.round(volume * 100) + "%";
        g.drawString(percent, panelX + panelWidth - 30 - g.getFontMetrics().stringWidth(percent),
                panelY + 85);

        final int trackX = panelX + 30;
        final int trackY = panelY + 100;
        final int trackWidth = panelWidth - 60;
        final int trackHeight = 14;
        final int fillWidth = Math.round(trackWidth * volume);

        g.setColor(Color.darkGray);
        g.fillRect(trackX, trackY, trackWidth, trackHeight);
        g.setColor(Color.red);
        g.fillRect(trackX, trackY, fillWidth, trackHeight);
        g.setColor(Color.white);
        g.drawRect(trackX, trackY, trackWidth, trackHeight);
        g.fillRect(trackX + fillWidth - 2, trackY - 5, 5, trackHeight + 11);

        g.setColor(Color.gray);
        g.setFont(baseFont.deriveFont(12f));
        drawCentered(g, "LEFT / RIGHT to adjust    ESC to go back", panelY + panelHeight - 20);
    }

    private void drawCentered(Graphics g, String text, int y) {
        int x = (d.width - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void update() {
        frame++;
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void handleMenuKey(int key) {
        switch (key) {
            case KeyEvent.VK_UP:
                selectedIndex = (selectedIndex + MENU_ITEMS.length - 1) % MENU_ITEMS.length;
                break;
            case KeyEvent.VK_DOWN:
                selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                activateSelection();
                break;
        }
    }

    private void activateSelection() {
        switch (selectedIndex) {
            case MENU_START:
                game.loadScene2();
                break;
            case MENU_OPTIONS:
                showingOptions = true;
                break;
            case MENU_EXIT:
                stop();
                System.exit(0);
                break;
        }
    }

    private void handleOptionsKey(int key) {
        switch (key) {
            case KeyEvent.VK_LEFT:
                AudioPlayer.setMasterVolume(AudioPlayer.getMasterVolume() - VOLUME_STEP);
                break;
            case KeyEvent.VK_RIGHT:
                AudioPlayer.setMasterVolume(AudioPlayer.getMasterVolume() + VOLUME_STEP);
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                showingOptions = false;
                break;
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (showingOptions) {
                handleOptionsKey(e.getKeyCode());
            } else {
                handleMenuKey(e.getKeyCode());
            }
        }
    }
}
