package com.cs4474.wordtropolis.view;

import javax.sound.sampled.*;
import java.net.URL;
import javax.swing.Timer;

/**
 *  CatSoundManager.java  —  Used in CatGamePanel.java
 */
public final class BurglarSoundManager {

    // ── Volume (0–100) ────────────────────────────────────────────────────────
    private static int sfxVolume   = 80;
    private static int musicVolume = 25;  // kept quieter so it doesn't drown SFX

    // ── Background music clip kept alive for looping ──────────────────────────
    private static Clip bgMusicClip;
    private static Timer bgLoopTimer;

    private BurglarSoundManager() {}

    // ── Sound file name constants ─────────────────────────────────────────────

    /** Magical Soft Chime (4th/5th sound) — played on correct word */
    public static final String SFX_CORRECT        = "magical_soft_chime.wav";

    /** Gentle Error Tone — played on wrong word, encourages retry */
    public static final String SFX_ERROR          = "gentle_error_tone.wav";

    /** Box Effect — played when player clicks a letter tile */
    public static final String SFX_TILE_CLICK     = "box_effect.wav";

    /** Completion Sound — played when all words are solved */
    public static final String SFX_LEVEL_COMPLETE = "completion_sound.wav";

    /** Game Start — played when player clicks Start Rescue */
    public static final String SFX_GAME_START     = "game_start.wav";

    /** Byte Blast — background music, loops quietly throughout */
    public static final String BGM_BACKGROUND     = "chase.wav";

    // ── One-shot SFX ──────────────────────────────────────────────────────────

    /**
     * Play a sound effect once.
     * Silently does nothing if the file is missing — game keeps running.
     *
     * Usage:
     *   CatSoundManager.play(CatSoundManager.SFX_CORRECT);
     */
    public static void play(String filename) {
        if (sfxVolume == 0) return;
        try {
            URL res = CatSoundManager.class.getResource("/sounds/cat_game/" + filename);
            if (res == null) {
                System.out.println("[CatSoundManager] Not found: " + filename);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(res);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyVolume(clip, sfxVolume);
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception ex) {
            System.out.println("[CatSoundManager] Could not play: " + filename);
        }
    }

    /** Start looping the background music with smooth fade at the loop point. */
    public static void startMusic() {
        stopMusic();
        if (musicVolume == 0) return;
        try {
            URL res = CatSoundManager.class.getResource("/sounds/burglar_game/" + BGM_BACKGROUND);
            if (res == null) {
                System.out.println("[BurglarSoundManager] BGM not found: " + BGM_BACKGROUND);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(res);
            bgMusicClip = AudioSystem.getClip();
            bgMusicClip.open(ais);
            applyVolume(bgMusicClip, musicVolume);

            // ── How many seconds to cut off the end ───────────────────────────
            double cutOffSec  = 3;   // stop 2 seconds before the actual end
            double fadeSec    = 0.1;   // start fading 0.8 seconds before cutoff point

            long clipLength   = bgMusicClip.getMicrosecondLength();
            long cutOffMicro  = clipLength - (long)(cutOffSec * 1_000_000);
            long fadeStartMicro = cutOffMicro - (long)(fadeSec * 1_000_000);

            bgMusicClip.start();

            // ── Fade + loop timer — checks every 50ms ─────────────────────────
            Timer loopTimer = new Timer(50, null);
            loopTimer.addActionListener(e -> {
                if (bgMusicClip == null || !bgMusicClip.isOpen()) {
                    ((Timer) e.getSource()).stop();
                    return;
                }

                long pos = bgMusicClip.getMicrosecondPosition();

                // Start fading out as we approach the cutoff
                if (pos >= fadeStartMicro && pos < cutOffMicro) {
                    // How far into the fade are we? 0.0 = fade start, 1.0 = cutoff
                    double fadePct = (double)(pos - fadeStartMicro)
                                   / (cutOffMicro - fadeStartMicro);
                    // Volume goes from musicVolume down to 0
                    int fadedVol = (int)(musicVolume * (1.0 - fadePct));
                    applyVolume(bgMusicClip, fadedVol);
                }

                // At cutoff — stop, reset to beginning, fade back in
                if (pos >= cutOffMicro) {
                    bgMusicClip.stop();
                    bgMusicClip.setMicrosecondPosition(0);
                    applyVolume(bgMusicClip, 0);   // start at silent
                    bgMusicClip.start();

                    // Fade back in over 0.5 seconds
                    fadeIn(loopTimer);
                }
            });
            loopTimer.start();

            // Keep a reference so stopMusic() can kill the timer too
            bgLoopTimer = loopTimer;
            
            

        } catch (Exception ex) {
            System.out.println("[CatSoundManager] Could not start BGM.");
        }
    }

    /** Gradually raise volume from 0 to musicVolume over ~500ms. */
    private static void fadeIn(Timer loopTimer) {
        final int steps    = 20;           // 20 steps × 25ms = 500ms fade-in
        final int[] step   = {0};
        Timer fadeInTimer  = new Timer(25, null);
        fadeInTimer.addActionListener(e -> {
            step[0]++;
            if (bgMusicClip == null || !bgMusicClip.isOpen()) {
                fadeInTimer.stop();
                return;
            }
            // Volume goes from 0 up to musicVolume
            int vol = (int)(musicVolume * ((double) step[0] / steps));
            applyVolume(bgMusicClip, Math.min(vol, musicVolume));

            if (step[0] >= steps) {
                fadeInTimer.stop();
                // Resume the main loop timer now that fade-in is done
                loopTimer.start();
            }
        });
        // Pause the main loop timer during fade-in to avoid double-triggering
        loopTimer.stop();
        fadeInTimer.start();
    }

    /** Stop the background music. Called in CatGamePanel.removeNotify(). */
    public static void stopMusic() {
    if (bgLoopTimer != null) {
        bgLoopTimer.stop();       // ADD — stop the fade/loop timer
        bgLoopTimer = null;
    }
    if (bgMusicClip != null && bgMusicClip.isOpen()) {
        bgMusicClip.stop();
        bgMusicClip.close();
        bgMusicClip = null;
    }
    }
    
    /**
    * Play only a specific portion of a sound file.
    *
    * Usage examples:
    *   CatSoundManager.playFrom("correct.wav", 2.0, 4.0);  // plays 2sec to 4sec
    *   CatSoundManager.playFrom("meow_mad.wav", 0.5, 2.5); // plays 0.5sec to 2.5sec
    *
    * @param filename  the .wav file name in resources/sounds/cat_game
    * @param startSec  when to start playing (in seconds)
    * @param endSec    when to stop playing (in seconds)
    */
   public static void playFrom(String filename, double startSec, double endSec) {
       if (sfxVolume == 0) return;
       try {
           URL res = CatSoundManager.class.getResource("/sounds/cat_game/" + filename);
           if (res == null) {
               System.out.println("[CatSoundManager] Not found: " + filename);
               return;
           }
           AudioInputStream ais = AudioSystem.getAudioInputStream(res);
           Clip clip = AudioSystem.getClip();
           clip.open(ais);
           applyVolume(clip, sfxVolume);

           // Convert seconds to microseconds (Java audio uses microseconds)
           long startMicro = (long)(startSec * 1_000_000);
           long endMicro   = (long)(endSec   * 1_000_000);

           // Make sure we don't go past the end of the clip
           long clipLength = clip.getMicrosecondLength();
           if (startMicro >= clipLength) {
               System.out.println("[CatSoundManager] startSec is past end of file: " + filename);
               return;
           }
           endMicro = Math.min(endMicro, clipLength);

           // Jump to start position
           clip.setMicrosecondPosition(startMicro);
           clip.start();

           // Stop at endSec using a timer
           final long stopAt = endMicro;
           Timer stopTimer = new Timer(10, null);
           stopTimer.addActionListener(e -> {
               if (clip.getMicrosecondPosition() >= stopAt) {
                   clip.stop();
                   clip.close();
                   stopTimer.stop();
               }
           });
           stopTimer.start();

       } catch (Exception ex) {
           System.out.println("[CatSoundManager] Could not play: " + filename);
       }
   }

    // ── Volume helpers ────────────────────────────────────────────────────────

    private static void applyVolume(Clip clip, int volumePct) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float gain = 20f * (float) Math.log10(Math.max(0.0001, volumePct / 100.0));
            fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), gain)));
        }
    }

    public static void setSfxVolume(int v)   { sfxVolume   = Math.max(0, Math.min(100, v)); }
    public static void setMusicVolume(int v) { musicVolume = Math.max(0, Math.min(100, v)); }
    public static int  getSfxVolume()        { return sfxVolume; }
    public static int  getMusicVolume()      { return musicVolume; }
    
}

