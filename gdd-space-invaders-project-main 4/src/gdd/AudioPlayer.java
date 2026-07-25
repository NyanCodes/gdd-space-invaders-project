// Java program to play an Audio
// file using Clip Object
package gdd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

    private static final Preferences PREFS = Preferences.userNodeForPackage(AudioPlayer.class);
    private static final String PREF_VOLUME = "masterVolume";

    // Master volume shared by every scene's music, changed from the options menu and
    // remembered across launches. Held as the slider position (0.0 - 1.0), not as
    // amplitude; applyVolume() tapers it. Open clips are tracked so a change applies
    // while they play.
    private static float masterVolume = clampVolume(PREFS.getFloat(PREF_VOLUME, Global.DEFAULT_VOLUME));
    private static final List<AudioPlayer> OPEN_PLAYERS = new CopyOnWriteArrayList<>();

    static {
        // Covers both ways out of the game: the EXIT menu item and the window close box.
        Runtime.getRuntime().addShutdownHook(new Thread(AudioPlayer::saveVolume));
    }

    // to store current position
    Long currentFrame;
    Clip clip;

    // current status of clip
    String status;

    AudioInputStream audioInputStream;
    String filePath;

    // constructor to initialize streams and clip
    public AudioPlayer(String filePath)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        // create AudioInputStream object
        this.filePath = filePath;
        audioInputStream
                = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // create clip reference
        clip = AudioSystem.getClip();

        // open audioInputStream to the clip
        clip.open(audioInputStream);

        OPEN_PLAYERS.add(this);
        applyVolume();

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(float volume) {
        masterVolume = clampVolume(volume);
        PREFS.putFloat(PREF_VOLUME, masterVolume);
        for (AudioPlayer player : OPEN_PLAYERS) {
            player.applyVolume();
        }
    }

    private static float clampVolume(float volume) {
        return Math.max(0f, Math.min(1f, volume));
    }

    private static void saveVolume() {
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            System.err.println("Could not save volume setting: " + e.getMessage());
        }
    }

    // Amplitude tapers off the slider position, so the quiet end of the bar gets the
    // finer control. A line's gain is in decibels, hence the log; position 0 snaps to
    // the line's minimum, which is silence.
    private void applyVolume() {
        if (clip == null || !clip.isOpen()) {
            return;
        }

        float amplitude = (float) Math.pow(masterVolume, Global.VOLUME_TAPER);

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float decibels = masterVolume <= 0f
                    ? gain.getMinimum()
                    : (float) (Math.log10(amplitude) * 20.0);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
        } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            // Fallback control, already linear over its own range.
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            float span = volume.getMaximum() - volume.getMinimum();
            volume.setValue(volume.getMinimum() + span * amplitude);
        }
    }

    public static void main(String[] args) {
        try {
            String filePath = "src/audio/title.wav";
            AudioPlayer audioPlayer = new AudioPlayer(filePath);

            audioPlayer.play();
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("1. pause");
                System.out.println("2. resume");
                System.out.println("3. restart");
                System.out.println("4. stop");
                System.out.println("5. Jump to specific time");
                int c = sc.nextInt();
                audioPlayer.gotoChoice(c);
                if (c == 4) {
                    break;
                }
            }
            sc.close();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();

        }
    }

    // Work as the user enters his choice
    private void gotoChoice(int c)
            throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        switch (c) {
            case 1:
                pause();
                break;
            case 2:
                resumeAudio();
                break;
            case 3:
                restart();
                break;
            case 4:
                stop();
                break;
            case 5:
                System.out.println("Enter time (" + 0
                        + ", " + clip.getMicrosecondLength() + ")");
                Scanner sc = new Scanner(System.in);
                long c1 = sc.nextLong();
                jump(c1);
                break;

        }

    }

    // Method to play the audio
    public void play() {
        //start the clip
        clip.start();

        status = "play";
    }


    // Method to pause the audio
    public void pause() {
        if (status.equals("paused")) {
            System.out.println("audio is already paused");
            return;
        }
        this.currentFrame
                = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    // Method to resume the audio
    public void resumeAudio() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        if (status.equals("play")) {
            System.out.println("Audio is already "
                    + "being played");
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        this.play();
    }

    // Method to restart the audio
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }

    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        currentFrame = 0L;
        clip.stop();
        clip.close();
        OPEN_PLAYERS.remove(this);
    }

    // Method to jump over a specific part
    public void jump(long c) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            this.play();
        }
    }

    // Method to reset audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(
                new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        applyVolume();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

}
