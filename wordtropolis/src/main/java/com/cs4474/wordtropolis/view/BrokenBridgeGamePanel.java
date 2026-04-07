package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.BrokenBridgeGameModel;
import com.cs4474.wordtropolis.model.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BrokenBridgeGamePanel – Broken Bridge mini-game
 *
 * Mirrors CatGamePanel behaviour exactly: • Intro card matches cat-game style
 * (title + 2 lines + divider + button) • Hero: correct avatar, centered under
 * bridge, sprite-sheet animated • Hero is clipped to below the bridge so no
 * artifact shows through the gap • No NPCs • Timer timeout → "Time's up! Try
 * again." + clear + restart (no game-over) • Check before all words placed →
 * "Pick some words first!" feedback • Enter key triggers CHECK (same as
 * CatGamePanel Enter → submit) • Left panel: vertical green progress bar (same
 * as CatGamePanel.paintLeftPanel) • After all rounds: score dialog → SCREEN_MAP
 * (same as CatGamePanel.showResultDialog) • Hero re-loaded in refresh() so the
 * chosen avatar always appears • Word count increases per round via
 * model.getWordsThisRound() • Sound: BGM on start, box on tile place, error on
 * wrong, cha-ching on correct
 */
public class BrokenBridgeGamePanel extends JPanel implements Refreshable {

    // ── Sparkle ───────────────────────────────────────────────────────────────
    private static final int SPKL_FRAMES = 14, SPKL_W = 32, SPKL_H = 32, SPKL_SCALE = 3;
    private boolean showSparkle = false;
    private int sparkleFrame = 0, sparkleX, sparkleY;
    private Timer sparkleTimer;

    // ── Hero sprite sheet (exact constants from CatGamePanel) ─────────────────
    private static final int HERO_COLS = 4, HERO_FW = 210, HERO_FH = 316, HERO_BACK_ROW = 3;
    private int heroFrame = 0;
    private Timer heroTimer;

    // ── Error flash ───────────────────────────────────────────────────────────
    private static final int ERROR_FRAMES = 32;
    private int errorFrame = 0;
    private Timer shakeTimer;
    private int shakeTick = 0;

    // ── Bridge geometry ───────────────────────────────────────────────────────
    private static final double BRIDGE_Y_RATIO = 0.56;
    private static final double BRIDGE_H_RATIO = 0.045;
    private static final double LEFT_CLIFF_RATIO = 0.28;
    private static final double RIGHT_CLIFF_RATIO = 0.72;

    // ── Word-tile sizing ──────────────────────────────────────────────────────
    private static final int TILE_W = 118, TILE_H = 50, TILE_GAP = 16;

    // ── Button size (matches CatGamePanel) ────────────────────────────────────
    private static final int BTN_SIZE = 56;

    // ── Screen state ──────────────────────────────────────────────────────────
    private enum Screen {
        INTRO, PLAYING, CORRECT_ANIM, ERROR_ANIM, FINISH
    }
    private Screen screen = Screen.INTRO;

    // ── Model ─────────────────────────────────────────────────────────────────
    private final Game game;
    private final BrokenBridgeGameModel model;

    // ── Feedback (mirrors CatGamePanel) ───────────────────────────────────────
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;
    private Timer feedbackTimer;

    // ── Click zones + tile rects ──────────────────────────────────────────────
    private final Map<String, Rectangle> tileRects = new LinkedHashMap<>();
    private final Map<String, Rectangle> clickZones = new java.util.HashMap<>();

    // ── Images ────────────────────────────────────────────────────────────────
    private BufferedImage imgBg, imgCheck, imgBack, imgIndividualBox,
            imgOuterBox, imgProgressBar, imgHero, imgSparkle;

    // ── Timers ────────────────────────────────────────────────────────────────
    private Timer gameTimer, animTimer;

    // ─────────────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────────────
    public BrokenBridgeGamePanel() {
        this.game = Game.getInstance();

        BrokenBridgeGameModel.Difficulty diff
                = (game.getDifficulty() == Game.Difficulty.HARD)
                ? BrokenBridgeGameModel.Difficulty.GRADE2
                : BrokenBridgeGameModel.Difficulty.GRADE1;

        this.model = new BrokenBridgeGameModel(diff);

        setLayout(null);
        setBackground(Color.BLACK);
        loadImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && screen == Screen.PLAYING) {
                    SoundManager.play(SoundManager.GAME_BTN_CLICK);
                    handleCheck();
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addNotify / removeNotify
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        stopTimers();
    }

    @Override
    public void refresh() {
        grabFocus();
        stopTimers();

        model.restartGame();
        tileRects.clear();
        clickZones.clear();
        screen = Screen.INTRO;
        feedbackMsg = "";
        sparkleFrame = 0;
        errorFrame = 0;
        showSparkle = false;
        imgHero = loadHeroImage();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image loading
    // ─────────────────────────────────────────────────────────────────────────
    private void loadImages() {
        imgBg = img("/images/backgrounds/bridge_background.png");
        imgIndividualBox = img("/images/ui/individual_box.png");
        imgOuterBox = img("/images/ui/outer_box.png");
        imgProgressBar = img("/images/ui/progress_bar.png");
        imgCheck = img("/images/ui/check.png");
        imgBack = img("/images/bridge_game/back.png");
        imgSparkle = img("/images/effects/blue_sparkle.png");
        imgHero = loadHeroImage();
    }

    private BufferedImage loadHeroImage() {
        try {
            String avatar = Game.getInstance().getAvatarPath();
            if ("Mia".equalsIgnoreCase(avatar)) {
                return img("/images/char_sprites/hero1_standing.png");
            }
            if ("Zyx".equalsIgnoreCase(avatar)) {
                return img("/images/char_sprites/alien_standing.png");
            }
            return img("/images/char_sprites/hero2_standing.png");
        } catch (Exception ignored) {
            System.out.println("[Bridge] Error loading hero");
        }
        return null;
    }

    private BufferedImage img(String... paths) {
        for (String path : paths) {
            try {
                URL url = getClass().getResource(path);
                if (url != null) {
                    BufferedImage bi = ImageIO.read(url);
                    if (bi != null) {
                        return bi;
                    }
                }
                System.out.println("[Bridge] missing: " + path);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timers
    // ─────────────────────────────────────────────────────────────────────────
    private void initTimers() {
        heroTimer = new Timer(180, e -> {
            heroFrame = (heroFrame + 1) % HERO_COLS;
            repaint();
        });
        heroTimer.start();

        animTimer = new Timer(16, e -> {
            if (screen == Screen.ERROR_ANIM) {
                errorFrame++;
                if (errorFrame >= ERROR_FRAMES) {
                    model.clearSelection();
                    errorFrame = 0;
                    screen = Screen.PLAYING;
                }
                repaint();
            }
        });
        animTimer.start();
    }

    private void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new Timer(1000, e -> {
            if (screen != Screen.PLAYING) {
                return;
            }
            boolean hitZero = model.tickSecond();
            if (hitZero) {
                model.clearSelection();
                model.resetTimer();
                showFeedback("Time's up! Try again.", UITheme.ACCENT_RED);
            }
            repaint();
        });
        gameTimer.start();
    }

    private void stopTimers() {
        SoundManager.stopAll();                          // ← stop BGM whenever we leave
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (animTimer != null) {
            animTimer.stop();
        }
        if (heroTimer != null) {
            heroTimer.stop();
        }
        if (sparkleTimer != null) {
            sparkleTimer.stop();
        }
        if (feedbackTimer != null) {
            feedbackTimer.stop();
        }
        if (shakeTimer != null) {
            shakeTimer.stop();
        }
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

    private void shakeEffect() {
        if (shakeTimer != null && shakeTimer.isRunning()) {
            return;
        }
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

    // ─────────────────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────────────────
    private void handleClick(int mx, int my) {
        if (screen == Screen.INTRO) {
            Rectangle s = clickZones.get("start_btn"), b = clickZones.get("back_btn");
            if (b != null && b.contains(mx, my)) {
                stopTimers();
                SoundManager.play(SoundManager.GAME_BTN_CLICK);
                SoundManager.stopAll();
                Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
            } else if (s != null && s.contains(mx, my)) {
                SoundManager.play(SoundManager.GAME_BTN_CLICK);
                startGame();
            }
            return;
        }
        if (screen == Screen.CORRECT_ANIM || screen == Screen.ERROR_ANIM) {
            return;
        }

        Rectangle back = clickZones.get("back_btn");
        Rectangle check = clickZones.get("check_btn");
        Rectangle clear = clickZones.get("clear_btn");

        if (back != null && back.contains(mx, my)) {
            stopTimers();
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            SoundManager.stopAll();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
            return;
        }
        if (clear != null && clear.contains(mx, my)) {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            model.clearSelection();
            repaint();
            return;
        }
        if (check != null && check.contains(mx, my)) {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            handleCheck();
            return;
        }

        // Click a placed slot → deselect
        int w = getWidth(), h = getHeight();
        List<String> sel = model.getSelected();
        int n = model.getWordsThisRound();
        int outerW = n * TILE_W + (n - 1) * TILE_GAP + 40;
        int outerX = w / 2 - outerW / 2;
        int outerY = h - 78 - 60;
        int slotY = outerY + (78 - TILE_H) / 2;
        for (int i = 0; i < sel.size(); i++) {
            int sx = outerX + 20 + i * (TILE_W + TILE_GAP);
            if (mx >= sx && mx <= sx + TILE_W && my >= slotY && my <= slotY + TILE_H) {
                model.deselectWord(sel.get(i));
                repaint();
                return;
            }
        }

        // Click unplaced tile → place + box sound
        for (Map.Entry<String, Rectangle> entry : tileRects.entrySet()) {
            if (entry.getValue().contains(mx, my)) {
                model.selectWord(entry.getKey());
                SoundManager.play(SoundManager.BRIDGE_BOX); // ← box sound on tile place
                repaint();
                return;
            }
        }
    }

    private void startGame() {
        screen = Screen.PLAYING;
        startGameTimer();
        initTimers();
        requestFocusInWindow();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        new Thread(() -> SoundManager.startMusic(SoundManager.BRIDGE_BGM), "bgm-thread").start();
        repaint();
    }

    private void handleCheck() {
        List<String> sel = model.getSelected();
        int n = model.getWordsThisRound();

        if (sel.size() < n) {
            showFeedback("Pick some words first!", UITheme.ACCENT_YELLOW);
            return;
        }

        if (model.isAnswerCorrect()) {
            model.applyCorrectRound();
            SoundManager.playFrom(SoundManager.BRIDGE_ADD_PIECE, 0.0, 1.5);
            triggerSparkle();
            if (model.isComplete()) {
                if (gameTimer != null) {
                    gameTimer.stop();
                }
                new Timer(900, e -> {
                    ((Timer) e.getSource()).stop();
                    stopTimers();
                    game.setBridgeCompleted(true);
                    game.addScore(model.getScore());
                    SoundManager.play(SoundManager.GAME_FINISH);
                    screen = Screen.FINISH;
                    repaint();
                }).start();
            } else {
                screen = Screen.CORRECT_ANIM;
                new Timer(900, e -> {
                    model.newRound();
                    model.resetTimer();
                    tileRects.clear();
                    screen = Screen.PLAYING;
                    repaint();
                    ((Timer) e.getSource()).stop();
                }).start();
            }
        } else {
            model.recordIncorrect();
            SoundManager.play(SoundManager.BRIDGE_ERROR);  // ← error sound on wrong order
            shakeEffect();
            showFeedback("Wrong order! Try again.", UITheme.ACCENT_RED);
            model.clearSelection();
            screen = Screen.ERROR_ANIM;
            errorFrame = 0;
        }
    }

    private void triggerSparkle() {
        int w = getWidth(), h = getHeight();
        int ce = (int) (w * LEFT_CLIFF_RATIO), cs = (int) (w * RIGHT_CLIFF_RATIO);
        sparkleX = ce + (int) ((double) model.getBridgeProgress()
                / BrokenBridgeGameModel.TOTAL_ROUNDS * (cs - ce));
        sparkleY = (int) (h * BRIDGE_Y_RATIO) - 30;
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

    // ─────────────────────────────────────────────────────────────────────────
    // Painting
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth(), h = getHeight();

        paintBackground(g2, w, h);
        paintHero(g2, w, h);
        paintBridge(g2, w, h);

        if (null != screen) {
            switch (screen) {
                case INTRO:
                    paintIntro(g2, w, h);
                    break;
                case PLAYING:
                    paintWordTiles(g2, w, h);
                    paintTimerBar(g2, w);
                    paintLeftPanel(g2, h);
                    paintBottomUI(g2, w, h);
                    if (showSparkle) {
                        paintSparkle(g2);
                    }
                    break;
                case FINISH:
                    paintFinishOverlay(g2, w, h);
                    break;
                default:
                    break;
            }
        }

        if (shakeTimer != null && shakeTimer.isRunning() && shakeTick % 2 == 0) {
            g2.setColor(new Color(1f, 0f, 0f, 0.12f));
            g2.fillRect(0, 0, w, h);
        }
        if (!feedbackMsg.isEmpty()) {
            paintFeedback(g2, w, h);
        }

        g2.dispose();
    }

    private void paintBackground(Graphics2D g2, int W, int H) {
        if (imgBg != null) {
            int bw = imgBg.getWidth(), bh = imgBg.getHeight();
            double scale = Math.max((double) W / bw, (double) H / bh);
            int dw = (int) (bw * scale), dh = (int) (bh * scale);
            g2.drawImage(imgBg, (W - dw) / 2, (H - dh) / 2, dw, dh, null);
        } else {
            g2.setPaint(new GradientPaint(0, 0, UITheme.SKY_TOP, 0, H * 0.7f, UITheme.SKY_BOT));
            g2.fillRect(0, 0, W, H);
            g2.setColor(UITheme.GRASS_COLOR);
            g2.fillRect(0, (int) (H * 0.85), W, (int) (H * 0.15));
        }
    }

    private void paintHero(Graphics2D g2, int W, int H) {
        if (imgHero == null) {
            return;
        }
        int drawW = 160;
        int drawH = (int) ((double) HERO_FH / HERO_FW * drawW);
        int groundY = (int) (H * 0.87) - drawH;
        int heroX = W / 2 - drawW / 2;

        int srcX1 = heroFrame * HERO_FW;
        int srcY1 = HERO_BACK_ROW * HERO_FH;

        int bridgeBottom = (int) (H * BRIDGE_Y_RATIO) + Math.max(22, (int) (H * BRIDGE_H_RATIO)) + 2;
        g2.setClip(0, bridgeBottom, W, H - bridgeBottom);
        g2.drawImage(imgHero,
                heroX, groundY, heroX + drawW, groundY + drawH,
                srcX1, srcY1 + 70, srcX1 + HERO_FW, srcY1 + HERO_FH, null);
        g2.setClip(null);
    }

    private void paintBridge(Graphics2D g2, int w, int h) {
        int bridgeY = (int) (h * BRIDGE_Y_RATIO);
        int bridgeH = Math.max(22, (int) (h * BRIDGE_H_RATIO));
        int cliffEnd = (int) (w * LEFT_CLIFF_RATIO);
        int cliffSt = (int) (w * RIGHT_CLIFF_RATIO);
        int gapW = cliffSt - cliffEnd;
        int progress = model.getBridgeProgress();

        int towerH = (int) (h * 0.075);
        int towerTop = bridgeY - towerH;
        int midX = w / 2;
        int ropeNadir = bridgeY + bridgeH / 2 + 14;

        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(90, 70, 45));
        drawQuadRope(g2, cliffEnd, towerTop, midX, ropeNadir, cliffSt, towerTop);
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(new Color(110, 88, 55));
        for (int sx = cliffEnd + 35; sx < cliffSt; sx += 44) {
            int ry = quadRopeY(cliffEnd, towerTop, midX, ropeNadir, cliffSt, towerTop, sx);
            g2.drawLine(sx, ry, sx, bridgeY);
        }
        g2.setStroke(new BasicStroke(1f));

        drawDeck(g2, 0, bridgeY, cliffEnd, bridgeH, false);
        drawDeck(g2, cliffSt, bridgeY, w - cliffSt, bridgeH, false);
        if (progress > 0) {
            int px = (int) ((double) progress / BrokenBridgeGameModel.TOTAL_ROUNDS * gapW);
            drawDeck(g2, cliffEnd, bridgeY, px, bridgeH, true);
        }
        int gapLeft = cliffEnd + (int) ((double) progress / BrokenBridgeGameModel.TOTAL_ROUNDS * gapW);
        if (gapLeft < cliffSt) {
            g2.setColor(new Color(0, 0, 0, 88));
            g2.fillRect(gapLeft, bridgeY, cliffSt - gapLeft, bridgeH);
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRect(gapLeft, bridgeY + bridgeH, cliffSt - gapLeft, 8);
        }
        drawTower(g2, cliffEnd - 13, towerTop, 26, towerH + bridgeH);
        drawTower(g2, cliffSt - 13, towerTop, 26, towerH + bridgeH);
    }

    private void drawDeck(Graphics2D g2, int x, int y, int width, int height, boolean wood) {
        if (width <= 0) {
            return;
        }
        if (wood) {
            for (int px = x; px < x + width; px += 13) {
                int pw = Math.min(11, (x + width) - px);
                g2.setColor(new Color(160, 108, 48));
                g2.fillRect(px, y, pw, height);
                g2.setColor(new Color(130, 85, 35));
                g2.fillRect(px, y, 2, height);
                g2.setColor(new Color(195, 145, 75));
                g2.fillRect(px + 3, y + 2, Math.max(0, pw - 5), 4);
            }
        } else {
            g2.setColor(new Color(105, 90, 75));
            g2.fillRect(x, y, width, height);
            g2.setColor(new Color(125, 110, 94));
            for (int bx = x; bx < x + width; bx += 24) {
                for (int by = y; by < y + height; by += 12) {
                    g2.drawRect(bx + (((by - y) / 12) % 2 == 0 ? 0 : 12), by, 22, 10);
                }
            }
        }
        g2.setColor(new Color(205, 185, 158));
        g2.fillRect(x, y, width, 3);
        g2.setColor(new Color(55, 42, 30));
        g2.fillRect(x, y + height - 3, width, 3);
    }

    private void drawTower(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(85, 70, 58));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(125, 110, 95));
        g2.fillRect(x, y, 3, h);
        g2.setColor(new Color(55, 44, 34));
        g2.fillRect(x + w - 3, y, 3, h);
        g2.setColor(new Color(70, 58, 46));
        g2.fillRect(x - 5, y, w + 10, 9);
        g2.setColor(new Color(145, 128, 108));
        g2.fillRect(x - 5, y, w + 10, 3);
    }

    private void drawQuadRope(Graphics2D g2, int x1, int y1, int cx, int cy, int x2, int y2) {
        int px = x1, py = y1;
        for (int i = 1; i <= 70; i++) {
            double t = i / 70.0, mt = 1 - t;
            int nx = (int) (mt * mt * x1 + 2 * mt * t * cx + t * t * x2);
            int ny = (int) (mt * mt * y1 + 2 * mt * t * cy + t * t * y2);
            g2.drawLine(px, py, nx, ny);
            px = nx;
            py = ny;
        }
    }

    private int quadRopeY(int x1, int y1, int cx, int cy, int x2, int y2, int x) {
        double t = Math.max(0, Math.min(1, (double) (x - x1) / (x2 - x1))), mt = 1 - t;
        return (int) (mt * mt * y1 + 2 * mt * t * cy + t * t * y2);
    }

    private void paintLeftPanel(Graphics2D g2, int H) {
        int panelX = 14;
        paintOuterBoxButton(g2, panelX, 12, 120, 40, "< Back", "back_btn");

        int barX = panelX + 10;
        int barTop = 80;
        int barH = Math.min(H - 80, 200);
        int barW = 18;

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(barX - 2, barTop - 2, barW + 4, barH + 4, 6, 6);

        int filled = (int) ((double) model.getBridgeProgress()
                / BrokenBridgeGameModel.TOTAL_ROUNDS * barH);

        if (imgProgressBar != null && filled > 0) {
            int srcX1 = 10, srcX2 = 35, srcY1 = (1 * 24) + 15, srcY2 = (2 * 24) - 10;
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.setClip(barX, barTop + barH - filled, barW, filled);
            g3.translate(barX + barW / 2.0, barTop + barH / 2.0);
            g3.rotate(Math.PI / 2);
            g3.drawImage(imgProgressBar, -barH / 2, -barW / 2, barH / 2, barW / 2,
                    srcX1, srcY1, srcX2, srcY2, null);
            g3.dispose();
        } else if (filled > 0) {
            g2.setColor(UITheme.ACCENT_TEAL);
            g2.fillRect(barX, barTop + barH - filled, barW, filled);
        }
    }

    private void paintTimerBar(Graphics2D g2, int W) {
        int tbX = 160, tbY = 8, tbH = 40, tbW = W - 320;
        float pct = (float) model.getTimeRemaining() / BrokenBridgeGameModel.ROUND_DURATION_SEC;
        int fillW = (int) (tbW * Math.max(0, pct));

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(tbX - 3, tbY - 3, tbW + 6, tbH + 6, 10, 10);

        if (imgProgressBar != null) {
            int srcX1 = 10, srcX2 = 35, srcY1 = (3 * 24) + 15, srcY2 = (4 * 24) - 10;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.drawImage(imgProgressBar, tbX, tbY, tbX + tbW, tbY + tbH, srcX1, srcY1, srcX2, srcY2, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            if (fillW > 0) {
                g2.setClip(tbX, tbY, fillW, tbH);
                g2.drawImage(imgProgressBar, tbX, tbY, tbX + tbW, tbY + tbH, srcX1, srcY1, srcX2, srcY2, null);
                g2.setClip(null);
            }
        } else if (fillW > 0) {
            Color c = pct > 0.5f ? new Color(210, 70, 145) : pct > 0.25f ? new Color(245, 145, 30) : UITheme.ACCENT_RED;
            g2.setColor(c);
            g2.fillRoundRect(tbX, tbY, fillW, tbH, 8, 8);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(UITheme.FONT_HEADING);
        String ts = model.getTimeRemaining() + "s";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(ts, tbX + (tbW - fm.stringWidth(ts)) / 2, tbY + tbH / 2 + fm.getAscent() / 2);

        paintOuterBoxButton(g2, tbX + tbW + 20, tbY, 140, tbH, "Score: " + model.getScore(), "");
    }

    private void paintIntro(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, W, H);

        int cardW = Math.min(W - 80, 680), cardH = 250;  // ← taller card
        int cardX = W / 2 - cardW / 2, cardY = H / 2 - cardH / 2;
        paintOuterBox(g2, cardX, cardY, cardW, cardH);

        // Title
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(new Color(0x3B2A1A));
        drawCentredString(g2, "Fix the Broken Bridge!", W / 2, cardY + 50);

        // Description — 3 lines with proper spacing
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(new Color(0x5A4A3A));
        drawCentredString(g2, "Words appear above the bridge.", W / 2, cardY + 86);
        drawCentredString(g2, "Click them in alphabetical (A \u2192 Z) order to fill the slots.", W / 2, cardY + 110);
        drawCentredString(g2, "Then press Enter or click the checkmark to submit!", W / 2, cardY + 134);

        // Divider — below all 3 lines
        g2.setColor(new Color(0xC8A97A));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cardX + 30, cardY + 152, cardX + cardW - 30, cardY + 152);
        g2.setStroke(new BasicStroke(1f));

        // Button
        int btnW = 260, btnH = 58;
        paintClickBox(g2, W / 2 - btnW / 2, cardY + cardH - btnH - 14,
                btnW, btnH, "Start Fixing!", UITheme.FONT_HEADING,
                new Color(0xFCD475), UITheme.BG_DARK, "start_btn");

        int panelX = 14;
        paintOuterBoxButton(g2, panelX, 12, 120, 40, "< Back", "back_btn");
    }

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
        drawCentredString(g2, "Total Score: " + game.getScore(), W / 2, statsStartY);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCentredString(g2, "Correct Guesses: " + model.getCorrectAttempts(), W / 2, statsStartY + 35);
        drawCentredString(g2, "Mistakes Made: " + model.getIncorrectAttempts(), W / 2, statsStartY + 65);

        // 6. Final Praise
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(new Color(0x2D5A27)); // Dark green for a "win" feel
        drawCentredString(g2, "The bridge is all fixd. Great job!", W / 2, cardY + 210);

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
                new Color(0xFCD475), UITheme.BG_DARK, "back_btn");
    }

    private void paintWordTiles(Graphics2D g2, int w, int h) {
        List<String> words = model.getDisplayWords();
        List<String> selected = model.getSelected();
        int n = words.size();
        int totalW = n * TILE_W + (n - 1) * TILE_GAP;
        int startX = (w - totalW) / 2;
        int tileY = (int) (h * 0.30);

        tileRects.clear();

        for (int i = 0; i < n; i++) {
            String word = words.get(i);
            boolean placed = selected.contains(word);
            int tx = startX + i * (TILE_W + TILE_GAP);

            if (!placed) {
                tileRects.put(word, new Rectangle(tx, tileY, TILE_W, TILE_H));
            }

            if (placed) {
                g2.setColor(new Color(170, 165, 155, 155));
                g2.fillRoundRect(tx, tileY, TILE_W, TILE_H, 12, 12);
                g2.setColor(new Color(130, 125, 118));
                g2.drawRoundRect(tx, tileY, TILE_W, TILE_H, 12, 12);
            } else if (imgIndividualBox != null) {
                g2.drawImage(imgIndividualBox, tx, tileY, TILE_W, TILE_H, null);
            } else {
                g2.setColor(new Color(255, 247, 215));
                g2.fillRoundRect(tx, tileY, TILE_W, TILE_H, 12, 12);
                g2.setColor(new Color(210, 165, 75));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(tx, tileY, TILE_W, TILE_H, 12, 12);
                g2.setStroke(new BasicStroke(1f));
            }

            Font f = gameFont(placed ? 13f : 15f);
            FontMetrics fm = g2.getFontMetrics(f);
            g2.setFont(f);
            g2.setColor(placed ? new Color(155, 150, 142) : new Color(48, 32, 10));
            g2.drawString(word,
                    tx + (TILE_W - fm.stringWidth(word)) / 2,
                    tileY + (TILE_H + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    private void paintBottomUI(Graphics2D g2, int w, int h) {
        int n = model.getWordsThisRound();
        int outerW = n * TILE_W + (n - 1) * TILE_GAP + 40;
        int outerH = 78;
        int outerX = w / 2 - outerW / 2;
        int outerY = h - outerH - 60;
        paintOuterBox(g2, outerX, outerY, outerW, outerH);

        List<String> selected = model.getSelected();
        int slotStartX = outerX + 20;
        int slotY = outerY + (outerH - TILE_H) / 2;

        for (int i = 0; i < n; i++) {
            int sx = slotStartX + i * (TILE_W + TILE_GAP);
            if (imgIndividualBox != null) {
                g2.drawImage(imgIndividualBox, sx, slotY, TILE_W, TILE_H, null);
            } else {
                g2.setColor(new Color(255, 248, 222));
                g2.fillRoundRect(sx, slotY, TILE_W, TILE_H, 10, 10);
                g2.setColor(new Color(200, 172, 102));
                g2.drawRoundRect(sx, slotY, TILE_W, TILE_H, 10, 10);
            }

            if (i < selected.size()) {
                Font f = gameFont(14f);
                FontMetrics fm = g2.getFontMetrics(f);
                g2.setFont(f);
                g2.setColor(new Color(48, 32, 10));
                String word = selected.get(i);
                g2.drawString(word,
                        sx + (TILE_W - fm.stringWidth(word)) / 2,
                        slotY + (TILE_H + fm.getAscent() - fm.getDescent()) / 2);
            } else {
                g2.setColor(new Color(185, 165, 122));
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
                g2.drawRoundRect(sx + 5, slotY + 5, TILE_W - 10, TILE_H - 10, 8, 8);
                g2.setStroke(new BasicStroke(1f));
            }
        }

        int clearX = outerX - BTN_SIZE - 10, clearY = outerY + outerH / 2 - BTN_SIZE / 2;
        paintIconBox(g2, clearX, clearY, BTN_SIZE, imgBack, "clear_btn");
        int checkX = outerX + outerW + 10, checkY = clearY;
        paintIconBox(g2, checkX, checkY, BTN_SIZE, imgCheck, "check_btn");
    }

    private void paintSparkle(Graphics2D g2) {
        if (imgSparkle == null || !showSparkle) {
            return;
        }
        int sx = sparkleFrame * SPKL_W;
        g2.drawImage(imgSparkle,
                sparkleX - SPKL_W, sparkleY - SPKL_H,
                sparkleX + SPKL_W * SPKL_SCALE, sparkleY + SPKL_H * SPKL_SCALE,
                sx, 0, sx + SPKL_W, SPKL_H, null);
    }

    private void paintFeedback(Graphics2D g2, int W, int H) {
        int fw = Math.min(W - 60, 700), fh = 54;
        int fx = (W - fw) / 2, fy = (int) (H * 0.70);
        paintOuterBoxButton(g2, fx, fy, fw, fh, "", "");
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(feedbackColor);
        drawCentredString(g2, feedbackMsg, W / 2, fy + fh / 2 + 7);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Draw helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void paintOuterBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(229, 226, 207));
        g2.fillRect(x, y, w, h);
        if (imgOuterBox != null) {
            int sh = Math.min(8, h / 2);
            g2.drawImage(imgOuterBox, x, y, x + w, y + sh, 0, 0, imgOuterBox.getWidth(), imgOuterBox.getHeight(), null);
            g2.drawImage(imgOuterBox, x, y + h - sh, x + w, y + h, 0, imgOuterBox.getHeight(), imgOuterBox.getWidth(), 0, null);
            g2.drawImage(imgOuterBox, x, y + sh, x + sh, y + h - sh, 0, 0, imgOuterBox.getHeight(), imgOuterBox.getHeight(), null);
            g2.drawImage(imgOuterBox, x + w - sh, y + sh, x + w, y + h - sh, imgOuterBox.getWidth() - imgOuterBox.getHeight(), 0, imgOuterBox.getWidth(), imgOuterBox.getHeight(), null);
        } else {
            g2.setColor(new Color(0xC8A97A));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void paintOuterBoxButton(Graphics2D g2, int x, int y, int w, int h, String label, String zoneId) {
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

    private void paintIconBox(Graphics2D g2, int x, int y, int size, BufferedImage icon, String zoneId) {
        if (icon != null) {
            int pad = 6;
            if ("clear_btn".equals(zoneId)) {
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
            String text, Font font, Color bgTint, Color textColor, String zoneId) {
        g2.setColor(new Color(bgTint.getRed(), bgTint.getGreen(), bgTint.getBlue(), 180));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        if (imgOuterBox != null) {
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

    private Font gameFont(float size) {
        if (size >= 17) {
            return UITheme.FONT_HEADING.deriveFont(size);
        }
        if (size >= 14) {
            return UITheme.FONT_LETTER.deriveFont(size);
        }
        return UITheme.FONT_SMALL.deriveFont(size);
    }
}
