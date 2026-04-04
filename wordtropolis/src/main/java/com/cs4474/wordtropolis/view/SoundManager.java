package com.cs4474.wordtropolis.view;

import javax.sound.sampled.*;
import javax.swing.Timer;
import java.net.URL;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  SoundManager.java  —  SHARED — all activities use this
 *
 *  Replaces CatSoundManager. All sound constants and
 *  behaviour are identical to what CatSoundManager had,
 *  so CatGamePanel just changes "CatSoundManager" → "SoundManager".
 *
 *  OTHER ACTIVITIES: add your sound file name constants here
 *  following the same pattern as the cat game constants below.
 *
 *  BASE VOLUMES (set here, user can adjust at runtime):
 *    SFX volume:   80 / 100
 *    Music volume: 40 / 100
 *
 *  USER SLIDER: call setSfxVolume(0-100) / setMusicVolume(0-100)
 *  from the settings panel. Values persist for the whole session.
 * ╚══════════════════════════════════════════════════════════╝
 */
public final class SoundManager {

    // ── Base volumes — these are the defaults you set ─────────────────────────
    private static final int DEFAULT_SFX_VOL   = 80;
    private static final int DEFAULT_MUSIC_VOL = 25;

    private static int sfxVolume   = DEFAULT_SFX_VOL;
    private static int musicVolume = DEFAULT_MUSIC_VOL;

    // ── Background music state ────────────────────────────────────────────────
    private static Clip  bgMusicClip;
    private static Timer bgLoopTimer;

    private SoundManager() {}

    // ═══════════════════════════════ SOUND FILE CONSTANTS ════════════════════

    // ── Cat Game ─────────────────────────────────────────────────────────────
    public static final String CAT_MEOW_HAPPY    = "cat_game/cat_meow.wav";
    public static final String CAT_MEOW_MAD      = "cat_game/cat_meow_mad.wav";
    public static final String CAT_CORRECT       = "cat_game/magical_soft_chime.wav";
    public static final String CAT_ERROR         = "cat_game/gentle_error_tone.wav";
    public static final String CAT_WIND          = "cat_game/wind_sound.wav";
    public static final String CAT_TILE_CLICK    = "cat_game/box_effect.wav";
    public static final String CAT_LEVEL_DONE    = "cat_game/completion_sound.wav";
    public static final String CAT_GAME_START    = "cat_game/game_start.wav";
    public static final String CAT_BGM           = "cat_game/background_music.wav";

    // ── Bridge Game ───────────────────────────────────────────────────────────
    // All files live in:  resources/sounds/bridge_game/
    public static final String BRIDGE_BGM        = "bridge_game/background.wav";
    public static final String BRIDGE_BOX        = "bridge_game/box_effect.wav";
    public static final String BRIDGE_ERROR      = "bridge_game/gentle_error_tone.wav";
    public static final String BRIDGE_WIN        = "bridge_game/chaching.wav";

    // ═══════════════════════════════ ONE-SHOT SFX ════════════════════════════

    public static void play(String filename) {
        if (sfxVolume == 0 || filename == null || filename.isEmpty()) return;
        new Thread(() -> {
            try {
                Clip clip = openClip(filename);
                if (clip == null) return;
                applyVolume(clip, sfxVolume);
                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception ex) {
                System.out.println("[SoundManager] Cannot play: " + filename);
            }
        }, "sfx-thread").start();
    }

    public static void playFrom(String filename, double startSec, double endSec) {
        if (sfxVolume == 0 || filename == null || filename.isEmpty()) return;
        new Thread(() -> {
            try {
                Clip clip = openClip(filename);
                if (clip == null) return;
                applyVolume(clip, sfxVolume);

                long startMicro = (long)(startSec * 1_000_000);
                long endMicro   = Math.min((long)(endSec * 1_000_000),
                                           clip.getMicrosecondLength());
                if (startMicro >= clip.getMicrosecondLength()) { clip.close(); return; }

                clip.setMicrosecondPosition(startMicro);
                clip.start();

                final long stopAt = endMicro;
                Timer t = new Timer(10, null);
                t.addActionListener(e -> {
                    if (clip.getMicrosecondPosition() >= stopAt) {
                        clip.stop(); clip.close(); t.stop();
                    }
                });
                t.start();
            } catch (Exception ex) {
                System.out.println("[SoundManager] Cannot playFrom: " + filename);
            }
        }, "sfx-thread").start();
    }

    // ═══════════════════════════════ BACKGROUND MUSIC ════════════════════════

    public static void startMusic(String filename) {
        stopMusic();
        if (musicVolume == 0 || filename == null || filename.isEmpty()) return;
        try {
            Clip clip = openClip(filename);
            if (clip == null) return;
            bgMusicClip = clip;
            applyVolume(bgMusicClip, musicVolume);

            double cutOffSec    = 1.0;
            double fadeSec      = 0.8;
            long   clipLen      = bgMusicClip.getMicrosecondLength();
            long   cutOffMicro  = clipLen - (long)(cutOffSec * 1_000_000);
            long   fadeStart    = cutOffMicro - (long)(fadeSec * 1_000_000);

            bgMusicClip.start();

            bgLoopTimer = new Timer(50, null);
            bgLoopTimer.addActionListener(e -> {
                if (bgMusicClip == null || !bgMusicClip.isOpen()) {
                    ((Timer)e.getSource()).stop(); return;
                }
                long pos = bgMusicClip.getMicrosecondPosition();
                if (pos >= fadeStart && pos < cutOffMicro) {
                    double fadePct = (double)(pos - fadeStart) / (cutOffMicro - fadeStart);
                    applyVolume(bgMusicClip, (int)(musicVolume * (1.0 - fadePct)));
                }
                if (pos >= cutOffMicro) {
                    bgMusicClip.stop();
                    bgMusicClip.setMicrosecondPosition(0);
                    applyVolume(bgMusicClip, 0);
                    bgMusicClip.start();
                    musicFadeIn((Timer)e.getSource());
                }
            });
            bgLoopTimer.start();

        } catch (Exception ex) {
            System.out.println("[SoundManager] Cannot start BGM: " + filename);
        }
    }

    public static void stopMusic() {
        if (bgLoopTimer != null) { bgLoopTimer.stop(); bgLoopTimer = null; }
        if (bgMusicClip != null && bgMusicClip.isOpen()) {
            bgMusicClip.stop();
            bgMusicClip.close();
            bgMusicClip = null;
        }
    }

    private static void musicFadeIn(Timer loopTimer) {
        loopTimer.stop();
        final int[] step = {0};
        final int steps  = 20;
        Timer fi = new Timer(25, null);
        fi.addActionListener(e -> {
            step[0]++;
            if (bgMusicClip == null || !bgMusicClip.isOpen()) { fi.stop(); return; }
            applyVolume(bgMusicClip,
                    Math.min(musicVolume, (int)(musicVolume * (double)step[0]/steps)));
            if (step[0] >= steps) { fi.stop(); loopTimer.start(); }
        });
        fi.start();
    }

    // ═══════════════════════════════ VOLUME CONTROL ═══════════════════════════

    public static void setSfxVolume(int v) {
        sfxVolume = Math.max(0, Math.min(100, v));
    }

    public static void setMusicVolume(int v) {
        musicVolume = Math.max(0, Math.min(100, v));
        if (bgMusicClip != null && bgMusicClip.isOpen()) {
            applyVolume(bgMusicClip, musicVolume);
        }
    }

    public static void resetToDefaults() {
        setSfxVolume(DEFAULT_SFX_VOL);
        setMusicVolume(DEFAULT_MUSIC_VOL);
    }

    public static int getSfxVolume()   { return sfxVolume; }
    public static int getMusicVolume() { return musicVolume; }

    // ═══════════════════════════════ PRIVATE HELPERS ══════════════════════════

    private static Clip openClip(String filename) {
        try {
            URL res = SoundManager.class.getResource("/sounds/" + filename);
            if (res == null) {
                System.out.println("[SoundManager] File not found: /sounds/" + filename);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(res);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            System.out.println("[SoundManager] Error opening: " + filename);
            return null;
        }
    }

    private static void applyVolume(Clip clip, int volumePct) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float gain = 20f * (float) Math.log10(Math.max(0.0001, volumePct / 100.0));
                fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), gain)));
            }
        } catch (Exception e) {
            // Some audio systems don't support MASTER_GAIN — skip silently
        }
    }
}