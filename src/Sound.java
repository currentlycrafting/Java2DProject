import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Sound {
    private Clip clip;

    /**
     * Sets the sound file for playback using a direct path within the src folder.
     */
    public void setFile(String filePath) {
        try {
            // Load the file from the src directory
            File file = new File("src/" + filePath);
            if (!file.exists()) {
                throw new IOException("Sound file not found: " + file.getAbsolutePath());
            }

            AudioInputStream sound = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(sound);
        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.setFramePosition(0); // Start from the beginning
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}