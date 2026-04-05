package com.cs4474.wordtropolis.view;

import javax.sound.sampled.*;
import javax.swing.Timer;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private static final CopyOnWriteArrayList<Clip> activeSfx = new CopyOnWriteArrayList<>();

    /**

    private SoundManager() {}

    // ═══════════════════════════════ GAME ACTIVITY ENUM ════════════════════════
    /**
     * Enum representing all possible game activities.
     * Used to conditionally play sounds based on current active game.
     */
    public enum GameActivity {
        SCREEN_MAP, CAT_GAME, BOSS_GAME, BURGLAR_GAME, BRIDGE_GAME, FIRE_GAME
    }

    // Current active game state - defaults to MAP
    private static GameActivity currentActivity = GameActivity.SCREEN_MAP;

    // ═══════════════════════════════ SOUND FILE CONSTANTS ════════════════════

    // ── Cat Game ─────────────────────────────────────────────────────────────

    // All files live in:  resources/sounds/cat_game/
    
    public static final String CAT_MEOW_HAPPY    = "cat_game/cat_meow.wav";
    public static final String CAT_MEOW_MAD      = "cat_game/cat_meow_mad.wav";
    public static final String CAT_CORRECT       = "cat_game/magical_soft_chime.wav";
    public static final String CAT_ERROR         = "cat_game/gentle_error_tone.wav";
    public static final String CAT_WIND          = "cat_game/wind_sound.wav";
    public static final String CAT_TILE_CLICK    = "cat_game/box_effect.wav";
    public static final String CAT_LEVEL_DONE    = "cat_game/completion_sound.wav";
    public static final String CAT_GAME_START    = "cat_game/game_start.wav";
    public static final String CAT_BGM           = "cat_game/background_music.wav"; 
    
    
       // ── Burglar Game ─────────────────────────────────────────────────────────────
    public static final String SFX_CORRECT        = "cat_game/magical_soft_chime.wav";
    public static final String SFX_ERROR          = "cat_game/gentle_error_tone.wav";
    public static final String SFX_TILE_CLICK     = "cat_game/box_effect.wav";
    public static final String SFX_LEVEL_COMPLETE = "cat_game/completion_sound.wav";
    public static final String SFX_GAME_START     = "cat_game/game_start.wav";
    public static final String BGM_BACKGROUND     = "burglar_game/chase.wav";

    // ── Bridge Game ───────────────────────────────────────────────────────────
    // All files live in:  resources/sounds/bridge_game/
    public static final String BRIDGE_BGM        = "bridge_game/background.wav";
    public static final String BRIDGE_BOX        = "bridge_game/box_effect.wav";
    public static final String BRIDGE_ERROR      = "bridge_game/gentle_error_tone.wav";
    public static final String BRIDGE_WIN        = "bridge_game/chaching.wav";

    // ── Boss Game sounds ─────────────────────────────────────────────────────
    public static final String BOSS_ATTACK_HIT = "boss_game/attack_hit.wav";
    public static final String BOSS_HERO_HURT = "boss_game/hero_hurt.wav";
    public static final String BOSS_SHIELD_ACTIVATE = "boss_game/shield_activate.wav";
    public static final String BOSS_SHIELD_BREAK = "boss_game/shield_break.wav";
    public static final String BOSS_VICTORY = "boss_game/victory.wav";
    public static final String BOSS_DEFEAT = "boss_game/defeat.wav";
    public static final String BOSS_TIMER_TICKING = "boss_game/timer_ticking.wav";
    public static final String BOSS_BGM           = "boss_game/Boss_Battle.wav"; 
    public static final String BOSS_HINT_ACTIVATE = "boss_game/hint_activate.wav";
    public static final String BOSS_HINT_PLACE = "boss_game/click.wav";
    
    // ── Add other activity sounds here following the same pattern ─────────────
    // Example:
    // public static final String FIRE_CORRECT  = "fire_game/correct.wav";
    // public static final String FIRE_BGM      = "fire_game/background_music.wav";

    // ═══════════════════════════════ ACTIVITY MANAGEMENT ══════════════════════

    /**
     * Set the currently active game activity.
     * Call this when transitioning between games or panels.
     * 
     * @param activity The new active game activity
     */
    public static void setActivity(GameActivity activity) {
        currentActivity = activity;
    }

    /**
     * Get the currently active game activity.
     * 
     * @return The current active game activity
     */
    public static GameActivity getActivity() {
        return currentActivity;
    }

    // ═══════════════════════════════ ONE-SHOT SFX ════════════════════════════

    public static void play(String filename) {
        if (sfxVolume == 0 || filename == null || filename.isEmpty()) return;
        
            // DEBUG: Print where the sound is being called from
    if (filename.contains("MEOW_MAD") || filename.contains("ERROR")) {
        System.out.println("[DEBUG] Sound called: " + filename + " from " + 
            Thread.currentThread().getStackTrace()[2]);
        System.out.println("[DEBUG] Current activity: " + currentActivity);
    }
        
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

    /**
     * Plays a sound only if the required activity matches the current active game.
     * This prevents sounds from playing when the user is in a different game.
     * 
     * @param filename The sound file to play
     * @param required The activity that this sound belongs to
     */
    public static void playConditional(String filename, GameActivity required) {
        System.out.println("Checking sound: " + filename + " | Required: " + required + " | Current: " + currentActivity);
    
    if (required == currentActivity) {
        play(filename);
    }
        if (required == currentActivity) {
            play(filename);
        }
    }

    /**
     * Plays a sound only if the required activity matches the provided current activity.
     * Alternative overload for cases where you want to pass the activity explicitly.
     * 
     * @param filename The sound file to play
     * @param required The activity that this sound belongs to
     * @param current The current active activity
     */
    public static void playConditional(String filename, GameActivity required, GameActivity current) {
        if (required == current) {
            play(filename);
        }
    }
    
        /**
     * Play only a portion of a sound file, conditionally based on current activity.
     *
     * @param filename  sound file constant (e.g. SoundManager.CAT_WIND)
     * @param startSec  start time in seconds
     * @param endSec    end time in seconds
     * @param required  The activity that this sound belongs to
     */
    public static void playFromConditional(String filename, double startSec, double endSec, GameActivity required) {
        if (required == currentActivity) {
            playFrom(filename, startSec, endSec);
        }
    }


    /**
     * Play only a portion of a sound file.
     * Useful for trimming long files or playing a specific stab.
     *
     * @param filename  sound file constant (e.g. SoundManager.CAT_WIND)
     * @param startSec  start time in seconds
     * @param endSec    end time in seconds
     */
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

    /**
     * Start background music only if the provided activity matches the current one.
     * Useful for auto-starting music when entering a game.
     *
     * @param filename  BGM file constant
     * @param required  The activity that this BGM belongs to
     */
    public static void startMusicConditional(String filename, GameActivity required) {
        if (required == currentActivity) {
            startMusic(filename);
        }
    }

    /** Stop background music and clean up resources. */
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