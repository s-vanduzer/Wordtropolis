package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.CatGameModel;
import com.cs4474.wordtropolis.model.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

/**
 * CatGamePanel.java
 */
public class CatGamePanel extends JPanel implements Refreshable {

    // ── Model ─────────────────────────────────────────────────────────────────
    private final CatGameModel model;

    // ── Game screens ──────────────────────────────────────────────────────────
    private enum Screen {
        INTRO, GAME, RESCUE, FINISH
    }
    private Screen currentScreen = Screen.INTRO;

    // ── Images ────────────────────────────────────────────────────────────────
    private BufferedImage imgBg, imgTree, imgCat, imgNpc, imgLadder;
    private BufferedImage imgLightPole, imgProgressBar;
    private BufferedImage imgLetterBox, imgOuterBox, imgBanner;
    private BufferedImage imgBack, imgCheck, imgSparkle;
    private BufferedImage imgHero;   // selected hero sprite sheet

    // ── Sprite constants ──────────────────────────────────────────────────────
    private static final int CAT_FRAMES = 10, CAT_FW = 32, CAT_FH = 32;
    private static final int NPC_COLS = 6, NPC_FW = 32, NPC_FH = 32;
    private static final int SPKL_FRAMES = 14, SPKL_W = 32, SPKL_H = 32;
    // Hero sprite sheet: 4 cols × 4 rows, each frame ~210×316px
    // Row 3 (last row, 0-indexed) = back-facing → hero looks toward tree
    private static final int HERO_COLS = 4;
    private static final int HERO_FW = 210;   // frame width  (843 / 4)
    private static final int HERO_FH = 316;   // frame height (1264 / 4)
    private static final int HERO_BACK_ROW = 3;    // last row = back-facing walk
    private int heroFrame = 0;
    private Timer heroTimer;

    // ── Progress bar from progress_bar.png ───────────────────────────────────
    // Row 1 (y=24) is the cyan/filled bar — we rotate it 90° to use vertically
    // Row 3 (y=72) is used as the timer bar (red/orange fill)
    private static final int PB_ROW_H = 24;
    private static final int PB_LADDER_ROW = 1;  // cyan bar for ladder progress
    private static final int PB_TIMER_ROW = 3;  // red/orange bar for timer

    // ── Animation state ───────────────────────────────────────────────────────
    private int catFrame = 0, npcFrame = 0, sparkleFrame = 0;
    private boolean showSparkle = false;
    private int sparkleX, sparkleY;
    private float catRescueY = 1.0f;

    private int catDrawX = -1;   // ADD THIS
    private int catDrawY = -1;   // ADD THIS

    private Timer catTimer, npcTimer, sparkleTimer, rescueTimer;

    // ── Floating letter tile positions ────────────────────────────────────────
    // Tiles float in screen space; wind offsets drift them
    private int[] tileBaseX, tileBaseY;   // original positions
    private int[] windX, windY;           // drift offsets
    private Timer windTimer;

    // ── Countdown timer ───────────────────────────────────────────────────────
    private static final int T_EASY = 60, T_MEDIUM = 45, T_HARD = 30, T_WARN = 10;
    private int timeLeft = T_EASY;
    private boolean warnPlayed = false;
    private Timer countdownTimer;

    // ── Difficulty ────────────────────────────────────────────────────────────
    private CatGameModel.Difficulty lastDiff = CatGameModel.Difficulty.EASY;

    // ── State flags ───────────────────────────────────────────────────────────
    private boolean waitingForNext = false;

    // ── Feedback message ──────────────────────────────────────────────────────
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;
    private Timer feedbackTimer;

    // ── Drag & Drop state ─────────────────────────────────────────────────────
    private int dragTileIndex = -1;   // index within available or arrangement list
    private boolean dragFromArr = false; // true = dragging from arrangement
    private int dragCurrentX, dragCurrentY;
    private boolean isDragging = false;
    private int hoverAvailIdx = -1;   // which available tile the cursor is over
    private int hoverArrIdx = -1;   // which arrangement tile the cursor is over

    // ─────────────────────────────────────────────────────────────────────────
    public CatGamePanel() {
        model = new CatGameModel(Game.getInstance().getWordList());
        setLayout(null);   // absolute positioning — we paint everything ourselves
        setBackground(Color.BLACK);
        loadImages();
        // FIX 3: music starts only when user clicks "Start Rescue!" not on load
        startAnimations();
        setupKeyboard();
        setupMouseInteraction();
        // Intro screen just paints — clicking Start Rescue transitions to GAME
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        SoundManager.stopMusic();
        stopAll();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // FIX 3: only request focus here — music is started in startGame()
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    public void refresh() {
        clickZones.clear();
        currentScreen = Screen.INTRO;
        feedbackMsg = "";

        imgHero = loadHeroImage();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
        repaint();
    }

    // ═══════════════════════════════ LOADING ═════════════════════════════════
    private void loadImages() {
        imgBg = img("/images/cat_game/city_background.png");
        imgTree = img("/images/cat_game/tree.png");
        imgCat = img("/images/cat_game/cat.png");
        imgNpc = img("/images/cat_game/npc.png");
        imgLadder = img("/images/cat_game/ladder.png");
        imgLightPole = img("/images/cat_game/light_pole.png");
        imgProgressBar = img("/images/cat_game/progress_bar.png");
        imgLetterBox = img("/images/cat_game/individual_box.png");
        imgOuterBox = img("/images/cat_game/outer_box.png");
        imgBanner = img("/images/cat_game/banner.png");
        imgBack = img("/images/cat_game/back.png");
        imgCheck = img("/images/cat_game/check.png");
        imgSparkle = img("/images/cat_game/blue_sparkle.png");
    }

    private BufferedImage loadHeroImage() {
        try {
            String avatar = Game.getInstance().getAvatarPath();
            System.out.println("[Fire] Trying to load: " + avatar);
            if ("Mia".equalsIgnoreCase(avatar)) {
                return img("/images/general/hero1_standing.png");
            }
            if ("Zyx".equalsIgnoreCase(avatar)) {
                return img("/images/general/alien_standing.png");
            }
            return img("/images/general/hero2_standing.png");
        } catch (Exception ignored) {
            System.out.println("[Cat] Error loading hero");
        }
        return null;
    }

    private BufferedImage img(String path) {
        try {
            URL r = getClass().getResource(path);
            if (r == null) {
                System.out.println("[Cat] missing: " + path);
                return null;
            }
            return ImageIO.read(r);
        } catch (Exception e) {
            System.out.println("[Cat] err: " + path);
            return null;
        }
    }

    // ═══════════════════════════════ ANIMATIONS ══════════════════════════════
    private void startAnimations() {
        catTimer = new Timer(150, e -> {
            catFrame = (catFrame + 1) % CAT_FRAMES;
            repaint();
        });
        catTimer.start();
        npcTimer = new Timer(200, e -> {
            npcFrame = (npcFrame + 1) % NPC_COLS;
            repaint();
        });
        npcTimer.start();
        heroTimer = new Timer(180, e -> {
            heroFrame = (heroFrame + 1) % HERO_COLS;
            repaint();
        });
        heroTimer.start();
    }

    private void triggerSparkle(int x, int y) {
        sparkleX = x;
        sparkleY = y;
        sparkleFrame = 0;
        showSparkle = true;
        if (sparkleTimer != null) {
            sparkleTimer.stop();
        }
        sparkleTimer = new Timer(55, e -> {
            if (++sparkleFrame >= SPKL_FRAMES) {
                showSparkle = false;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        sparkleTimer.start();
    }

    private void stopAll() {
        for (Timer t : new Timer[]{catTimer, npcTimer, heroTimer, sparkleTimer, rescueTimer,
            windTimer, countdownTimer, feedbackTimer}) {
            if (t != null) {
                t.stop();
            }
        }
    }

    // ═══════════════════════════════ KEYBOARD ════════════════════════════════
    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (currentScreen != Screen.GAME || waitingForNext) {
                    return;
                }
                char key = e.getKeyChar();
                // Ignore control characters — backspace/enter/escape handled in keyPressed
                if (key == KeyEvent.CHAR_UNDEFINED || key < 32) {
                    return;
                }
                switch (model.typeKey(key)) {
                    case TYPED_OK:
                        SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.CAT_GAME);
                        triggerSparkle(getWidth() / 2, getHeight() / 2 - 60);
                        repaint();
                        break;
                    case TYPED_NOT_AVAILABLE:
                        SoundManager.playConditional(SoundManager.CAT_ERROR, SoundManager.GameActivity.CAT_GAME);
                        shakeEffect();
                        showFeedback("'" + Character.toUpperCase(key) + "' is not available!", UITheme.ACCENT_RED);
                        break;
                    case TYPED_FULL:
                        showFeedback("Press Submit!", UITheme.ACCENT_YELLOW);
                        break;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (currentScreen != Screen.GAME || waitingForNext) {
                    return;
                }
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_BACK_SPACE && model.backspace()) {
                    SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.CAT_GAME);
                    repaint();
                } else if (c == KeyEvent.VK_ENTER) {
                    handleSubmit();
                } else if (c == KeyEvent.VK_ESCAPE) {
                    model.clearArrangement();
                    repaint();
                }
            }
        });
    }

    // ═══════════════════════════════ TILE POSITIONS ═══════════════════════════
    /**
     * Compute the floating positions for the available letter tiles. They
     * spread out in a gentle arc in the centre of the screen.
     */
    private void computeTilePositions() {
        int count = model.getAvailableLetters().size();
        tileBaseX = new int[count];
        tileBaseY = new int[count];
        windX = new int[count];
        windY = new int[count];

        int W = getWidth(), H = getHeight();
        int tileSize = 56;
        int totalW = count * (tileSize + 10) - 10;
        int startX = (W - totalW) / 2;
        int baseY = (int) (H * 0.45f);   // vertically centred in scene area

        // Gentle arc: y varies sinusoidally
        for (int i = 0; i < count; i++) {
            tileBaseX[i] = startX + i * (tileSize + 10);
            double angle = Math.PI * i / Math.max(1, count - 1);
            tileBaseY[i] = baseY - (int) (Math.sin(angle) * 20);
        }
    }

    // ═══════════════════════════════ GAME LOGIC ══════════════════════════════
    private void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        switch (model.getCurrentDifficulty()) {
            case MEDIUM:
                timeLeft = T_MEDIUM;
                break;
            case HARD:
                timeLeft = T_HARD;
                break;
            default:
                timeLeft = T_EASY;
                break;
        }
        warnPlayed = false;
        countdownTimer = new Timer(1000, e -> {
            if (--timeLeft <= 0) {
                countdownTimer.stop();
                showFeedback("Time's up! Try again.", UITheme.ACCENT_RED);
                SoundManager.playConditional(SoundManager.CAT_ERROR, SoundManager.GameActivity.CAT_GAME);
                model.clearArrangement();
                startCountdown();
                repaint();
            } else if (timeLeft == T_WARN && !warnPlayed) {
                SoundManager.playConditional(SoundManager.CAT_MEOW_MAD, SoundManager.GameActivity.CAT_GAME);
                warnPlayed = true;
            }
            repaint();
        });
        countdownTimer.start();
    }

    private void startWindEffect() {
        if (windTimer != null) {
            windTimer.stop();
        }

        // FIX 3a: play wind sound every time tile drifting starts
        int prevVol = SoundManager.getSfxVolume();
        SoundManager.setSfxVolume(30);                           // quiet wind
        SoundManager.playFromConditional(SoundManager.CAT_WIND, 0.0, 1.5, SoundManager.GameActivity.CAT_GAME);
        SoundManager.setSfxVolume(prevVol);

        java.util.Random rand = new java.util.Random();
        int DMAX = 55, DSTEP = 7;
        windTimer = new Timer(80, e -> {
            if (windX == null) {
                return;
            }
            for (int i = 0; i < windX.length; i++) {
                windX[i] = clamp(windX[i] + (rand.nextInt(3) - 1) * DSTEP, -DMAX, DMAX);
                windY[i] = clamp(windY[i] + (rand.nextInt(3) - 1) * DSTEP, -DMAX, DMAX);
            }
            repaint();
        });
        windTimer.start();
    }

    private void stopWindEffect() {
        if (windTimer != null) {
            windTimer.stop();
            windTimer = null;
        }
        windX = windY = null;
    }

    private void handleSubmit() {
        if (waitingForNext) {
            return;
        }
        if (model.getPlayerArrangement().isEmpty()) {
            showFeedback("Pick some letters first!", UITheme.ACCENT_YELLOW);
            return;
        }
        if (model.getPlayerArrangement().size() < model.getCurrentWord().length()) {
            showFeedback("Place all letters!", UITheme.ACCENT_YELLOW);
            return;
        }
        boolean correct = model.submitAnswer();
        if (correct) {
            showFeedback("Great spelling! The ladder is growing!", UITheme.ACCENT_TEAL);
            SoundManager.playFromConditional(SoundManager.CAT_CORRECT, 1.0, 1.5, SoundManager.GameActivity.CAT_GAME);
            SoundManager.playConditional(SoundManager.CAT_MEOW_HAPPY, SoundManager.GameActivity.CAT_GAME);
            triggerSparkle(getWidth() / 2, getHeight() / 2);
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            stopWindEffect();
            repaint();
            if (model.isComplete()) {
                rescueComplete();
            } else {
                waitingForNext = true;
                new Timer(1400, e -> {
                    model.loadNextWord();
                    refreshGame();
                    ((Timer) e.getSource()).stop();
                }).start();
            }
        } else {
            showFeedback("Incorrect. Please try again!", UITheme.ACCENT_RED);
            SoundManager.playConditional(SoundManager.CAT_MEOW_MAD, SoundManager.GameActivity.CAT_GAME);
            SoundManager.playConditional(SoundManager.CAT_ERROR, SoundManager.GameActivity.CAT_GAME);
            shakeEffect();
            repaint();
        }
    }

    private void refreshGame() {
        waitingForNext = false;
        computeTilePositions();
        speakWord();
        startCountdown();
        CatGameModel.Difficulty d = model.getCurrentDifficulty();
        if (d != lastDiff) {
            lastDiff = d;
        }
        // FIX 3c: startWindEffect() now plays wind sound itself at start of each drift
        if (d != CatGameModel.Difficulty.EASY) {
            startWindEffect();
        } else {
            stopWindEffect();
        }
        repaint();
    }

    private void rescueComplete() {
        feedbackMsg = "";
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        stopWindEffect();
        if (feedbackTimer != null) {
            feedbackTimer.stop();
        }

        SoundManager.playConditional(SoundManager.CAT_LEVEL_DONE, SoundManager.GameActivity.CAT_GAME);
        Game.getInstance().setCatCompleted(true);
        currentScreen = Screen.RESCUE;

        catRescueY = 0.0f;
        rescueTimer = new Timer(55, e -> {
            catRescueY += 0.015f;
            if (catRescueY >= 1.0f) {
                catRescueY = 1.0f;
                ((Timer) e.getSource()).stop();
                SoundManager.stopMusic();
                stopAll();
                currentScreen = Screen.FINISH;
            }
            repaint();
        });
        rescueTimer.start();
    }

    private void speakWord() {
        String w = model.getCurrentWord();
        if (w == null || w.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                String os = System.getProperty("os.name", "").toLowerCase();

                if (os.contains("mac")) {
                    // macOS — built-in say command, Alex voice at clear speed
                    // [[volm 1.0]] sets TTS engine volume to maximum
                    new ProcessBuilder(
                            "say", "-v", "Alex", "-r", "110",
                            "[[volm 1.0]] " + w)
                            .start().waitFor();

                } else if (os.contains("win")) {
                    // Windows — PowerShell SpeechSynthesizer (built-in, no install)
                    String ps = "Add-Type -AssemblyName System.Speech;"
                            + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer;"
                            + "$s.Rate = -2;" // -10 slowest, 10 fastest; -2 = clear
                            + "$s.Volume = 100;" // 0-100
                            + "$s.Speak('" + w.replace("'", "") + "');";
                    new ProcessBuilder("powershell", "-Command", ps)
                            .start().waitFor();

                } else {
                    // Linux — espeak (install: sudo apt-get install espeak)
                    new ProcessBuilder("espeak", "-s", "120", "-a", "200", w)
                            .start().waitFor();
                }
            } catch (Exception e) {
                // TTS not available on this machine — silent fail, game continues
                System.out.println("[Cat] TTS not available: " + e.getMessage());
            }
        }, "tts-thread").start();
    }

    private void showFeedback(String msg, Color c) {
        feedbackMsg = msg;
        feedbackColor = c;
        if (feedbackTimer != null) {
            feedbackTimer.stop();
        }
        feedbackTimer = new Timer(3000, e -> {
            feedbackMsg = "";
            repaint();
            ((Timer) e.getSource()).stop();
        });
        feedbackTimer.start();
        repaint();
    }

    // ── Shake: flashes the panel red + plays wind briefly ────────────────────
    private Timer shakeTimer;
    int shakeTick;

    private void shakeEffect() {
        if (shakeTimer != null && shakeTimer.isRunning()) {
            return;
        }

        // FIX 3b: play wind sound at the start of every shake/wrong-answer effect
        int prevVol = SoundManager.getSfxVolume();
        SoundManager.setSfxVolume(25);                           // very subtle wind
        SoundManager.playFromConditional(SoundManager.CAT_WIND, 0.0, 0.8, SoundManager.GameActivity.CAT_GAME);
        SoundManager.setSfxVolume(prevVol);

        shakeTick = 0;
        shakeTimer = new Timer(40, e -> {
            if (++shakeTick > 8) {
                shakeTimer.stop();
                repaint();
            } else {
                repaint();
            }
        });
        shakeTimer.start();
    }

    // ═══════════════════════════════ PAINTING ════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int W = getWidth(), H = getHeight();

        // ── 1. Background — fills the whole panel ─────────────────────────────
        paintBackground(g2, W, H);

        // ── 3. NPCs at the bottom ─────────────────────────────────────────────
        paintNpcs(g2, W, H);

        // ── 3b. Hero — centre-right, back-facing so they look toward the tree ─
        paintHero(g2, W, H);

        // ── 4. Tree centred ───────────────────────────────────────────────────
        int treeW = (int) (W * 0.28), treeH = (int) (H * 0.55);
        int treeX = (W - treeW) / 2, treeY = (int) (H * 0.20);
        paintTree(g2, treeX, treeY, treeW, treeH);

        // ── 5. Ladder (appears when words are solved) ─────────────────────────
        paintLadder(g2, treeX, treeY, treeW, treeH, H);

        // ── 6. Cat on top of tree ─────────────────────────────────────────────
        paintCat(g2, treeX, treeY, treeW);

        // ── 7. Sparkle effect ─────────────────────────────────────────────────
        if (showSparkle) {
            paintSparkle(g2);
        }

        // ── 8. Left panel: banner + vertical progress bar (ladder progress) ───
        paintLeftPanel(g2, H);

        // ── 9. Top: timer bar ─────────────────────────────────────────────────
        paintTimerBar(g2, W);

        if (null == currentScreen) {
            paintGameUI(g2, W, H);
        } else // -- 10. Intro, Game, or Finish overlays --
        {
            switch (currentScreen) {
                case INTRO ->
                    paintIntro(g2, W, H);
                case FINISH ->
                    paintFinishOverlay(g2, W, H);
                default ->
                    paintGameUI(g2, W, H);
            }
        }

        // ── 11. Drag ghost — semi-transparent tile following the cursor ──────────
        if (isDragging && dragTileIndex >= 0) {
            int ghostSize = 64;
            int gx = dragCurrentX - ghostSize / 2;
            int gy = dragCurrentY - ghostSize / 2;
            String ghostLetter = "";
            if (!dragFromArr) {
                List<Character> avail = model.getAvailableLetters();
                if (dragTileIndex < avail.size()) {
                    ghostLetter = String.valueOf(avail.get(dragTileIndex));
                }
            } else {
                List<Character> arr = model.getPlayerArrangement();
                if (dragTileIndex < arr.size()) {
                    ghostLetter = String.valueOf(arr.get(dragTileIndex));
                }
            }
            Graphics2D gg = (Graphics2D) g2.create();
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
            if (imgLetterBox != null) {
                gg.drawImage(imgLetterBox, gx, gy, ghostSize, ghostSize, null);
            } else {
                gg.setColor(UITheme.BG_CARD);
                gg.fillRoundRect(gx, gy, ghostSize, ghostSize, 8, 8);
            }
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
        if (!feedbackMsg.isEmpty()) {
            paintFeedback(g2, W, H);
        }

        // ── 12. Shake flash ───────────────────────────────────────────────────
        if (shakeTimer != null && shakeTimer.isRunning() && shakeTick % 2 == 0) {
            g2.setColor(new Color(1f, 0f, 0f, 0.12f));
            g2.fillRect(0, 0, W, H);
        }

        g2.dispose();
    }

    // ── Background ────────────────────────────────────────────────────────────
    private void paintBackground(Graphics2D g2, int W, int H) {
        if (imgBg != null) {
            // Scale to fill, centred
            int bw = imgBg.getWidth(), bh = imgBg.getHeight();
            double scaleW = (double) W / bw, scaleH = (double) H / bh;
            double scale = Math.max(scaleW, scaleH);
            int dw = (int) (bw * scale), dh = (int) (bh * scale);
            int dx = (W - dw) / 2, dy = (H - dh) / 2;
            g2.drawImage(imgBg, dx, dy, dw, dh, null);
        } else {
            g2.setPaint(new GradientPaint(0, 0, UITheme.SKY_TOP, 0, H * 0.7f, UITheme.SKY_BOT));
            g2.fillRect(0, 0, W, H);
            g2.setColor(UITheme.GRASS_COLOR);
            g2.fillRect(0, (int) (H * 0.85), W, (int) (H * 0.15));
        }
    }

    // ── NPCs ─────────────────────────────────────────────────────────────────
    private void paintNpcs(Graphics2D g2, int W, int H) {
        if (imgNpc == null) {
            return;
        }

        int npcScale = 4;
        int fw = NPC_FW * npcScale;   // drawn width  (96px)
        int fh = NPC_FH * npcScale;   // drawn height (96px)
        int groundY = (int) (H * 0.87) - fh;

        int srcY = 1 * 32;
        int srcX = npcFrame * NPC_FW;

        // ── 3 NPCs on the LEFT side of the tree ──────────────────────────────
        int[] leftPositions = {
            (int) (W * 0.11), // leftmost
            (int) (W * 0.18), // middle-left
            (int) (W * 0.25), // closest to tree
        };
        for (int x : leftPositions) {

            g2.drawImage(imgNpc,
                    x, groundY, //destination top-left
                    x + fw, groundY + fh, //destination bottom-right
                    srcX, srcY, //source top-left
                    srcX + NPC_FW, srcY + NPC_FH, //source bottom-right
                    null);
        }

        // ── 3 NPCs on the RIGHT side of the tree ─────────────────────────────
        int[] rightPositions = {
            (int) (W * 0.65), // closest to tree
            (int) (W * 0.72), // middle-right
            (int) (W * 0.79), // rightmost
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
     * Draw the selected hero at ground level, slightly right of centre, using
     * the back-facing row so they appear to be looking at the tree.
     *
     * Sprite sheet layout: 4 cols × 4 rows, each frame HERO_FW × HERO_FH px.
     * Row 3 (HERO_BACK_ROW) = back-facing walk cycle.
     */
    private void paintHero(Graphics2D g2, int W, int H) {
        if (imgHero == null) {
            return;
        }

        // Drawn size — scale down to look proportional next to NPCs
        int drawW = 160;
        int drawH = (int) ((double) HERO_FH / HERO_FW * drawW);  // keep aspect ratio

        // Ground line matches NPC ground Y
        int groundY = (int) (H * 0.87) - drawH;

        // Position: just right of the tree centre
        int heroX = (int) (W * 0.50);

        // Source rectangle: back-facing row, current animation frame
        int srcX1 = heroFrame * HERO_FW;
        int srcY1 = HERO_BACK_ROW * HERO_FH;
        int srcX2 = srcX1 + HERO_FW;
        int srcY2 = srcY1 + HERO_FH;

        g2.drawImage(imgHero,
                heroX, groundY, heroX + drawW, groundY + drawH,
                srcX1, srcY1 + 100, srcX2, srcY2 + 50, null);
    }

    // ── Tree ─────────────────────────────────────────────────────────────────
    private void paintTree(Graphics2D g2, int tx, int ty, int tw, int th) {
        if (imgTree != null) {
            g2.drawImage(imgTree, tx, ty + 30, tw, th + 30, null);
        } else {
            g2.setColor(UITheme.TREE_TRUNK);
            g2.fillRoundRect(tx + tw / 3, ty + th / 2, tw / 3, th / 2, 8, 8);
            g2.setColor(UITheme.TREE_LEAVES);
            g2.fillOval(tx, ty, tw, th / 2);
        }
    }

    // ── Ladder ────────────────────────────────────────────────────────────────
    private void paintLadder(Graphics2D g2, int tx, int ty, int tw, int th, int H) {
        int rungs = model.getWordsCompleted();
        if (rungs == 0) {
            return;
        }

        int ladderW = (int) (tw * 0.2);
        int ladderX = tx + tw / 2 - ladderW - 4;
        int ladderBotY = ty + th + 60;

        if (imgLadder != null) {
            int lfw = ladderW;
            int lfh = imgLadder.getHeight() * ladderW / imgLadder.getWidth();

            for (int i = 0; i < rungs; i++) {
                int yy = ladderBotY - (i + 1) * lfh;
                g2.drawImage(imgLadder, ladderX, yy, lfw, lfh, null);
            }
        } else {
            int rungHeight = 20; // fallback height
            for (int i = 0; i < rungs; i++) {
                int yy = ladderBotY - (i + 1) * rungHeight;
                g2.fillRect(ladderX, yy, ladderW, rungHeight);
            }
        }
    }

    // ── Cat ──────────────────────────────────────────────────────────────────
    private void paintCat(Graphics2D g2, int tx, int ty, int tw) {
        if (imgCat == null) {
            return;
        }

        int catScale = 3;
        int cw = CAT_FW * catScale;
        int ch = CAT_FH * catScale;

        // Compute position only once when not yet set, or during rescue animation
        if (catDrawX < 0 || currentScreen == Screen.RESCUE) {
            catDrawX = tx + tw / 2 - cw / 2 + 10;
        }

        if (currentScreen == Screen.RESCUE) {
            int treeH = (int) (getHeight() * 0.55);
            catDrawY = ty + (int) (catRescueY * treeH) - ch;
        } else {
            // Only set Y once — never recalculate unless window resizes
            if (catDrawY < 0) {
                catDrawY = ty - ch / 2 + 10;
            }
        }

        // Source X is the ONLY thing that changes — picks which frame to show
        int sx = catFrame * CAT_FW;

        g2.drawImage(imgCat,
                catDrawX + 20, catDrawY + 30, // destination: FIXED position
                catDrawX + cw + 20, catDrawY + ch + 30, // destination: FIXED size
                sx, 0, // source: only X changes per frame
                sx + CAT_FW, CAT_FH, // source: fixed height
                null);
    }

    // ── Sparkle ───────────────────────────────────────────────────────────────
    private void paintSparkle(Graphics2D g2) {
        if (imgSparkle == null || !showSparkle) {
            return;
        }
        int scale = 3;
        int sx = sparkleFrame * SPKL_W;
        g2.drawImage(imgSparkle,
                sparkleX - SPKL_W, sparkleY - SPKL_H,
                sparkleX + SPKL_W * scale, sparkleY + SPKL_H * scale,
                sx, 0, sx + SPKL_W, SPKL_H, null);
    }

    // ── Left panel: banner + vertical ladder progress bar ────────────────────
    private void paintLeftPanel(Graphics2D g2, int H) {
        int panelX = 14;

        // NEW CHANGE B: replace banner with outer_box "Back" button
        // Uses only the outer_box.png image (top + bottom strips, centre filled)
        // Clicking it returns the player to the map screen
        int backBtnW = 120, backBtnH = 40;
        paintOuterBoxButton(g2, panelX, 12, backBtnW, backBtnH, "< Back", "back_to_map");

        // Vertical progress bar using progress_bar.png strip (ladder progress)
        // We take the first coloured row (row 1 = cyan) and rotate 90° to draw vertically
        int barX = panelX + 10;
        int barTop = 80;
        int barH = Math.min(H - 80, 200);
        int barW = 18;

        // Background (dark strip)
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(barX - 2, barTop - 2, barW + 4, barH + 4, 6, 6);

        // Filled portion
        int filled = (int) ((double) model.getWordsCompleted() / model.getTotalWords() * barH);
        if (imgProgressBar != null && filled > 0) {
            // Rotate 90° so the horizontal sprite runs vertically; crop dark border pixels
            int srcX1 = 10, srcX2 = 35;
            int srcY1 = (PB_LADDER_ROW * PB_ROW_H) + 15;
            int srcY2 = ((PB_LADDER_ROW + 1) * PB_ROW_H) - 10;
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.setClip(barX, barTop + barH - filled, barW, filled);
            g3.translate(barX + barW / 2.0, barTop + barH / 2.0);
            g3.rotate(Math.PI / 2);
            int drawLen = barH, drawThk = barW;
            g3.drawImage(imgProgressBar,
                    -drawLen / 2, -drawThk / 2, drawLen / 2, drawThk / 2,
                    srcX1, srcY1, srcX2, srcY2, null);
            g3.dispose();
        } else {
            g2.setColor(UITheme.ACCENT_TEAL);
            g2.fillRect(barX, barTop + barH - filled, barW, filled);
        }
    }

    // ── Top timer bar ─────────────────────────────────────────────────────────
    private void paintTimerBar(Graphics2D g2, int W) {
        // FIX 2: taller bar + zoom into ONE bar from the sprite sheet
        int tbX = 160, tbY = 8;
        int tbH = 40;
        // Leave 160px on the right for the score box (140px wide + 10px gap + 10px margin)
        int tbW = W - 320;   // reduced to make room for score box on the right

        int maxTime;
        switch (model.getCurrentDifficulty()) {
            case MEDIUM:
                maxTime = T_MEDIUM;
                break;
            case HARD:
                maxTime = T_HARD;
                break;
            default:
                maxTime = T_EASY;
                break;
        }
        double pct = Math.max(0, (double) timeLeft / maxTime);
        int fillW = (int) (tbW * pct);

        // Dark background track
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(tbX - 3, tbY - 3, tbW + 6, tbH + 6, 10, 10);

        // Empty track (dim version of bar sprite)
        if (imgProgressBar != null) {
            // Source: one full bar row — srcX from 5 to 235 focuses on the bar only,
            // excluding the black edge pixels (first ~5px and last ~20px are dark border)
            // This zooms into just the colourful bar content
            int srcX1 = 10, srcX2 = 35;   // crop to active bar region only
            int srcY1 = (PB_TIMER_ROW * PB_ROW_H) + 15;
            int srcY2 = ((PB_TIMER_ROW + 1) * PB_ROW_H) - 10;

            // Draw empty (dimmed) full track
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.drawImage(imgProgressBar,
                    tbX, tbY, tbX + tbW, tbY + tbH,
                    srcX1, srcY1, srcX2, srcY2, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Draw filled portion clipped to fillW
            if (fillW > 0) {
                g2.setClip(tbX, tbY, fillW, tbH);
                g2.drawImage(imgProgressBar,
                        tbX, tbY, tbX + tbW, tbY + tbH, // dest: full width stretched
                        srcX1, srcY1, srcX2, srcY2, // src: zoomed into one bar
                        null);
                g2.setClip(null);
            }
        } else {
            // Fallback solid bar
            Color timerColor = pct > 0.5 ? UITheme.ACCENT_TEAL
                    : pct > 0.25 ? UITheme.ACCENT_YELLOW : UITheme.ACCENT_RED;
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(tbX, tbY, tbW, tbH, 8, 8);
            g2.setColor(timerColor);
            g2.fillRoundRect(tbX, tbY, fillW, tbH, 8, 8);
        }

        // Time label centred on the bar
        paintLabelBox(g2, tbX + tbW / 2 - 36, tbY + tbH / 2 - 14, 72, 28,
                timeLeft + "s", UITheme.FONT_HEADING, Color.WHITE);

        // ── Score box: same height as timer, flush to the RIGHT edge ──────────
        // Uses paintOuterBoxButton so it gets the same outer_box.png border
        // as the Back button — cream fill + top/bottom strip borders
        int scoreW = 140, scoreH = tbH;
        int scoreX = tbX + tbW + 20;   // 10px gap after timer bar right edge
        int scoreY = tbY;
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
        drawCentredString(g2, "Cat Stuck in a Tree!", W / 2, cardY + 48);

        // Description text inside card — two lines if needed
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(new Color(0x5A4A3A));
        drawCentredString(g2,
                "Listen to the word spoken aloud, then unscramble the letters.",
                W / 2, cardY + 84);
        drawCentredString(g2,
                "Click or drag tiles into the box. Each correct word builds the ladder!",
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
        paintClickBox(g2, btnX, btnY, btnW, btnH, "Start Rescue!", UITheme.FONT_HEADING,
                new Color(0xFCD475), UITheme.BG_DARK, "start_rescue");
    }

    // ── Game UI: floating tiles + word box + buttons ───────────────────────
    private void paintGameUI(Graphics2D g2, int W, int H) {
        if (currentScreen == Screen.RESCUE) {
            return;
        }

        int tileSize = 64;

        // ── Floating available tiles ──────────────────────────────────────────
        List<Character> avail = model.getAvailableLetters();
        if (tileBaseX == null || tileBaseX.length != avail.size()) {
            computeTilePositions();
        }

        for (int i = 0; i < avail.size(); i++) {
            int dx = (windX != null && i < windX.length) ? windX[i] : 0;
            int dy = (windY != null && i < windY.length) ? windY[i] : 0;
            int x = tileBaseX[i] + dx;
            int y = tileBaseY[i] + dy;
            paintLetterTile(g2, x, y, tileSize, String.valueOf(avail.get(i)), false, i);
        }

        // ── Bottom: outer_box word display row ────────────────────────────────
        String word = model.getCurrentWord();
        int wordLen = word.length();
        int slotSize = 64, slotGap = 10;
        int totalSlotW = wordLen * (slotSize + slotGap) - slotGap;

        // The outer box spans the word slots
        int boxPad = 20;
        int boxW = totalSlotW + boxPad * 2;
        int boxH = slotSize + boxPad;
        int boxX = (W - boxW) / 2;
        int boxY = H - boxH - 60;

        // Draw outer_box tiled as the container
        paintOuterBox(g2, boxX, boxY, boxW, boxH);

        // Draw arrangement slots inside
        List<Character> arr = model.getPlayerArrangement();
        int slotStartX = boxX + boxPad;
        for (int i = 0; i < wordLen; i++) {
            int sx = slotStartX + i * (slotSize + slotGap);
            int sy = boxY + boxPad / 2;
            if (i < arr.size()) {
                paintLetterTile(g2, sx, sy, slotSize, String.valueOf(arr.get(i)), true, i);
            } else {
                paintEmptySlot(g2, sx, sy, slotSize);
            }
        }

        // ── Back (clear) button to the LEFT of the box ────────────────────────
        int btnSize = 56;
        int backX = boxX - btnSize - 10;
        int backY = boxY + boxH / 2 - btnSize / 2;
        paintIconBox(g2, backX, backY, btnSize, imgBack, "back_btn");

        // ── Check (submit) button to the RIGHT of the box ─────────────────────
        int checkX = boxX + boxW + 10;
        int checkY = backY;
        paintIconBox(g2, checkX, checkY, btnSize, imgCheck, "check_btn");

    }  // end paintGameUI

    private void paintFinishOverlay(Graphics2D g2, int W, int H) {
        // 1. Full-page dark overlay (dimming the background)
        g2.setColor(new Color(0, 0, 0, 180)); // Slightly darker for the finish
        g2.fillRect(0, 0, W, H);

        // 2. Card Dimensions (Matching your Intro style)
        int cardW = Math.min(W - 80, 600);
        int cardH = 320; // Slightly taller to fit stats
        int cardX = W / 2 - cardW / 2;
        int cardY = H / 2 - cardH / 2;

        // 3. Draw the Card Background
        paintOuterBoxButton(g2, cardX, cardY, cardW, cardH, "", "");

        // 4. Header: Congratulatory Message
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(new Color(0x3B2A1A));
        drawCentredString(g2, "Mission Accomplished!", W / 2, cardY + 50);

        // 5. Stats Section
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(new Color(0x5A4A3A));

        // Y-offsets for stats
        int statsStartY = cardY + 100;

        // You'll need to ensure these methods exist in your model/logic
        // If your variables are named differently, swap them here:
        drawCentredString(g2, "Total Score: " + Game.getInstance().getScore(), W / 2, statsStartY);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCentredString(g2, "Correct Guesses: " + model.getTotalScore(), W / 2, statsStartY + 35);
        drawCentredString(g2, "Mistakes Made: " + model.getIncorrectAttempts(), W / 2, statsStartY + 65);

        // 6. Final Praise
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(new Color(0x2D5A27)); // Dark green for a "win" feel
        drawCentredString(g2, "The cat is safe. Thank you Hero!", W / 2, cardY + 210);

        // 7. Divider Line
        g2.setColor(new Color(0xC8A97A));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cardX + 60, cardY + 235, cardX + cardW - 60, cardY + 235);
        g2.setStroke(new BasicStroke(1));

        // 8. Back to Map Button (Matching the "Start" button style)
        int btnW = 240, btnH = 50;
        int btnX = W / 2 - btnW / 2;
        int btnY = cardY + cardH - btnH - 20;

        // Using your click box style with a gold tint
        paintClickBox(g2, btnX, btnY, btnW, btnH, "Back to Map", UITheme.FONT_HEADING,
                new Color(0xFCD475), UITheme.BG_DARK, "back_to_map");
    }

    // ── Feedback message ──────────────────────────────────────────────────────
    private void paintFeedback(Graphics2D g2, int W, int H) {
        // FIX 3: bigger feedback box + larger font so text is easy to read
        int fw = Math.min(W - 60, 700);
        int fh = 54;   // was 36 — taller box
        int fx = (W - fw) / 2;
        int fy = (int) (H * 0.70);
        paintOuterBoxButton(g2, fx, fy, fw, fh, "", "");  // use the nicer outer_box style
        g2.setFont(UITheme.FONT_HEADING);   // was FONT_SMALL — much bigger now
        g2.setColor(feedbackColor);
        drawCentredString(g2, feedbackMsg, W / 2, fy + fh / 2 + 7);
    }

    // ═══════════════════════════════ DRAW HELPERS ════════════════════════════
    /**
     * Draw a letter tile using individual_box.png as background.
     *
     * @param placed true if in arrangement (tinted blue), false if available
     * @param index index within the relevant list (used for hover/drag
     * detection)
     */
    private void paintLetterTile(Graphics2D g2, int x, int y, int size,
            String letter, boolean placed, int index) {
        // Skip tile currently being dragged — ghost draws instead
        if (isDragging && dragTileIndex == index && dragFromArr == placed) {
            return;
        }

        if (imgLetterBox != null) {
            g2.drawImage(imgLetterBox, x, y, size, size, null);
            if (placed) {
                g2.setColor(new Color(0.2f, 0.5f, 1f, 0.3f));
                g2.fillRect(x, y, size, size);
            }
        } else {
            g2.setColor(placed ? UITheme.ACCENT_BLUE : UITheme.BG_CARD);
            g2.fillRoundRect(x, y, size, size, 8, 8);
        }

        // Hover highlight — white glow + golden border
        boolean hovered = placed ? (hoverArrIdx == index) : (hoverAvailIdx == index);
        if (hovered && !isDragging) {
            g2.setColor(new Color(1f, 1f, 1f, 0.28f));
            g2.fillRoundRect(x, y, size, size, 6, 6);
            g2.setColor(new Color(0xFF, 0xD7, 0x00, 220));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, size / 2 + 2));
        g2.setColor(placed ? UITheme.TEXT_BRIGHT : new Color(0x3B2A1A));
        drawCentredString(g2, letter, x + size / 2, y + size / 2 + 6);
    }

    private void paintEmptySlot(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x, y, size, size, 6, 6);
        if (imgLetterBox != null) {
            // Dim version of the box as empty slot
            g2.setColor(new Color(1f, 1f, 1f, 0.2f));
            g2.fillRect(x, y, size, size);
        }
        g2.setColor(new Color(1f, 1f, 1f, 0.4f));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                0, new float[]{4, 4}, 0));
        g2.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 6, 6);
        g2.setStroke(new BasicStroke(1));
    }

    /**
     * Draw a wide box using outer_box.png tiled. outer_box is 32x8 — we tile it
     * horizontally and stack for height.
     */
    /**
     * NEW CHANGE B: Draw a proper button using outer_box.png as border strips.
     * - Top edge: outer_box.png stretched to full width - Bottom edge:
     * outer_box.png flipped vertically, stretched to full width - Centre:
     * filled with the outer_box mid-colour (warm cream) - Text centred inside -
     * Click zone registered under zoneId
     *
     * This gives a clean pixel-art button look using only the first/last 8px
     * rows of the outer_box image as decorative borders.
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
     * Draw a box using outer_box.png as top/bottom border strips with a filled
     * centre. Uses the same approach as paintOuterBoxButton so nothing is cut
     * off on the right.
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
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
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
        drawCentredString(g2, text, x + w / 2, y + h / 2 + 5);
    }

    /**
     * Draw a clickable button box with an icon image. Also registers a click
     * region so mouseClicked can detect it.
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
                        x + pad, y + pad + (size - pad * 2),
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
            g2.setColor(new Color(1f, 1f, 1f, 0.15f));
            g2.fillRoundRect(x, y, w, h, 8, 8);
        }
        g2.setColor(new Color(0, 0, 0, 100));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 8, 8);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(font);
        g2.setColor(textColor);
        drawCentredString(g2, text, x + w / 2, y + h / 2 + 5);
        clickZones.put(zoneId, new Rectangle(x, y, w, h));
    }

    private void drawCentredString(Graphics2D g2, String s, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mouse input — drag & drop tiles + click zones
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Single source of truth for the word-box geometry (must match
     * paintGameUI).
     */
    private Rectangle wordBoxGeo() {
        String word = model.getCurrentWord();
        int wordLen = (word == null) ? 1 : word.length();
        int slotSize = 64, slotGap = 10;
        int totalSlotW = wordLen * (slotSize + slotGap) - slotGap;
        int boxPad = 20;
        int boxW = totalSlotW + boxPad * 2;
        int boxH = slotSize + boxPad;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = getHeight() - boxH - 60;
        return new Rectangle(boxX, boxY, boxW, boxH);
    }

    private void setupMouseInteraction() {
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                if (currentScreen != Screen.GAME || waitingForNext) {
                    return;
                }

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
                            isDragging = true;
                            dragFromArr = false;
                            dragTileIndex = i;
                            dragCurrentX = mx;
                            dragCurrentY = my;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }

                // Try to grab a tile from the arrangement
                Rectangle box = wordBoxGeo();
                List<Character> arr = model.getPlayerArrangement();
                int slotSize2 = 64, slotGap = 10, boxPad = 20;
                int slotStartX = box.x + boxPad;
                for (int i = 0; i < arr.size(); i++) {
                    int sx = slotStartX + i * (slotSize2 + slotGap);
                    int sy = box.y + boxPad / 2;
                    if (mx >= sx && mx <= sx + slotSize2 && my >= sy && my <= sy + slotSize2) {
                        isDragging = true;
                        dragFromArr = true;
                        dragTileIndex = i;
                        dragCurrentX = mx;
                        dragCurrentY = my;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int mx = e.getX(), my = e.getY();

                // Named zone clicks (always checked, even without dragging)
                Rectangle backToMap = clickZones.get("back_to_map");
                if (backToMap != null && backToMap.contains(mx, my) && !isDragging) {
                    SoundManager.stopAll();
                    stopAll();
                    Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
                    return;
                }
                if (currentScreen == Screen.INTRO) {
                    Rectangle r = clickZones.get("start_rescue");
                    if (r != null && r.contains(mx, my) && !isDragging) {
                        startGame();
                    }
                    isDragging = false;
                    return;
                }
                if (currentScreen != Screen.GAME || waitingForNext) {
                    isDragging = false;
                    return;
                }

                // Button clicks when NOT dragging
                if (!isDragging) {
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
                    return;
                }

                // Drop logic
                Rectangle box = wordBoxGeo();
                int slotSize = 64, slotGap = 10, boxPad = 20;
                boolean inBox = box.contains(mx, my);

                if (!dragFromArr && inBox) {
                    // Available tile dropped into word box → place it
                    SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.CAT_GAME);
                    model.selectLetter(dragTileIndex);
                    computeTilePositions();
                    repaint();
                } else if (dragFromArr && !inBox) {
                    // Arrangement tile dragged out → return to pool
                    SoundManager.playConditional(SoundManager.CAT_TILE_CLICK, SoundManager.GameActivity.CAT_GAME);
                    model.removeLetter(dragTileIndex);
                    computeTilePositions();
                    repaint();
                } else if (dragFromArr && inBox) {
                    // Arrangement tile dropped onto another slot → swap
                    int slotStartX = box.x + boxPad;
                    String word = model.getCurrentWord();
                    int targetSlot = -1;
                    for (int i = 0; i < word.length(); i++) {
                        int sx = slotStartX + i * (slotSize + slotGap);
                        int sy = box.y + boxPad / 2;
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

                isDragging = false;
                dragTileIndex = -1;
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

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (currentScreen != Screen.GAME) {
                    return;
                }
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

                boolean changed = (newHoverAvail != hoverAvailIdx || newHoverArr != hoverArrIdx);
                hoverAvailIdx = newHoverAvail;
                hoverArrIdx = newHoverArr;
                setCursor((newHoverAvail >= 0 || newHoverArr >= 0)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                if (changed) {
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private void startGame() {
        SoundManager.playConditional(SoundManager.CAT_GAME_START, SoundManager.GameActivity.CAT_GAME);
        // FIX 3d: music starts when user clicks Start Rescue — not before
        SoundManager.startMusic(SoundManager.CAT_BGM);
        // Show instruction dialog
//        JOptionPane.showMessageDialog(this,
//            "<html><div style='font-size:14px;text-align:center;padding:10px'>"
//            + "<b>How to Play</b><br><br>"
//            + "Listen to the word spoken aloud.<br>"
//            + "Drag the floating letters or type on your keyboard<br>"
//            + "to spell the word in the box below.<br><br>"
//            + "Each correct word builds the rescue ladder!<br>"
//            + "You have 60 seconds per word. Good luck!"
//            + "</div></html>",
//            "How to Play", JOptionPane.INFORMATION_MESSAGE);
        currentScreen = Screen.GAME;
        computeTilePositions();
        speakWord();
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
    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
