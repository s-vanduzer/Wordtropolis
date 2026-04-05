package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.BossGameModel;
import static com.cs4474.wordtropolis.model.BossGameModel.Difficulty.HARD;
import static com.cs4474.wordtropolis.model.BossGameModel.Difficulty.MEDIUM;
import static com.cs4474.wordtropolis.model.BossGameModel.TypeResult.TYPED_FULL;
import static com.cs4474.wordtropolis.model.BossGameModel.TypeResult.TYPED_NOT_AVAILABLE;
import static com.cs4474.wordtropolis.model.BossGameModel.TypeResult.TYPED_OK;
import com.cs4474.wordtropolis.model.Game;

import com.cs4474.wordtropolis.model.BossGameModel.AnswerResult;
import com.cs4474.wordtropolis.model.BossGameModel.BattlePhase;



import javax.imageio.ImageIO;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionListener;

/**
 *  BossGamePanel.java 
 */
public class BossGamePanel extends JPanel {

    // ── Model ─────────────────────────────────────────────────────────────────
    private final BossGameModel model;

    // ── Game screens ──────────────────────────────────────────────────────────
    private enum Screen { INTRO, GAME, RESCUE }
    private Screen currentScreen = Screen.INTRO;

    // ── Images ────────────────────────────────────────────────────────────────
    private BufferedImage imgBg, imgTree, imgCat, imgNpc, imgLadder;
    private BufferedImage imgLightPole, imgProgressBar;
    private BufferedImage imgLetterBox, imgOuterBox, imgBanner;
    private BufferedImage imgBack, imgCheck, imgSparkle;
    private BufferedImage imgHero;   // selected hero sprite sheet
    private BufferedImage imgVillain;
    private BufferedImage imgShield;

    // ── Sprite constants ──────────────────────────────────────────────────────
    private static final int CAT_FRAMES   = 10,  CAT_FW = 32, CAT_FH = 32;
    private static final int NPC_COLS     = 6,  NPC_FW = 32, NPC_FH = 32;
    private static final int SPKL_FRAMES  = 14, SPKL_W = 32, SPKL_H = 32;
    // Hero sprite sheet: 4 cols × 4 rows, each frame ~210×316px
    // Row 3 (last row, 0-indexed) = back-facing → hero looks toward tree
    private static final int HERO_COLS    = 4;
    private static final int HERO_FW      = 210;   // frame width  (843 / 4)
    private static final int HERO_FH      = 330;   // frame height (1264 / 4)
    private static final int HERO_BACK_ROW = 3;    // last row = back-facing walk
    private int   heroFrame = 0;
    private Timer heroTimer;

    // ── Progress bar from progress_bar.png ───────────────────────────────────
    // Row 1 (y=24) is the cyan/filled bar — we rotate it 90° to use vertically
    // Row 3 (y=72) is used as the timer bar (red/orange fill)
    private static final int PB_ROW_H     = 24;
    private static final int PB_LADDER_ROW = 1;  // cyan bar for ladder progress
    private static final int PB_TIMER_ROW  = 3;  // red/orange bar for timer

    // ── Animation state ───────────────────────────────────────────────────────
    private int   catFrame = 0, npcFrame = 0, sparkleFrame = 0;
    private boolean showSparkle = false;
    private int   sparkleX, sparkleY;
    private float catRescueY = 1.0f;
    
    private int   catDrawX = -1;   // ADD THIS
    private int   catDrawY = -1;   // ADD THIS
    
    private Timer catTimer, npcTimer, sparkleTimer, rescueTimer;
    
    // Add these animation state variables
        private Timer attackTimer;
        private int attackAnimProgress = 0;
        private boolean showingHitEffect = false;
        private int hitEffectX = 0, hitEffectY = 0;
        private Timer hitEffectTimer;

    // ── Floating letter tile positions ────────────────────────────────────────
    // Tiles float in screen space; wind offsets drift them
    private int[]   tileBaseX, tileBaseY;   // original positions
    private int[]   windX, windY;           // drift offsets
    private Timer   windTimer;

    // ── Countdown timer ───────────────────────────────────────────────────────
    private static final int T_EASY = 30, T_MEDIUM = 25, T_HARD = 15, T_WARN = 10;
    private int   timeLeft = T_EASY;
    private boolean warnPlayed = false;
    private Timer countdownTimer;

    // ── Difficulty ────────────────────────────────────────────────────────────
    private BossGameModel.Difficulty lastDiff = BossGameModel.Difficulty.EASY;

    // ── State flags ───────────────────────────────────────────────────────────
    private boolean waitingForNext = false;

    // ── Feedback message ──────────────────────────────────────────────────────
    private String  feedbackMsg   = "";
    private Color   feedbackColor = Color.WHITE;
    private Timer   feedbackTimer;
    
     // ── Drag & Drop state ─────────────────────────────────────────────────────
    private int     dragTileIndex = -1;   // index within available or arrangement list
    private boolean dragFromArr   = false; // true = dragging from arrangement
    private int     dragCurrentX, dragCurrentY;
    private boolean isDragging    = false;
    private int     hoverAvailIdx = -1;   // which available tile the cursor is over
    private int     hoverArrIdx   = -1;   // which arrangement tile the cursor is over
    
    
    private boolean skipButtonPressed = false;
    private Rectangle skipButtonRect = null;
    
    private Timer hintTimer;
    private boolean hintModeActive = false;
    private int hintFillCount = 0;
    private Color hintGlowColor = new Color(255, 215, 0, 50);

    // ─────────────────────────────────────────────────────────────────────────
    public BossGamePanel() {
            model = new BossGameModel(
        Game.getInstance().getWordList(),
        Game.getInstance().getMisspelledWordList() // You'll need to add this getter
    );
            printGameWordList();
        setLayout(null);   // absolute positioning — we paint everything ourselves
        setBackground(Color.BLACK);
        loadImages();
        // FIX 3: music starts only when user clicks "Start Rescue!" not on load
        startAnimations();
        setupKeyboard();
        setupMouseInteraction();
        // Intro screen just paints — clicking Start Rescue transitions to GAME
    }

        // Update cleanup in removeNotify
        @Override
        public void removeNotify() {
            super.removeNotify();
            SoundManager.stopMusic();
            if (hintTimer != null) hintTimer.stop();
            stopAll();
        }

    @Override public void addNotify() {
        super.addNotify();
        // FIX 3: only request focus here — music is started in startGame()
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // ═══════════════════════════════ LOADING ═════════════════════════════════

    private void loadImages() {
        
        imgVillain     = img("/images/boss_game/boss.png");
        imgShield      = img("/images/boss_game/shield.png");
        imgBg          = img("/images/boss_game/roof_bg.png");
        imgProgressBar = img("/images/cat_game/progress_bar.png");
        imgLetterBox   = img("/images/cat_game/individual_box.png");
        imgOuterBox    = img("/images/cat_game/outer_box.png");
        imgBanner      = img("/images/cat_game/banner.png");
        imgBack        = img("/images/cat_game/back.png");
        imgCheck       = img("/images/cat_game/check.png");
        imgSparkle     = img("/images/cat_game/blue_sparkle.png");
        imgSparkle     = img("/images/cat_game/blue_sparkle.png");

        // Pick hero sprite sheet based on what PickHeroPanel saved via setAvatarPath().
        // HERO_LABELS are "Mia" (girl), "Alex" (boy), "Zyx" (alien).
        String avatarPath = Game.getInstance().getAvatarPath();
        if ("Mia".equalsIgnoreCase(avatarPath)) {
            imgHero = img("/images/general/hero1_standing.png");
        } else if ("Zyx".equalsIgnoreCase(avatarPath)) {
            imgHero = img("/images/general/alien_standing.png");
        } else {
            // Default: Alex / boy hero
            imgHero = img("/images/general/hero2_standing.png");
        }
    }

    private BufferedImage img(String path) {
        try {
            URL r = getClass().getResource(path);
            if (r == null) { System.out.println("[Cat] missing: " + path); return null; }
            return ImageIO.read(r);
        } catch (Exception e) { System.out.println("[Cat] err: " + path); return null; }
    }

    // ═══════════════════════════════ ANIMATIONS ══════════════════════════════

    private void startAnimations() {
        catTimer  = new Timer(150, e -> { catFrame  = (catFrame +1)%CAT_FRAMES; repaint(); });
        catTimer.start();
        npcTimer  = new Timer(200, e -> { npcFrame  = (npcFrame +1)%NPC_COLS;   repaint(); });
        npcTimer.start();
        heroTimer = new Timer(180, e -> { heroFrame = (heroFrame+1)%HERO_COLS;  repaint(); });
        heroTimer.start();
    }

    private void triggerSparkle(int x, int y) {
        sparkleX = x; sparkleY = y; sparkleFrame = 0; showSparkle = true;
        if (sparkleTimer != null) sparkleTimer.stop();
        sparkleTimer = new Timer(55, e -> {
            if (++sparkleFrame >= SPKL_FRAMES) { showSparkle = false; ((Timer)e.getSource()).stop(); }
            repaint();
        });
        sparkleTimer.start();
    }

    private void stopAll() {
        for (Timer t : new Timer[]{catTimer,npcTimer,heroTimer,sparkleTimer,rescueTimer,
                                    windTimer,countdownTimer,feedbackTimer})
            if (t != null) t.stop();
    }

    // ═══════════════════════════════ KEYBOARD ════════════════════════════════

    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (currentScreen != Screen.GAME || waitingForNext) return;
                char key = e.getKeyChar();
                // Ignore control characters — backspace/enter/escape handled in keyPressed
                if (key == KeyEvent.CHAR_UNDEFINED || key < 32) return;
                switch (model.typeKey(key)) {
                    case TYPED_OK:
                        SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.BOSS_GAME);
                        triggerSparkle(getWidth()/2, getHeight()/2 - 60);
                        repaint(); break;
                    case TYPED_NOT_AVAILABLE:
                        SoundManager.playConditional(SoundManager.CAT_ERROR, SoundManager.GameActivity.BOSS_GAME);
                        shakeEffect();
                        showFeedback("'" + Character.toUpperCase(key) + "' is not available!", UITheme.ACCENT_RED); break;
                    case TYPED_FULL:
                        showFeedback("Press Submit!", UITheme.ACCENT_YELLOW); break;
                }
            }
            @Override public void keyPressed(KeyEvent e) {
                if (currentScreen != Screen.GAME || waitingForNext) return;
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_BACK_SPACE && model.backspace()) {
                    SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.BOSS_GAME);
                    repaint();
                } else if (c == KeyEvent.VK_ENTER) {
                    handleSubmit();
                } else if (c == KeyEvent.VK_ESCAPE) {
                    model.clearArrangement(); repaint();
                }
            }
        });
    }

    // ═══════════════════════════════ TILE POSITIONS ═══════════════════════════

    /**
     * Compute the floating positions for the available letter tiles.
     * They spread out in a gentle arc in the centre of the screen.
     */
    private void computeTilePositions() {
        int count = model.getAvailableLetters().size();
        tileBaseX = new int[count];
        tileBaseY = new int[count];
        windX     = new int[count];
        windY     = new int[count];

        int W = getWidth(), H = getHeight();
        int tileSize = 56;
        int totalW   = count * (tileSize + 10) - 10;
        int startX   = (W - totalW) / 2;
        int baseY    = (int)(H * 0.5f);   // vertically centred in scene area

        // Gentle arc: y varies sinusoidally
        for (int i = 0; i < count; i++) {
            tileBaseX[i] = startX + i * (tileSize + 10);
            double angle = 0;
            tileBaseY[i] = baseY - (int)(Math.sin(angle) * 20);
        }
    }

    // ═══════════════════════════════ GAME LOGIC ══════════════════════════════

// Update startCountdown to play ticking sound
// Update the countdown timer to track timeout words

// Update the startCountdown method to include hint system
private void startCountdown() {
    if (countdownTimer != null) countdownTimer.stop();
    if (hintTimer != null) hintTimer.stop();
    
    // Store the current word BEFORE starting timer
    final String currentWordAtStart = model.getCurrentWord();
    
    switch (model.getCurrentDifficulty()) {
        case MEDIUM: timeLeft = T_MEDIUM; break;
        case HARD:   timeLeft = T_HARD;   break;
        default:     timeLeft = T_EASY;   break;
    }
    warnPlayed = false;
    hintModeActive = false;
    
    countdownTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (--timeLeft <= 0) {
                // Time's up - word is automatically added to timeout list
                countdownTimer.stop();
                if (hintTimer != null) hintTimer.stop();
                
                String timedOutWord = model.getCurrentWord();
                if (timedOutWord != null && !timedOutWord.isEmpty()) {
                    if (!model.isComplete() && !model.isHeroDead()) {
                        System.out.println("[BossGame] Word timed out: " + timedOutWord);
                        model.addTimeoutWord(timedOutWord);
                        
                        // Check if word was auto-completed
                        if (model.isWordAutoCompleted()) {
                            showFeedback("Time's up! Word auto-completed and added to practice!", 
                                        new Color(255, 100, 100));
                        } else {
                            showFeedback("Time's up! '" + timedOutWord + "' added to practice pool!", 
                                        UITheme.ACCENT_YELLOW);
                        }
                        
                        SoundManager.playConditional(SoundManager.BOSS_TIMER_TICKING, SoundManager.GameActivity.BOSS_GAME);
                        
                        // Hero takes damage for timeout
                        // You can add this line if you want timeout to hurt
                        // model.takeTimeoutDamage(5);
                    }
                }
                
                model.clearArrangement();
                
                if (!model.isComplete() && !model.isHeroDead()) {
                    model.advanceToNextWord();
                    refreshGame();
                } else if (model.isHeroDead()) {
                    battleComplete(false);
                } else if (model.isComplete()) {
                    battleComplete(true);
                }
                repaint();
            } else if (timeLeft <= 10 && !hintModeActive) {
                // ACTIVATE HINT MODE when 10 seconds or less remaining
                hintModeActive = true;
                model.startHintMode();
                startHintTimer();
                showFeedback("HINT MODE ACTIVATED! Letters will auto-fill!", 
                            new Color(252, 201, 0));
                SoundManager.playConditional(SoundManager.BOSS_HINT_ACTIVATE, SoundManager.GameActivity.BOSS_GAME);
            } else if (timeLeft == T_WARN && !warnPlayed) {
                SoundManager.playConditional(SoundManager.BOSS_TIMER_TICKING, SoundManager.GameActivity.BOSS_GAME);
                warnPlayed = true;
                showFeedback("Hurry! Hint mode activates at 10 seconds!", 
                            UITheme.ACCENT_YELLOW);
            }
            
            // Update timer bar color based on hint mode
            repaint();
        }
    });
    countdownTimer.start();
}


// Add hint timer that auto-fills letters every second when in hint mode
private void startHintTimer() {
    if (hintTimer != null) hintTimer.stop();
    
    hintTimer = new Timer(2000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (hintModeActive && model.isHintModeActive()) {
                // Check if word is already complete
                if (model.getPlayerArrangement().size() >= model.getCurrentWord().length()) {
                    // Word is complete, stop hint timer
                    if (hintTimer != null) hintTimer.stop();
                    return;
                }
                
                // Update hint - auto-fill one letter
                model.updateHint();
                
                // Play hint sound effect
                SoundManager.playConditional(SoundManager.BOSS_HINT_PLACE, SoundManager.GameActivity.BOSS_GAME);
                
                // Show visual feedback
                triggerHintGlow();
                
                // Refresh display
                computeTilePositions();
                repaint();
                
                // Check if word is now auto-completed
                if (model.isWordAutoCompleted()) {
                    showFeedback("Word auto-completed! This will count as incorrect!", 
                                new Color(255, 100, 100));
                    SoundManager.playConditional(SoundManager.CAT_ERROR, SoundManager.GameActivity.BOSS_GAME);
                    if (hintTimer != null) hintTimer.stop();
                    
                    // Auto-submit after a short delay
                    new Timer(1500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            if (!waitingForNext) {
                                handleSubmit();
                            }
                            ((Timer)evt.getSource()).stop();
                        }
                    }).start();
                }
            }
        }
    });
    hintTimer.start();
}

// Add visual feedback for hint placement
private void triggerHintGlow() {
    Timer glowTimer = new Timer(50, new ActionListener() {
        int glowCount = 0;
        @Override
        public void actionPerformed(ActionEvent e) {
            glowCount++;
            if (glowCount >= 6) {
                ((Timer)e.getSource()).stop();
            }
            repaint();
        }
    });
    glowTimer.start();
}

// Update the paintLetterTile to show hint glow
private void paintLetterTile(Graphics2D g2, int x, int y, int size,
                              String letter, boolean placed, int index) {
    if (isDragging && dragTileIndex == index && dragFromArr == placed) return;
 
    // Check if this tile was auto-placed by hint system
    boolean isHintPlaced = (hintModeActive && placed && 
                            model.isHintModeActive() && 
                            index >= model.getPlayerArrangement().size() - 1);
    
    if (imgLetterBox != null) {
        g2.drawImage(imgLetterBox, x, y, size, size, null);
        if (placed) {
            if (isHintPlaced) {
                // Gold tint for hint-placed letters
                g2.setColor(new Color(1f, 0.8f, 0f, 0.4f));
            } else {
                g2.setColor(new Color(0.2f, 0.5f, 1f, 0.3f));
            }
            g2.fillRect(x, y, size, size);
        }
    } else {
        g2.setColor(placed ? (isHintPlaced ? new Color(255, 215, 0, 100) : UITheme.ACCENT_BLUE) 
                           : UITheme.BG_CARD);
        g2.fillRoundRect(x, y, size, size, 8, 8);
    }
 
    // Hover highlight
    boolean hovered = placed ? (hoverArrIdx == index) : (hoverAvailIdx == index);
    if (hovered && !isDragging) {
        g2.setColor(new Color(1f, 1f, 1f, 0.28f));
        g2.fillRoundRect(x, y, size, size, 6, 6);
        g2.setColor(new Color(0xFF, 0xD7, 0x00, 220));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 6, 6);
        g2.setStroke(new BasicStroke(1));
    }
 
    g2.setFont(new Font("Monospaced", Font.BOLD, size/2 + 2));
    
    // Hint-placed letters get golden color
    if (isHintPlaced) {
        g2.setColor(new Color(255, 215, 0));
    } else {
        g2.setColor(placed ? UITheme.TEXT_BRIGHT : new Color(0x3B2A1A));
    }
    drawCentredString(g2, letter, x + size/2, y + size/2 + 6);
}




    // Add method to manually trigger timeout (if player gives up)
    private void handleTimeout() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            String currentWord = model.getCurrentWord();
            if (currentWord != null && !currentWord.isEmpty()) {
                model.addTimeoutWord(currentWord);
                showFeedback("Word added to practice pool for Phase 1B!", UITheme.ACCENT_YELLOW);
            }
            countdownTimer.stop();
            model.clearArrangement();
            model.advanceToNextWord();
            refreshGame();
        }
    }


    private void heroTimeOut() {
        showFeedback("Time's up! Hero takes damage!", UITheme.ACCENT_RED);
        SoundManager.playConditional(SoundManager.BOSS_HERO_HURT, SoundManager.GameActivity.BOSS_GAME);
        // Apply timeout penalty here
        if (model.getHeroHealth() <= 0) {
            battleComplete(false);
        } else {
            model.clearArrangement();
            startCountdown();
            repaint();
        }
    }

    private void startWindEffect() {
        if (windTimer != null) windTimer.stop();

        // FIX 3a: play wind sound every time tile drifting starts
        int prevVol = SoundManager.getSfxVolume();
        SoundManager.setSfxVolume(30);                           // quiet wind
        SoundManager.playFromConditional(SoundManager.CAT_WIND, 0.0, 1.5, SoundManager.GameActivity.BOSS_GAME);
        SoundManager.setSfxVolume(prevVol);

        java.util.Random rand = new java.util.Random();
        int DMAX = 55, DSTEP = 7;
        windTimer = new Timer(80, e -> {
            if (windX == null) return;
            for (int i = 0; i < windX.length; i++) {
                windX[i] = clamp(windX[i] + (rand.nextInt(3)-1)*DSTEP, -DMAX, DMAX);
                windY[i] = clamp(windY[i] + (rand.nextInt(3)-1)*DSTEP, -DMAX, DMAX);
            }
            repaint();
        });
        windTimer.start();
    }

    private void stopWindEffect() {
        if (windTimer != null) { windTimer.stop(); windTimer = null; }
        windX = windY = null;
    }

private void handleSubmit() {
    if (waitingForNext) return;
    
    if (model.getPlayerArrangement().isEmpty()) {
        showFeedback("Spell the word to attack!", UITheme.ACCENT_YELLOW);
        return;
    }
    
    if (model.getPlayerArrangement().size() < model.getCurrentWord().length()) {
        showFeedback("Place all letters!", UITheme.ACCENT_YELLOW);
        return;
    }
    
    AnswerResult result = model.submitAnswer();
    
    if (result.correct) {
        // Play attack animation
        triggerAttackAnimation(result.damageDealt);
        
        // Play appropriate sound
        if (result.newPhase == BattlePhase.PHASE_1B && result.effect.equals("SHIELD_ACTIVATED")) {
            SoundManager.playConditional(SoundManager.BOSS_SHIELD_ACTIVATE, SoundManager.GameActivity.BOSS_GAME);
            showFeedback("Shield Activated! Break through to continue!", UITheme.ACCENT_TEAL);
        } else if (result.newPhase == BattlePhase.PHASE_1C) {
            SoundManager.playConditional(SoundManager.BOSS_SHIELD_BREAK, SoundManager.GameActivity.BOSS_GAME);
            showFeedback("Shield Broken! Final assault!", UITheme.ACCENT_TEAL);
        } else {
            SoundManager.playConditional(SoundManager.BOSS_ATTACK_HIT, SoundManager.GameActivity.BOSS_GAME);
            showFeedback("Hit! Dealt " + result.damageDealt + " damage!", UITheme.ACCENT_TEAL);
        }
        
        if (countdownTimer != null) countdownTimer.stop();
        stopWindEffect();
        repaint();
        
        if (result.victory) {
            battleComplete(true);
        } else if (result.heroDied) {
            battleComplete(false);
        } else if (result.newPhase != model.getCurrentPhase()) {
            // Phase transition handled, wait for next word load
            waitingForNext = true;
            new Timer(2000, e -> {
                model.advanceToNextWord();
                refreshGame();
                ((Timer)e.getSource()).stop();
            }).start();
        } else {
            waitingForNext = true;
            new Timer(1400, e -> {
                model.advanceToNextWord();
                refreshGame();
                ((Timer)e.getSource()).stop();
            }).start();
        }
    } else {
        // Incorrect answer - hero takes damage
        showFeedback("Misspelled! Hero took " + result.damageTaken + " damage!", UITheme.ACCENT_RED);
        SoundManager.playConditional(SoundManager.BOSS_HERO_HURT, SoundManager.GameActivity.BOSS_GAME);
        triggerHeroHurtAnimation();
        
        if (result.heroDied) {
            battleComplete(false);
        } else {
            shakeEffect();
            repaint();
        }
    }
}



// Add attack animation
private void triggerAttackAnimation(int damage) {
    attackAnimProgress = 20;
    if (attackTimer != null) attackTimer.stop();
    attackTimer = new Timer(20, e -> {
        attackAnimProgress -= 2;
        if (attackAnimProgress <= 0) {
            attackAnimProgress = 0;
            attackTimer.stop();
        }
        repaint();
    });
    attackTimer.start();
    
    // Show hit effect at villain positionshowingHitEffect = true;
    // Position text over the villain's new right-side coordinates
    hitEffectX = (int)(getWidth() * 0.80) - 100; 
    hitEffectY = (int)(getHeight() * 0.50);
    if (hitEffectTimer != null) hitEffectTimer.stop();
    hitEffectTimer = new Timer(300, e -> {
        showingHitEffect = false;
        hitEffectTimer.stop();
        repaint();
    });
    hitEffectTimer.start();
}

private void triggerHeroHurtAnimation() {
    // Flash the panel red
    Timer hurtFlash = new Timer(50, new ActionListener() {
        int flashes = 0;
        @Override
        public void actionPerformed(ActionEvent e) {
            flashes++;
            if (flashes >= 6) {
                ((Timer)e.getSource()).stop();
            }
            repaint();
        }
    });
    hurtFlash.start();
}



// Update refreshGame to reset hint mode
private void refreshGame() {
    waitingForNext = false;
    hintModeActive = false;
    if (hintTimer != null) hintTimer.stop();
    computeTilePositions();
    startCountdown();
    BossGameModel.Difficulty d = model.getCurrentDifficulty();
    if (d != lastDiff) {
        lastDiff = d;
    }
    if (d != BossGameModel.Difficulty.EASY) startWindEffect();
    else stopWindEffect();
    repaint();
}

    private void rescueComplete() {
        showFeedback("Mission Complete!", UITheme.ACCENT_TEAL);
        SoundManager.playConditional(SoundManager.CAT_LEVEL_DONE, SoundManager.GameActivity.BOSS_GAME);
//        Game.getInstance().setCatCompleted(true);
        currentScreen = Screen.RESCUE;
        catRescueY = 0.0f;   // start at bottom of tree, move upward
        rescueTimer = new Timer(55, e -> {
            catRescueY += 0.015f;
            if (catRescueY >= 1.0f) { catRescueY = 1.0f; ((Timer)e.getSource()).stop(); }
            repaint();
        });
        rescueTimer.start();
        new Timer(4000, e -> {
            showResultDialog();
            ((Timer)e.getSource()).stop();
        }).start();
    }
    
    private void showResultDialog() {
        // FIX 3c: stop music immediately before dialog and before navigating
        SoundManager.stopMusic();
        stopAll();  // stop all timers too so nothing runs in background
        JOptionPane.showMessageDialog(this,
            "<html><div style='font-size:14px;text-align:center;padding:10px'>"
            + "<b>Cat Rescued!</b><br><br>"
            + "Score: <b>" + model.getTotalScore() + "</b><br>"
            + "Wrong attempts: " + model.getIncorrectAttempts()
            + "</div></html>",
            "Mission Complete!", JOptionPane.PLAIN_MESSAGE);
        Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
    }

    // TTS removed — only CatGamePanel uses TTS

    private void showFeedback(String msg, Color c) {
        feedbackMsg = msg; feedbackColor = c;
        if (feedbackTimer != null) feedbackTimer.stop();
        feedbackTimer = new Timer(3000, e -> { feedbackMsg = ""; repaint(); ((Timer)e.getSource()).stop(); });
        feedbackTimer.start();
        repaint();
    }

    // ── Shake: flashes the panel red + plays wind briefly ────────────────────
    private Timer shakeTimer; int shakeTick;
    private void shakeEffect() {
        if (shakeTimer != null && shakeTimer.isRunning()) return;

        // FIX 3b: play wind sound at the start of every shake/wrong-answer effect
        int prevVol = SoundManager.getSfxVolume();
        SoundManager.setSfxVolume(25);                           // very subtle wind
        SoundManager.playFromConditional(SoundManager.CAT_WIND, 0.0, 0.8, SoundManager.GameActivity.CAT_GAME);
        SoundManager.setSfxVolume(prevVol);

        shakeTick = 0;
        shakeTimer = new Timer(40, e -> {
            if (++shakeTick > 8) { shakeTimer.stop(); repaint(); }
            else repaint();
        });
        shakeTimer.start();
    }

    // ═══════════════════════════════ PAINTING ════════════════════════════════

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int W = getWidth(), H = getHeight();

        // ── 1. Background — fills the whole panel ─────────────────────────────
        paintBackground(g2, W, H);

        // Paint background
    paintBackground(g2, W, H);
    
    // Paint health bars (replaces left panel)
    paintHealthBars(g2, W, H);
    
    // Paint villain (replaces tree)
    paintVillain(g2, W, H);
    
    // Paint hero with attack animation
    paintHeroBattle(g2, W, H);
    
    paintBackButton(g2);
    
    // Paint hit effect
    if (showingHitEffect) {
        paintHitEffect(g2);
    }
    
    // Paint timer bar
    paintTimerBar(g2, W);
    
    // Paint shield overlay if active
    if (model.getCurrentPhase() == BattlePhase.PHASE_1B && model.getMayorShield() > 0) {
        paintShieldOverlay(g2, W, H);
    }
    
    // Paint game UI
    paintGameUI(g2, W, H);
    
    // Paint attack swoosh
    if (attackAnimProgress > 0) {
        paintAttackSwoosh(g2, W, H);
    }

        // ── 7. Sparkle effect ─────────────────────────────────────────────────
        if (showSparkle) paintSparkle(g2);

        // ── 8. Left panel: banner + vertical progress bar (ladder progress) ───
        paintHealthBars(g2, W, H);

        // ── 9. Top: timer bar ─────────────────────────────────────────────────
        paintTimerBar(g2, W);

        // ── 10. Intro or game puzzle overlay ──────────────────────────────────
        if (currentScreen == Screen.INTRO) {
            paintIntro(g2, W, H);
        } else {
            paintGameUI(g2, W, H);
        }
        
        // ── 11. Drag ghost — semi-transparent tile following the cursor ──────────
        if (isDragging && dragTileIndex >= 0) {
            int ghostSize = 64;
            int gx = dragCurrentX - ghostSize / 2;
            int gy = dragCurrentY - ghostSize / 2;
            String ghostLetter = "";
            if (!dragFromArr) {
                List<Character> avail = model.getAvailableLetters();
                if (dragTileIndex < avail.size()) ghostLetter = String.valueOf(avail.get(dragTileIndex));
            } else {
                List<Character> arr = model.getPlayerArrangement();
                if (dragTileIndex < arr.size()) ghostLetter = String.valueOf(arr.get(dragTileIndex));
            }
            Graphics2D gg = (Graphics2D) g2.create();
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
            if (imgLetterBox != null) gg.drawImage(imgLetterBox, gx, gy, ghostSize, ghostSize, null);
            else { gg.setColor(UITheme.BG_CARD); gg.fillRoundRect(gx, gy, ghostSize, ghostSize, 8, 8); }
            gg.setColor(new Color(0xFF, 0xD7, 0x00));
            gg.setStroke(new BasicStroke(2.5f));
            gg.drawRoundRect(gx + 1, gy + 1, ghostSize - 2, ghostSize - 2, 6, 6);
            gg.setStroke(new BasicStroke(1));
            gg.setFont(new Font("Monospaced", Font.BOLD, ghostSize / 2 + 2));
            gg.setColor(new Color(0x3B2A1A));
            drawCentredString(gg, ghostLetter, gx + ghostSize / 2, gy + ghostSize / 2 + 6);
            gg.dispose();
        }

        // ── 11. Feedback message ──────────────────────────────────────────────
        if (!feedbackMsg.isEmpty()) paintFeedback(g2, W, H);

        // ── 12. Shake flash ───────────────────────────────────────────────────
        if (shakeTimer != null && shakeTimer.isRunning() && shakeTick % 2 == 0) {
            g2.setColor(new Color(1f, 0f, 0f, 0.12f));
            g2.fillRect(0, 0, W, H);
        }

        g2.dispose();
    }

    
    
    
    // Add new painting methods
private void paintVillain(Graphics2D g2, int W, int H) {
    int villainW = (int)(W * 0.28);
    int villainH = (int)(H * 0.56);
    // Move to the RIGHT side (80% across the width)
    int villainX = (int)(W * 0.90) - villainW; 
    // Align to the same ground level as the hero
    int groundY = (int)(H * 0.85) - villainH; 

    if (imgVillain != null) {
        g2.drawImage(imgVillain, villainX, groundY, villainW, villainH, null);
    }
}

private void paintHeroBattle(Graphics2D g2, int W, int H) {
    if (imgHero == null) return;

    int drawW = 180;
    int drawH = (int)((double) HERO_FH / HERO_FW * drawW);
    // Set ground level at 85% of height
    int groundY = (int)(H * 0.85) - drawH;
    int heroX = (int)(W * 0.15); 

    // Attack animation lunges FORWARD (positive X)
    int attackOffset = (attackAnimProgress > 0) ? (20 - attackAnimProgress) * 5 : 0;

    int srcX1 = heroFrame * HERO_FW;
    int srcY1 = HERO_BACK_ROW * HERO_FH;

    g2.drawImage(imgHero,
            heroX + attackOffset, groundY,
            heroX + drawW + attackOffset, groundY + drawH,
            srcX1, srcY1 + 70, srcX1 + HERO_FW, srcY1 + HERO_FH, null);
}

    private void paintShieldOverlay(Graphics2D g2, int W, int H) {
        int shieldSize = 200;
        int shieldX = (W/2 - shieldSize/2)+10;
        int shieldY = (H/3 - shieldSize/2)+10;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        if (imgShield != null) {
            g2.drawImage(imgShield, shieldX, shieldY, shieldSize, shieldSize, null);
        } else {
            // Draw hexagonal shield
            g2.setColor(new Color(0, 150, 255, 100));
            int[] xPoints = {shieldX + shieldSize/2, shieldX + shieldSize, shieldX + shieldSize,
                             shieldX + shieldSize/2, shieldX, shieldX};
            int[] yPoints = {shieldY, shieldY + shieldSize/3, shieldY + 2*shieldSize/3,
                             shieldY + shieldSize, shieldY + 2*shieldSize/3, shieldY + shieldSize/3};
            g2.fillPolygon(xPoints, yPoints, 6);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

private void paintAttackSwoosh(Graphics2D g2, int W, int H) {
    // Start slightly in front of the hero
    int startX = (int)(W * 0.15) + 150;
    int startY = (int)(H * 0.75); 
    // End at the villain's position
    int endX = (int)(W * 0.80) - 100;
    int endY = (int)(H * 0.75);
    
    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    for (int i = 0; i < 3; i++) {
        float alpha = 0.8f - i * 0.2f;
        g2.setColor(new Color(1f, 0.9f, 0.3f, alpha)); // Brighter yellow/gold
        int offset = i * 12;
        // Draw horizontal swoosh lines
        g2.drawLine(startX, startY + offset, endX, endY + offset);
    }
    g2.setStroke(new BasicStroke(1));
}

private void paintHitEffect(Graphics2D g2) {
    g2.setFont(new Font("Monospaced", Font.BOLD, 48));
    g2.setColor(new Color(255, 200, 0, 200));
    drawCentredString(g2, "!", hitEffectX, hitEffectY);
    g2.setFont(new Font("Monospaced", Font.BOLD, 24));
    drawCentredString(g2, "HIT", hitEffectX, hitEffectY + 40);
}

private void battleComplete(boolean victory) {
    SoundManager.stopMusic();
    stopAll();
    
    if (victory) {
        showFeedback("VICTORY! Mayor Defeated!", UITheme.ACCENT_TEAL);
        SoundManager.playConditional(SoundManager.BOSS_VICTORY, SoundManager.GameActivity.BOSS_GAME);
        Game.getInstance().setCatCompleted(true);
    } else {
        showFeedback("DEFEAT! Hero has fallen!", UITheme.ACCENT_RED);
        SoundManager.playConditional(SoundManager.BOSS_DEFEAT, SoundManager.GameActivity.BOSS_GAME);
    }
    
    new Timer(3000, e -> {
        JOptionPane.showMessageDialog(this,
            "<html><div style='font-size:14px;text-align:center;padding:10px'>"
            + "<b>" + (victory ? "VICTORY!" : "DEFEAT!") + "</b><br><br>"
            + "Score: <b>" + model.getTotalScore() + "</b><br>"
            + "Incorrect attempts: " + model.getIncorrectAttempts()
            + "</div></html>",
            victory ? "Battle Won!" : "Battle Lost", JOptionPane.PLAIN_MESSAGE);
        Wordtropolis.showScreen(Wordtropolis.SCREEN_FINISH);
        ((Timer)e.getSource()).stop();
    }).start();
}
    
    
    
    // ── Background ────────────────────────────────────────────────────────────

    private void paintBackground(Graphics2D g2, int W, int H) {
        if (imgBg != null) {
            // Scale to fill, centred
            int bw = imgBg.getWidth(), bh = imgBg.getHeight();
            double scaleW = (double)W/bw, scaleH = (double)H/bh;
            double scale  = Math.max(scaleW, scaleH);
            int dw = (int)(bw*scale), dh = (int)(bh*scale);
            int dx = (W-dw)/2, dy = (H-dh)/2;
            g2.drawImage(imgBg, dx, dy, dw, dh, null);
        } else {
            g2.setPaint(new GradientPaint(0,0,UITheme.SKY_TOP,0,H*0.7f,UITheme.SKY_BOT));
            g2.fillRect(0,0,W,H);
            g2.setColor(UITheme.GRASS_COLOR);
            g2.fillRect(0,(int)(H*0.85),W,(int)(H*0.15));
        }
    }


    // ── NPCs ─────────────────────────────────────────────────────────────────

    private void paintNpcs(Graphics2D g2, int W, int H) {
    if (imgNpc == null) return;

    int npcScale  = 4;
    int fw        = NPC_FW * npcScale;   // drawn width  (96px)
    int fh        = NPC_FH * npcScale;   // drawn height (96px)
    int groundY   = (int)(H * 0.87) - fh;

    int srcY = 1 * 32;
    int srcX = npcFrame * NPC_FW;

    // ── 3 NPCs on the LEFT side of the tree ──────────────────────────────
    int[] leftPositions = {
        (int)(W * 0.11),    // leftmost
        (int)(W * 0.18),    // middle-left
        (int)(W * 0.25),    // closest to tree
    };
    for (int x : leftPositions) {

    g2.drawImage(imgNpc,
            x, groundY,  //destination top-left
            x + fw, groundY + fh, //destination bottom-right
            srcX, srcY, //source top-left
            srcX + NPC_FW, srcY + NPC_FH, //source bottom-right
            null);
    }

    // ── 3 NPCs on the RIGHT side of the tree ─────────────────────────────
    int[] rightPositions = {
        (int)(W * 0.65),    // closest to tree
        (int)(W * 0.72),    // middle-right
        (int)(W * 0.79),    // rightmost
    };
    for (int x : rightPositions) {
        g2.drawImage(imgNpc,
                x + fw, groundY, x, groundY + fh,
                srcX, srcY,
                srcX + NPC_FW, srcY + NPC_FH,
                null);
    }
}

    // ── Hero ──────────────────────────────────────────────────────────────────
    /**
     * Draw the selected hero at ground level, slightly right of centre,
     * using the back-facing row so they appear to be looking at the tree.
     *
     * Sprite sheet layout: 4 cols × 4 rows, each frame HERO_FW × HERO_FH px.
     * Row 3 (HERO_BACK_ROW) = back-facing walk cycle.
     */
    private void paintHero(Graphics2D g2, int W, int H) {
        if (imgHero == null) return;

        // Drawn size — scale down to look proportional next to NPCs
        int drawW = 160;
        int drawH = (int)((double) HERO_FH / HERO_FW * drawW);  // keep aspect ratio

        // Ground line matches NPC ground Y
        int groundY = (int)(H * 0.87) - drawH;

        // Position: just right of the tree centre
        int heroX = (int)(W * 0.50);

        // Source rectangle: back-facing row, current animation frame
        int srcX1 = heroFrame * HERO_FW;
        int srcY1 = HERO_BACK_ROW * HERO_FH;
        int srcX2 = srcX1 + HERO_FW;
        int srcY2 = srcY1 + HERO_FH;

        g2.drawImage(imgHero,
                heroX, groundY, heroX + drawW, groundY + drawH,
                srcX1, srcY1 + 70, srcX2, srcY2, null);
    }

    // ── Tree ─────────────────────────────────────────────────────────────────

    private void paintTree(Graphics2D g2, int tx, int ty, int tw, int th) {
        if (imgTree != null) {
            g2.drawImage(imgTree, tx, ty+30, tw, th+30, null);
        } else {
            g2.setColor(UITheme.TREE_TRUNK);
            g2.fillRoundRect(tx + tw/3, ty + th/2, tw/3, th/2, 8, 8);
            g2.setColor(UITheme.TREE_LEAVES);
            g2.fillOval(tx, ty, tw, th/2);
        }
    }
    
    
    private void printGameWordList() {
    System.out.println("\n╔════════════════════════════════════════════════════════════╗");
    System.out.println("║           WORDTROPOLIS - BATTLE GAME WORD LIST            ║");
    System.out.println("╚════════════════════════════════════════════════════════════╝");
    
    System.out.println("\n▶ PHASE 1A - Main Attack Words (from provided wordlist):");
    List<String> mainWords = Game.getInstance().getWordList();
    if (mainWords != null && !mainWords.isEmpty()) {
        for (int i = 0; i < mainWords.size(); i++) {
            System.out.printf("   %2d. %s%n", i+1, mainWords.get(i).toUpperCase());
        }
    } else {
        System.out.println("   ⚠ No words provided - using defaults");
    }
    
    System.out.println("\n▶ PHASE 1B - Misspelled Words (tracked during gameplay):");
    List<String> misspelled = Game.getInstance().getMisspelledWordList();
    if (misspelled != null && !misspelled.isEmpty()) {
        for (int i = 0; i < misspelled.size(); i++) {
            System.out.printf("   %2d. %s%n", i+1, misspelled.get(i).toUpperCase());
        }
    } else {
        System.out.println("   (Will be populated as player misspells words)");
    }
    
    System.out.println("\n▶ PHASE 1C - Final Battle Words (harder difficulty):");
    String[] finalWords = {"LASER", "BUILD", "LEVEL", "BLUE", "LETTER"};
    for (int i = 0; i < finalWords.length; i++) {
        System.out.printf("   %2d. %s%n", i+1, finalWords[i]);
    }
    
    System.out.println("\n═══════════════════════════════════════════════════════════════\n");
}

    // ── Sparkle ───────────────────────────────────────────────────────────────

    private void paintSparkle(Graphics2D g2) {
        if (imgSparkle == null || !showSparkle) return;
        int scale = 3;
        int sx = sparkleFrame * SPKL_W;
        g2.drawImage(imgSparkle,
                sparkleX - SPKL_W, sparkleY - SPKL_H,
                sparkleX + SPKL_W*scale, sparkleY + SPKL_H*scale,
                sx, 0, sx+SPKL_W, SPKL_H, null);
    }

    // ── Left panel: banner + vertical ladder progress bar ────────────────────
// Replace paintLeftPanel with paintHealthBars
private void paintHealthBars(Graphics2D g2, int W, int H) {
    int barWidth = 280;
    int barHeight = 28;
    int heroBarX = 20;
    int mayorBarX = W - barWidth - 20;
    int barY = 20;
    
    // Hero Health Bar
    g2.setFont(new Font("Monospaced", Font.BOLD, 12));
    g2.setColor(Color.WHITE);
    drawCentredString(g2, "HERO", heroBarX + barWidth/2, barY - 5);
    
    // Background
    g2.setColor(new Color(60, 60, 60));
    g2.fillRoundRect(heroBarX, barY, barWidth, barHeight, 8, 8);
    
    // Health fill
    int heroFill = (int)((double)model.getHeroHealth() / model.getHeroMaxHealth() * barWidth);
    GradientPaint heroGrad = new GradientPaint(heroBarX, barY, new Color(0, 200, 100),
                                                 heroBarX + heroFill, barY, new Color(0, 255, 150));
    g2.setPaint(heroGrad);
    g2.fillRoundRect(heroBarX, barY, heroFill, barHeight, 8, 8);
    
    // Health text
    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Monospaced", Font.BOLD, 14));
    drawCentredString(g2, model.getHeroHealth() + "/" + model.getHeroMaxHealth(),
                      heroBarX + barWidth/2, barY + barHeight/2 + 5);
    
    // Mayor Health Bar
    g2.setColor(Color.WHITE);
    drawCentredString(g2, "MAYOR", mayorBarX + barWidth/2, barY - 5);
    
    g2.setColor(new Color(60, 60, 60));
    g2.fillRoundRect(mayorBarX, barY, barWidth, barHeight, 8, 8);
    
    // Show shield if active
    if (model.getCurrentPhase() == BattlePhase.PHASE_1B && model.getMayorShield() > 0) {
        int shieldFill = (int)((double)model.getMayorShield() / model.getShieldMaxHealth() * barWidth);
        GradientPaint shieldGrad = new GradientPaint(mayorBarX, barY, new Color(0, 100, 200),
                                                      mayorBarX + shieldFill, barY, new Color(100, 200, 255));
        g2.setPaint(shieldGrad);
        g2.fillRoundRect(mayorBarX, barY, shieldFill, barHeight, 8, 8);
        
        // Overlay shield icon text
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        drawCentredString(g2, "SHIELD: " + model.getMayorShield(), mayorBarX + barWidth/2, barY + barHeight/2 + 5);
    } else {
        int mayorFill = (int)((double)model.getMayorHealth() / model.getMayorMaxHealth() * barWidth);
        GradientPaint mayorGrad = new GradientPaint(mayorBarX, barY, new Color(200, 0, 0),
                                                     mayorBarX + mayorFill, barY, new Color(255, 50, 50));
        g2.setPaint(mayorGrad);
        g2.fillRoundRect(mayorBarX, barY, mayorFill, barHeight, 8, 8);
        
        g2.setColor(Color.WHITE);
        drawCentredString(g2, model.getMayorHealth() + "/" + model.getMayorMaxHealth(),
                          mayorBarX + barWidth/2, barY + barHeight/2 + 5);
    }
}
    
    
    // ── Top timer bar ─────────────────────────────────────────────────────────
// Update the paintTimerBar to show hint mode warning
private void paintTimerBar(Graphics2D g2, int W) {
    int tbX = 20;
    int tbY = 80;
    int tbH = 20;
    int tbW = W - 40;

    int maxTime;
    switch (model.getCurrentDifficulty()) {
        case MEDIUM: maxTime = T_MEDIUM; break;
        case HARD:   maxTime = T_HARD;   break;
        default:     maxTime = T_EASY;   break;
    }
    double pct = Math.max(0, (double) timeLeft / maxTime);
    int fillW = (int)(tbW * pct);

    // Timer background
    g2.setColor(new Color(0, 0, 0, 180));
    g2.fillRoundRect(tbX - 3, tbY - 3, tbW + 6, tbH + 6, 10, 10);

    // Pulse effect when in hint mode
    if (hintModeActive && timeLeft <= 10) {
        float alpha = 0.5f + (float)Math.sin(System.currentTimeMillis() * 0.01) * 0.3f;
        g2.setColor(new Color(1f, 0.5f, 0f, alpha));
        g2.fillRoundRect(tbX - 2, tbY - 2, tbW + 4, tbH + 4, 10, 10);
    }
    
    Color timerColor;
    if (hintModeActive && timeLeft <= 10) {
        timerColor = new Color(255, 100, 0); // Orange-red for hint mode
    } else if (pct > 0.5) {
        timerColor = UITheme.ACCENT_TEAL;
    } else if (pct > 0.25) {
        timerColor = UITheme.ACCENT_YELLOW;
    } else {
        timerColor = UITheme.ACCENT_RED;
    }
    
    g2.setColor(timerColor);
    g2.fillRoundRect(tbX, tbY, fillW, tbH, 8, 8);
    
    // Time label
    g2.setFont(UITheme.FONT_HEADING);
    g2.setColor(hintModeActive ? new Color(255, 215, 0) : Color.WHITE);
    drawCentredString(g2, timeLeft + "s", W/2, tbY + tbH/2 + 5);
    
    // Hint mode indicator
    if (hintModeActive && timeLeft <= 10) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(new Color(255, 215, 0));
        drawCentredString(g2, "⚡ HINT MODE ⚡", W/2, tbY - 8);
    }
    
    // Score on right
    int scoreW = 140;
    int scoreH = 50;
    int scoreX = 390 ;
    int scoreY = 10;
    paintOuterBoxButton(g2, scoreX, scoreY, scoreW, scoreH,
            "Score: " + model.getTotalScore(), "");
}
    // ── Intro screen ──────────────────────────────────────────────────────────

    private void paintIntro(Graphics2D g2, int W, int H) {
        // Full-page dark overlay
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, W, H);

        // FIX 1: One single outer_box card centred on screen
        // Contains: title line, description line, and Start Rescue button
        // Wide enough to fit the longest text line comfortably
        int cardW = Math.min(W - 80, 680);
        int cardH = 220;
        int cardX = W / 2 - cardW / 2;
        int cardY = H / 2 - cardH / 2;

        // Draw the outer_box as the card background
        paintOuterBoxButton(g2, cardX, cardY, cardW, cardH, "", "");  // background only

        // Title text inside card
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(new Color(0x3B2A1A));
        drawCentredString(g2, "It was the Mayor this whole time!", W / 2, cardY + 48);

        // Description text inside card — two lines if needed
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(new Color(0x5A4A3A));
        drawCentredString(g2,
                "Battle the final boss, unscramble the letters.",
                W / 2, cardY + 84);
        drawCentredString(g2,
                "Click or drag tiles into the box. Each correct word is a hit!",
                W / 2, cardY + 108);

        // Divider line
        g2.setColor(new Color(0xC8A97A));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cardX + 30, cardY + 124, cardX + cardW - 30, cardY + 124);
        g2.setStroke(new BasicStroke(1));

        // Start Rescue button INSIDE the card at the bottom
        int btnW = 260, btnH = 58;
        int btnX = W / 2 - btnW / 2;
        int btnY = cardY + cardH - btnH - 20;
        paintClickBox(g2, btnX, btnY, btnW, btnH, "Start battle!", UITheme.FONT_HEADING,
                new Color(0xFCD475), UITheme.BG_DARK, "start_rescue");
    }

    // ── Game UI: floating tiles + word box + buttons ───────────────────────

    private void paintGameUI(Graphics2D g2, int W, int H) {
        if (currentScreen == Screen.RESCUE) {
            paintRescueOverlay(g2, W, H);
            return;
        }

        int tileSize = 64;

        // ── Floating available tiles ──────────────────────────────────────────
        List<Character> avail = model.getAvailableLetters();
        if (tileBaseX == null || tileBaseX.length != avail.size()) computeTilePositions();

        for (int i = 0; i < avail.size(); i++) {
            int dx = (windX != null && i < windX.length) ? windX[i] : 0;
            int dy = (windY != null && i < windY.length) ? windY[i] : 0;
            int x  = tileBaseX[i] + dx;
            int y  = tileBaseY[i] + dy;
            paintLetterTile(g2, x, y, tileSize, String.valueOf(avail.get(i)), false, i);
        }

        // ── Bottom: outer_box word display row ────────────────────────────────
        String word     = model.getCurrentWord();
        int wordLen     = word.length();
        int slotSize    = 64, slotGap = 10;
        int totalSlotW  = wordLen * (slotSize + slotGap) - slotGap;

        // The outer box spans the word slots
        int boxPad  = 20;
        int boxW    = totalSlotW + boxPad * 2;
        int boxH    = slotSize + boxPad;
        int boxX    = (W - boxW) / 2;
        int boxY    = H - boxH - 60;

        // Draw outer_box tiled as the container
        paintOuterBox(g2, boxX, boxY, boxW, boxH);

        // Draw arrangement slots inside
        List<Character> arr = model.getPlayerArrangement();
        int slotStartX = boxX + boxPad;
        for (int i = 0; i < wordLen; i++) {
            int sx = slotStartX + i * (slotSize + slotGap);
            int sy = boxY + boxPad/2;
            if (i < arr.size()) {
                paintLetterTile(g2, sx, sy, slotSize, String.valueOf(arr.get(i)), true, i);
            } else {
                paintEmptySlot(g2, sx, sy, slotSize);
            }
        }

        // ── Back (clear) button to the LEFT of the box ────────────────────────
        int btnSize = 56;
        int backX   = boxX - btnSize - 10;
        int backY   = boxY + boxH/2 - btnSize/2;
        paintIconBox(g2, backX, backY, btnSize, imgBack, "back_btn");

        // ── Check (submit) button to the RIGHT of the box ─────────────────────
        int checkX  = boxX + boxW + 10;
        int checkY  = backY;
        paintIconBox(g2, checkX, checkY, btnSize, imgCheck, "check_btn");

        // ── SKIP button - Now beside the Check button ─────────────────────────
        int skipW = 80, skipH = 40;

        // Position it to the right of the Check button (checkX + btnSize + 10px gap)
        int skipX = checkX + btnSize + 10; 

        // Align the vertical centers of the two buttons
        int skipY = checkY + (btnSize / 2) - (skipH / 2);

        paintClickBox(g2, skipX, skipY, skipW, skipH, "Skip Word", UITheme.FONT_SMALL,
                      new Color(0xFFD700), new Color(0x3B2A1A), "skip_btn");

        skipButtonRect = new Rectangle(skipX, skipY, skipW, skipH);
        

    }  // end paintGameUI

    private void paintRescueOverlay(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(0f, 0f, 0f, 0.4f));
        g2.fillRect(0, H/2 - 40, W, 80);
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(UITheme.TEXT_BRIGHT);
        drawCentredString(g2, "City is safe!", W/2, H/2 + 8);
    }

    // ── Feedback message ──────────────────────────────────────────────────────

    private void paintFeedback(Graphics2D g2, int W, int H) {
        // FIX 3: bigger feedback box + larger font so text is easy to read
        int fw = Math.min(W - 60, 700);
        int fh = 54;   // was 36 — taller box
        int fx = (W - fw) / 2;
        int fy = (int)(H * 0.70);
        paintOuterBoxButton(g2, fx, fy, fw, fh, "", "");  // use the nicer outer_box style
        g2.setFont(UITheme.FONT_HEADING);   // was FONT_SMALL — much bigger now
        g2.setColor(feedbackColor);
        drawCentredString(g2, feedbackMsg, W / 2, fy + fh / 2 + 7);
    }

    // ═══════════════════════════════ DRAW HELPERS ════════════════════════════

    /**
     * Draw a letter tile using individual_box.png as background.
     * @param placed  true if in arrangement (tinted blue), false if available
     * @param index   index within the relevant list (used for hover/drag detection)
     */


private void paintBackButton(Graphics2D g2) {
    int padding = 14; // Distance from the edges
    int backBtnW = 120;
    int backBtnH = 40;

    // Calculate positions
    int x = padding; 
    // getHeight() gets the total height of the component
    int y = getHeight() - backBtnH - padding; 

    // NEW CHANGE B: replace banner with outer_box "Back" button
    paintOuterBoxButton(g2, x, y, backBtnW, backBtnH, "< Back", "back_to_map");

    // Update the click zone to match the new coordinates
    // Note: Fixed the last parameter from backBtnW to backBtnH
    clickZones.put("back_btn", new Rectangle(x, y, backBtnW, backBtnH));
}
    


private void paintEmptySlot(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(0,0,0,80));
        g2.fillRoundRect(x, y, size, size, 6, 6);
        if (imgLetterBox != null) {
            // Dim version of the box as empty slot
            g2.setColor(new Color(1f,1f,1f,0.2f));
            g2.fillRect(x, y, size, size);
        }
        g2.setColor(new Color(1f,1f,1f,0.4f));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                0, new float[]{4,4}, 0));
        g2.drawRoundRect(x+2, y+2, size-4, size-4, 6, 6);
        g2.setStroke(new BasicStroke(1));
    }

    /**
     * Draw a wide box using outer_box.png tiled.
     * outer_box is 32x8 — we tile it horizontally and stack for height.
     */
/**
     * NEW CHANGE B: Draw a proper button using outer_box.png as border strips.
     * - Top edge: outer_box.png stretched to full width
     * - Bottom edge: outer_box.png flipped vertically, stretched to full width
     * - Centre: filled with the outer_box mid-colour (warm cream)
     * - Text centred inside
     * - Click zone registered under zoneId
     *
     * This gives a clean pixel-art button look using only the first/last
     * 8px rows of the outer_box image as decorative borders.
     */
    private void paintOuterBoxButton(Graphics2D g2, int x, int y, int w, int h,
                                      String label, String zoneId) {
        paintOuterBox(g2, x, y, w, h);
        if (!label.isEmpty()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.setColor(new Color(0x3B2A1A));
            drawCentredString(g2, label, x + w / 2, y + h / 2 + 5);
        }
        if (!zoneId.isEmpty()) {
            clickZones.put(zoneId, new Rectangle(x, y, w, h));
        }
    }
 
    /**
     * Draw a box using outer_box.png as top/bottom border strips with a filled centre.
     * Uses the same approach as paintOuterBoxButton so nothing is cut off on the right.
     */
    private void paintOuterBox(Graphics2D g2, int x, int y, int w, int h) {
        // Centre fill — warm cream matching outer_box interior
        g2.setColor(new Color(229, 226, 207));
        g2.fillRect(x, y, w, h);
 
        if (imgOuterBox != null) {
            int stripH = Math.min(8, h / 2);
            // Top strip — stretched to full width (no tiling = no cut-off)
            g2.drawImage(imgOuterBox,
                    x, y, x + w, y + stripH,
                    0, 0, imgOuterBox.getWidth(), imgOuterBox.getHeight(), null);
            // Bottom strip — flipped vertically
            g2.drawImage(imgOuterBox,
                    x, y + h - stripH, x + w, y + h,
                    0, imgOuterBox.getHeight(), imgOuterBox.getWidth(), 0, null);
            // Left edge strip — rotated/stretched vertically
            g2.drawImage(imgOuterBox,
                    x, y + stripH, x + stripH, y + h - stripH,
                    0, 0, imgOuterBox.getHeight(), imgOuterBox.getHeight(), null);
            // Right edge strip
            g2.drawImage(imgOuterBox,
                    x + w - stripH, y + stripH, x + w, y + h - stripH,
                    imgOuterBox.getWidth() - imgOuterBox.getHeight(), 0,
                    imgOuterBox.getWidth(), imgOuterBox.getHeight(), null);
        } else {
            g2.setColor(new Color(0xC8A97A));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x+1, y+1, w-2, h-2, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }
    }
 
 

    /**
     * Draw a text label inside an outer_box styled background.
     */
    private void paintLabelBox(Graphics2D g2, int x, int y, int w, int h,
                                String text, Font font, Color textColor) {
        g2.setFont(font);
        g2.setColor(textColor);
        drawCentredString(g2, text, x + w/2, y + h/2 + 5);
    }

    /**
     * Draw a clickable button box with an icon image.
     * Also registers a click region so mouseClicked can detect it.
     */
    private java.util.Map<String, Rectangle> clickZones = new java.util.HashMap<>();

    private void paintIconBox(Graphics2D g2, int x, int y, int size,
                               BufferedImage icon, String zoneId) {
        // No outer_box background — draw icon directly
        if (icon != null) {
            int pad = 6;
            if ("back_btn".equals(zoneId)) {
                // Mirror horizontally so the arrow points LEFT
                g2.drawImage(icon,
                        x + pad + (size - pad * 2), y + pad,
                        x + pad,                    y + pad + (size - pad * 2),
                        0, 0, icon.getWidth(), icon.getHeight(), null);
            } else {
                g2.drawImage(icon, x + pad, y + pad, size - pad * 2, size - pad * 2, null);
            }
        }
        clickZones.put(zoneId, new Rectangle(x, y, size, size));
    }

    private void paintClickBox(Graphics2D g2, int x, int y, int w, int h,
                                String text, Font font, Color bgTint, Color textColor,
                                String zoneId) {
        // Slightly tinted outer box for clickable elements
        g2.setColor(new Color(bgTint.getRed(), bgTint.getGreen(), bgTint.getBlue(), 180));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        if (imgOuterBox != null) {
            // Light overlay
            g2.setColor(new Color(1f,1f,1f,0.15f));
            g2.fillRoundRect(x, y, w, h, 8, 8);
        }
        g2.setColor(new Color(0,0,0,100));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x+1, y+1, w-2, h-2, 8, 8);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(font);
        g2.setColor(textColor);
        drawCentredString(g2, text, x + w/2, y + h/2 + 5);
        clickZones.put(zoneId, new Rectangle(x, y, w, h));
    }

    private void drawCentredString(Graphics2D g2, String s, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, cx - fm.stringWidth(s)/2, cy);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mouse input — drag & drop tiles + click zones
    // ─────────────────────────────────────────────────────────────────────────
 
    /** Single source of truth for the word-box geometry (must match paintGameUI). */
    private Rectangle wordBoxGeo() {
        String word    = model.getCurrentWord();
        int wordLen    = (word == null) ? 1 : word.length();
        int slotSize   = 64, slotGap = 10;
        int totalSlotW = wordLen * (slotSize + slotGap) - slotGap;
        int boxPad     = 20;
        int boxW       = totalSlotW + boxPad * 2;
        int boxH       = slotSize + boxPad;
        int boxX       = (getWidth() - boxW) / 2;
        int boxY       = getHeight() - boxH - 60;
        return new Rectangle(boxX, boxY, boxW, boxH);
    }
 
    private void setupMouseInteraction() {
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
 
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                if (currentScreen != Screen.GAME || waitingForNext) return;
 
                int tileSize = 64;
                List<Character> avail = model.getAvailableLetters();
 
                // Try to grab an available floating tile
                if (tileBaseX != null) {
                    for (int i = 0; i < avail.size(); i++) {
                        int dx = (windX != null && i < windX.length) ? windX[i] : 0;
                        int dy = (windY != null && i < windY.length) ? windY[i] : 0;
                        int tx = tileBaseX[i] + dx;
                        int ty = tileBaseY[i] + dy;
                        if (mx >= tx && mx <= tx + tileSize && my >= ty && my <= ty + tileSize) {
                            isDragging    = true;
                            dragFromArr   = false;
                            dragTileIndex = i;
                            dragCurrentX  = mx;
                            dragCurrentY  = my;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
 
                // Try to grab a tile from the arrangement
                Rectangle box  = wordBoxGeo();
                List<Character> arr = model.getPlayerArrangement();
                int slotSize2  = 64, slotGap = 10, boxPad = 20;
                int slotStartX = box.x + boxPad;
                for (int i = 0; i < arr.size(); i++) {
                    int sx = slotStartX + i * (slotSize2 + slotGap);
                    int sy = box.y + boxPad / 2;
                    if (mx >= sx && mx <= sx + slotSize2 && my >= sy && my <= sy + slotSize2) {
                        isDragging    = true;
                        dragFromArr   = true;
                        dragTileIndex = i;
                        dragCurrentX  = mx;
                        dragCurrentY  = my;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }
                // Update the mouseReleased method - FIXED version
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    int mx = e.getX(), my = e.getY();

                    // FIRST: Check for button clicks (only when NOT dragging)
                    if (!isDragging) {
                        // Check Skip button FIRST (most specific)
                        if (skipButtonRect != null && skipButtonRect.contains(mx, my)) {
                            System.out.println("[CatGame] Skip button clicked!");
                            handleSkipWord();
                            return;
                        }

                        // Check Back to Map button
                        Rectangle backToMap = clickZones.get("back_to_map");
                        if (backToMap != null && backToMap.contains(mx, my)) {
                            SoundManager.stopMusic();
                            Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
                            //Wordtropolis.replaceScreen(Wordtropolis.SCREEN_MAP, new MapPagePanel());
                            return;
                        }

                        // Check Start Rescue button (intro screen)
                        if (currentScreen == Screen.INTRO) {
                            Rectangle r = clickZones.get("start_rescue");
                            if (r != null && r.contains(mx, my)) {
                                startGame();
                            }
                            isDragging = false;
                            return;
                        }

                        // Check game buttons
                        if (currentScreen == Screen.GAME && !waitingForNext) {
                            Rectangle check = clickZones.get("check_btn");
                            if (check != null && check.contains(mx, my)) {
                                handleSubmit();
                                return;
                            }

                            Rectangle back = clickZones.get("back_btn");
                            if (back != null && back.contains(mx, my)) {
                                model.clearArrangement();
                                computeTilePositions();
                                repaint();
                                return;
                            }
                        }
                    }

                    // Handle drag and drop logic (only if we were dragging)
                    if (isDragging && currentScreen == Screen.GAME && !waitingForNext) {
                        Rectangle box = wordBoxGeo();
                        boolean inBox = box.contains(mx, my);

                        if (!dragFromArr && inBox) {
                            // Available tile dropped into word box
                            SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.BOSS_GAME);
                            model.selectLetter(dragTileIndex);
                            computeTilePositions();
                            repaint();
                        } else if (dragFromArr && !inBox) {
                            // Arrangement tile dragged out
                            SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.BOSS_GAME);
                            model.removeLetter(dragTileIndex);
                            computeTilePositions();
                            repaint();
                        } else if (dragFromArr && inBox) {
                            // Arrangement tile dropped onto another slot
                            int slotStartX = box.x + 20; // boxPad
                            int slotSize = 64, slotGap = 10;
                            String word = model.getCurrentWord();
                            int targetSlot = -1;
                            for (int i = 0; i < word.length(); i++) {
                                int sx = slotStartX + i * (slotSize + slotGap);
                                int sy = box.y + 10; // boxPad/2
                                if (mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
                                    targetSlot = i;
                                    break;
                                }
                            }
                            if (targetSlot >= 0 && targetSlot != dragTileIndex) {
                                model.swapArrangement(dragTileIndex, targetSlot);
                                repaint();
                            }
                        }
                    }

                    // Reset drag state
                    isDragging = false;
                    dragTileIndex = -1;
                    dragFromArr = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
 
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (isDragging) {
                    dragCurrentX = e.getX();
                    dragCurrentY = e.getY();
                    repaint();
                }
            }
 
// Also update mouseMoved to NOT trigger on hover
@Override
public void mouseMoved(java.awt.event.MouseEvent e) {
    if (currentScreen != Screen.GAME) return;
    
    int mx = e.getX(), my = e.getY();
    int tileSize = 64;
    int newHoverAvail = -1, newHoverArr = -1;

    // Check available tiles
    List<Character> avail = model.getAvailableLetters();
    if (tileBaseX != null) {
        for (int i = 0; i < avail.size(); i++) {
            int dx = (windX != null && i < windX.length) ? windX[i] : 0;
            int dy = (windY != null && i < windY.length) ? windY[i] : 0;
            int tx = tileBaseX[i] + dx;
            int ty = tileBaseY[i] + dy;
            if (mx >= tx && mx <= tx + tileSize && my >= ty && my <= ty + tileSize) {
                newHoverAvail = i;
                break;
            }
        }
    }

    // Check arrangement tiles
    Rectangle box = wordBoxGeo();
    int slotSize = 64, slotGap = 10, boxPad = 20;
    int slotStartX = box.x + boxPad;
    List<Character> arr = model.getPlayerArrangement();
    for (int i = 0; i < arr.size(); i++) {
        int sx = slotStartX + i * (slotSize + slotGap);
        int sy = box.y + boxPad / 2;
        if (mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
            newHoverArr = i;
            break;
        }
    }

    // Change cursor for tiles AND skip button
    boolean overTile = (newHoverAvail >= 0 || newHoverArr >= 0);
    boolean overSkipButton = (skipButtonRect != null && skipButtonRect.contains(mx, my));
    
    setCursor((overTile || overSkipButton)
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getDefaultCursor());
    
    // Only repaint if hover changed
    boolean changed = (newHoverAvail != hoverAvailIdx || newHoverArr != hoverArrIdx);
    hoverAvailIdx = newHoverAvail;
    hoverArrIdx = newHoverArr;
    
    if (changed) repaint();
}
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }
    
    // Add the skip word handler method
private void handleSkipWord() {
    if (waitingForNext) return;
    if (currentScreen != Screen.GAME) return;
    
    System.out.println("[CatGame] Skipping word: " + model.getCurrentWord());
    
    // Add current word to timeout list
    String skippedWord = model.getCurrentWord();
    if (skippedWord != null && !skippedWord.isEmpty()) {
        model.addTimeoutWord(skippedWord);
        showFeedback("Skipped! '" + skippedWord + "' added to practice pool!", 
                    UITheme.ACCENT_YELLOW);
        SoundManager.playConditional(SoundManager.BOSS_TIMER_TICKING, SoundManager.GameActivity.BOSS_GAME);
    }
    
    // Stop current timer
    if (countdownTimer != null) {
        countdownTimer.stop();
    }
    
    // Clear arrangement and load next word
    model.clearArrangement();
    
    if (!model.isComplete() && !model.isHeroDead()) {
        model.advanceToNextWord();
        refreshGame();
    } else if (model.isHeroDead()) {
        battleComplete(false);
    } else if (model.isComplete()) {
        battleComplete(true);
    }
    
    repaint();
}

    private void startGame() {
        SoundManager.playConditional(SoundManager.CAT_GAME_START, SoundManager.GameActivity.BOSS_GAME);
        // FIX 3d: music starts when user clicks Start Rescue — not before
        SoundManager.startMusic(SoundManager.BOSS_BGM);
        // Show instruction dialog
        currentScreen = Screen.GAME;
        computeTilePositions();
        startCountdown();
        repaint();
    }
    
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        catDrawX = -1;   // force recalculate on next paint
        catDrawY = -1;
        computeTilePositions();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}