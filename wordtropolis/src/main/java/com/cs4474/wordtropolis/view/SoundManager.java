package com.cs4474.wordtropolis.view;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Utility class for playing WAV sound effects.
 *
 * How to use:
 *   SoundManager.play("correct.wav");
 *   SoundManager.play("meow.wav");
 *   SoundManager.play("cheer.wav");
 *
 * Drop your .wav files into:
 *   src/main/resources/com/cs4474/wordtropolis/sounds/
 *
 * Silently no-ops if a file is missing — the game always works without audio.
 */
public final class SoundManager {

    private static int sfxVolume = 80;  // 0–100

    private SoundManager() {}

    public static void play(String filename) {
        if (sfxVolume == 0) return;
        try {
            URL res = SoundManager.class.getResource(
                    "/com/cs4474/wordtropolis/sounds/" + filename);
            if (res == null) return;   // asset not yet added

            AudioInputStream ais = AudioSystem.getAudioInputStream(res);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl vol = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float gain = 20f * (float) Math.log10(sfxVolume / 100.0);
                vol.setValue(Math.max(vol.getMinimum(), gain));
            }

            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            // Silent fail — game continues without sound
        }
    }

    public static void setSfxVolume(int v) { sfxVolume = Math.max(0, Math.min(100, v)); }
    public static int  getSfxVolume()      { return sfxVolume; }
}
