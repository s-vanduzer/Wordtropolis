/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.FireGameModel;
import com.cs4474.wordtropolis.model.Game;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.Random;

/**
 *
 * @author svand
 */
public class FireGamePanel extends JPanel {

    private enum Screen {
        INTRO, GAME, FINISH
    }
    private Screen currentScreen = Screen.INTRO;

    private static final int REIGNITE_LIMIT = 4;
    private static final int HINT_THRESHOLD = 3; // Initial wrong tries before first hint
    private static final int NUMBER_OF_FIRES = 6;
    private static final int BASE_SCORE = 50;

    private static final int NPC_COLS = 6, NPC_FW = 32, NPC_FH = 32;

    // Top-left corner of the first window
    private static final int START_X = 570;
    private static final int START_Y = 90;
    // Distance between the centers of the windows
    private static final int GAP_X = 75;
    private static final int GAP_Y = 160;

    private static final int FLAME_W = 80;
    private static final int FLAME_H = 100;

    private final FireGameModel model;

    private BufferedImage imgBg, imgHero, imgFireTruck, imgNpc;
    private BufferedImage imgFireStart, imgFireLoop, imgFireEnd;
    private BufferedImage imgBack, imgCheck;
    private BufferedImage imgPanel, imgOuterBox, imgBanner;

    private final Random random = new Random();

    private static final int HERO_COLS = 4;
    private static final int HERO_FW = 210;   // frame width  (843 / 4)
    private static final int HERO_FH = 316;   // frame height (1264 / 4)
    private static final int HERO_BACK_ROW = 3;    // last row = back-facing walk

    private int npcFrame = 0;
    private int heroFrame = 0;

    private Timer heroTimer, npcTimer, feedbackTimer;

    private java.util.Map<String, Rectangle> clickZones = new java.util.HashMap<>();

    // 6 FlameLabel instances in a 3x2 grid for fire animations
    private List<FlameLabel> flames;

    private int fireCounter = 0; // Counter for completed fires
    private int fireIndex = 0;

    private int hintCount = 0; // Number of letters revealed as hints
    private int streakCount = 1; // For score multiplier

    private StringBuilder currentInput = new StringBuilder();
    private String feedbackMsg = "";

    public FireGamePanel() {
        model = new FireGameModel(Game.getInstance().getWordList());
        loadImages();
        startAnimations();
        setupKeyboard();
        setupMouseInteraction();
        SoundManager.play(SoundManager.FIRE_TRUCK);

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });
    }

    private BufferedImage img(String path) {
        try {
            URL r = getClass().getResource(path);
            if (r == null) {
                System.out.println("[Fire] missing: " + path);
                return null;
            }
            return ImageIO.read(r);
        } catch (Exception e) {
            System.out.println("[Fire] err: " + path);
            return null;
        }
    }

    private void loadImages() {
        imgBg = img("/images/fire_game/building.png"); // Background image for Fire Game
        imgFireStart = img("/images/fire_game/fire_start.png");
        imgFireLoop = img("/images/fire_game/fire_loop.png");
        imgFireEnd = img("/images/fire_game/fire_end.png");
        imgFireTruck = img("/images/fire_game/fire_truck.png"); // Fire truck image
        imgOuterBox = img("/images/fire_game/outer_box.png");
        imgBack = img("/images/fire_game/back.png");
        imgCheck = img("/images/fire_game/check.png");
        imgNpc = img("/images/fire_game/npc.png");
        imgPanel = img("/images/fire_game/panel.png");
        imgBanner = img("/images/fire_game/banner.png");

        // Load hero image based on avatar selection
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

    private void initFires() {
        flames = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_FIRES; i++) {
            FlameLabel flame = new FlameLabel(imgFireStart, imgFireLoop, imgFireEnd, this);
            flames.add(flame);
            flame.ignite();
        }

        SoundManager.play(SoundManager.FIRE_IGNITE);
    }

    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (currentScreen != Screen.GAME) {
                    return;
                }
                char key = e.getKeyChar();

                // Only allow letters and stay within word length
                if (Character.isLetter(key) && currentInput.length() < model.getCurrentWord().length()) {
                    currentInput.append(Character.toLowerCase(key));
                    repaint();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (currentScreen != Screen.GAME) {
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleDelete();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSubmit();
                }
            }
        });
    }

    private void handleSubmit() {
        if (model.submitWord(currentInput.toString())) {
            if (streakCount > 1) {
                showFeedback("Fire extinguished! Score bonus :D");
            } else {
                showFeedback("Fire extinguished!");
            }

            hintCount = 0;
            extinguishFire();

            Game.getInstance().addScore(BASE_SCORE * streakCount);

            streakCount = (model.getWrongCount() == 0) ? streakCount + 1 : 1;

            System.out.println(streakCount);

            model.resetWrongCount();

            if (fireCounter < NUMBER_OF_FIRES) {
                model.pickNextWord();

                playWord();

            } else {
                speakWord("Congratulations. You did it hero!");
                stopAll();
                SoundManager.startMusic(SoundManager.FIRE_BGM);
                currentScreen = Screen.FINISH;
                SoundManager.play(SoundManager.FIRE_END_GAME);
                Game.getInstance().setFireCompleted(true);
            }

        } else {
            if (streakCount > 1) {
                showFeedback("Score bonus lost, try again!");
            } else {
                showFeedback("Try again!");
            }

            streakCount = 1;

            if (model.getWrongCount() > 0 && model.getWrongCount() % HINT_THRESHOLD == 0) {
                if (hintCount < model.getCurrentWord().length()) {
                    hintCount++;
                    showFeedback("You can make out a letter. You can do it!");
                }
            }

            int prevVol = SoundManager.getSfxVolume();
            SoundManager.setSfxVolume(25);                           // subtle error
            SoundManager.play(SoundManager.FIRE_ERROR);
            SoundManager.setSfxVolume(prevVol);

            if (model.getWrongCount()%REIGNITE_LIMIT == 0 && fireCounter > 0) {
                reigniteFire();
                Game.getInstance().addMisspelledWord(model.getCurrentWord());
            }
        }
        resetInputWithHints();
        repaint();
    }

    private void handleDelete() {
        // Prevent deleting if the current length is at or below the hintCount
        if (currentInput.length() > hintCount) {
            currentInput.setLength(currentInput.length() - 1);
            repaint();
        }
    }

    private void resetInputWithHints() {
        currentInput.setLength(0);
        String target = model.getCurrentWord();
        // Pre-fill the input with the number of hint letters earned
        for (int i = 0; i < hintCount; i++) {
            currentInput.append(target.charAt(i));
        }
    }

    // Update the fire counter
    private void updateFireCounter(int val) {
        if (val > 0) {
            fireCounter++;
        } else {
            fireCounter--;
        }

        int i = (fireIndex == 5) ? 0 : fireIndex;
        for (; i < NUMBER_OF_FIRES; i++) {
            if (flames.get(i).isActive()) {
                fireIndex = i;
                break;
            }
        }
        repaint();
    }

    // Handle fire extinguishing logic (extinguish the fire when necessary)
    private void extinguishFire() {
        if (fireIndex >= 0 && fireIndex < flames.size()) {
            flames.get(fireIndex).extinguish(); // Play the end animation for this fire

            System.out.println("extinguish " + fireIndex + "\n");
            updateFireCounter(1); // Increment the counter when the fire is extinguished
            SoundManager.play(SoundManager.FIRE_HOSE);
        }
    }

    // Handle reigniting logic (allow fires to be reignited after extinguishing)
    private void reigniteFire() {
        updateFireCounter(-1);
        System.out.println("reigniting...");

        boolean ignite = true;
        int i;

        while (ignite) {
            i = random.nextInt(NUMBER_OF_FIRES);
            System.out.println("Checking " + i);
            if (!flames.get(i).isActive()) {
                flames.get(i).ignite(); // Reignite the fire and start the loop animation
                System.out.println("reigniting " + i + "\n");
                ignite = false;
            }
        }
        SoundManager.play(SoundManager.FIRE_IGNITE);
    }

    private void playWord() {
        speakWord("The window says \"" + model.getCurrentWord() + "\"");
    }

    private void startGame() {
        initFires();
        SoundManager.startMusic(SoundManager.FIRE_BGM_FIRE);

        currentScreen = Screen.GAME;
        this.requestFocusInWindow();

        showFeedback("Tell the fire truck which window to aim the water at!");

        playWord();
        repaint();
    }

    private void startAnimations() {
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

    private void stopAll() {
        for (Timer t : new Timer[]{heroTimer, npcTimer, feedbackTimer}) {
            if (t != null) {
                t.stop();
            }
        }
        feedbackMsg = "";
        repaint();
        SoundManager.stopMusic();
    }

    private void showFeedback(String msg) {
        this.feedbackMsg = msg;
        repaint();

        // If a timer is already running (e.g., from a previous message), stop it
        if (feedbackTimer != null && feedbackTimer.isRunning()) {
            feedbackTimer.stop();
        }

        // Create a timer that waits 3000ms (3 seconds) then clears the text
        feedbackTimer = new Timer(3000, e -> {
            feedbackMsg = "";
            repaint();
        });

        feedbackTimer.setRepeats(false); // Crucial: only run once
        feedbackTimer.start();
    }

    private void setupMouseInteraction() {
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int mx = e.getX(), my = e.getY();

                Rectangle backToMap = clickZones.get("back_to_map");
                if (backToMap != null && backToMap.contains(mx, my)) {
                    stopAll();
                    Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
                    return;
                }

                if (currentScreen == Screen.INTRO) {
                    Rectangle start = clickZones.get("start_btn");
                    if (start != null && start.contains(mx, my)) {
                        startGame();
                    }
                    return;
                }

                Rectangle check = clickZones.get("check_btn");
                if (check != null && check.contains(mx, my)) {
                    handleSubmit();
                    return;
                }
                Rectangle back = clickZones.get("back_btn");
                if (back != null && back.contains(mx, my)) {
                    handleDelete();
                    return;
                }
                Rectangle again = clickZones.get("again_btn");
                if (again != null && again.contains(mx, my)) {
                    playWord();
                    return;
                }
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // -- ALL PAINTING --------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // ── 1. Background — fills the whole panel ─────────────────────────────
        paintBackground(g2, W, H);

        // ── 3. NPCs at the bottom ─────────────────────────────────────────────
        paintNPCS(g2, W, H);

        // -- 2. Fire truck
        paintFireTruck(g2, W, H);

        // ── 3b. Hero — centre-right, back-facing so they look toward the tree ─
        paintHero(g2, W, H);

        // ── 8. Left panel: banner + vertical progress bar (ladder progress) ───
        paintLeftPanel(g2, H);

        if (null == currentScreen) {
            paintFinishOverlay(g2, W, H);
        } else // ── 10. Intro or game puzzle overlay ──────────────────────────────────
        {
            switch (currentScreen) {
                case INTRO ->
                    paintIntro(g2, W, H);
                case GAME -> {
                    paintGameUI(g2, W, H);
                    paintFlames(g2, W, H);
                    paintTopBanner(g2, W);
                }
                default ->
                    paintFinishOverlay(g2, W, H);
            }
        }

        paintFeedback(g2, W, H);

        g2.dispose();
    }

// Paint background
    private void paintBackground(Graphics2D g2, int W, int H) {
        if (imgBg != null) {
            g2.drawImage(imgBg, 0, 0, W, H, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
        }
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
        drawCentredString(g2, "The Pet Shop is on fire!", W / 2, cardY + 48);

        // Description text inside card — two lines if needed
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(new Color(0x5A4A3A));
        drawCentredString(g2,
                "Listen to the word spoken aloud and then type the word.",
                W / 2, cardY + 84);
        drawCentredString(g2,
                "Each correct word puts out a fire. Be careful, the fire might spread...",
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
        paintClickBox(g2, btnX, btnY, btnW, btnH, "Start!", UITheme.FONT_HEADING,
                new Color(0xFCD475), UITheme.BG_DARK, "start_btn");
    }

    private void paintLeftPanel(Graphics2D g2, int H) {
        int panelX = 14;

        // --- 1. Back Button ---
        int backBtnW = 120, backBtnH = 40;
        paintOuterBoxButton(g2, panelX, 12, backBtnW, backBtnH, "< Back", "back_to_map");

        // --- 2. Side Status Panel (Replacing the Top Banner) ---
        if (currentScreen == Screen.GAME) {
            int panelW = 200;
            int panelH = 180;
            int panelY = 70; // Positioned below the back button

            // Draw the panel image
            if (imgPanel != null) {
                g2.drawImage(imgPanel, panelX, panelY, panelW, panelH, null);
            } else {
                paintOuterBox(g2, panelX, panelY, panelW, panelH);
            }

            // Prepare Text Data
            int firesLeft = NUMBER_OF_FIRES - fireCounter;
            int guessesLeft = REIGNITE_LIMIT - model.getWrongCount()%REIGNITE_LIMIT;

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));

            // Draw Status Text inside the panel
            int textX = panelX + 20;
            g2.drawString("STATUS", textX, panelY + 40);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString("Fires Left: " + firesLeft, textX, panelY + 75);

            // Multi-line wrap for the warning text
            g2.drawString("Next flare in:", textX, panelY + 110);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString(guessesLeft + " tries", textX, panelY + 130);

            // --- 3. Play Word Again Button (Now on the Left) ---
            int againW = 180;
            int againH = 45;
            int againY = panelY + panelH + 15;
            paintOuterBoxButton(g2, panelX, againY, againW, againH, "Play Word Again", "again_btn");
        }
    }

    // Paint Flames
    private void paintFlames(Graphics2D g2, int W, int H) {
        if (flames == null || flames.isEmpty()) {
            return;
        }

        for (int i = 0; i < flames.size(); i++) {
            FlameLabel flame = flames.get(i);

            // Calculate grid position (3 columns, 2 rows)
            int col = i % 3;
            int row = i / 3;

            // Calculate actual pixel position
            int x = START_X + (col * GAP_X);
            int y = START_Y + (row * GAP_Y);

            // Center the flame on the window coordinate
            int drawX = x - (FLAME_W / 2);
            int drawY = y - (FLAME_H / 2);

            // Map the FlameLabel's internal bounds for the paint call
            flame.setBounds(0, 0, FLAME_W, FLAME_H);

            // Move graphics context to the window, paint, and move back
            g2.translate(drawX, drawY);
            flame.paint(g2);
            g2.translate(-drawX, -drawY);
        }
    }

    // Paint Hero
    private void paintHero(Graphics2D g2, int W, int H) {
        if (imgHero == null) {
            return;
        }

        int drawW = 160;
        int drawH = (int) ((double) HERO_FH / HERO_FW * drawW);

        int groundY = (int) (H * 0.82) - drawH;
        int heroX = (int) (W * 0.50);

        int srcX1 = heroFrame * HERO_FW;
        int srcY1 = HERO_BACK_ROW * HERO_FH;
        int srcX2 = srcX1 + HERO_FW;
        int srcY2 = srcY1 + HERO_FH;

        g2.drawImage(imgHero,
                heroX, groundY, heroX + drawW, groundY + drawH,
                srcX1, srcY1, srcX2, srcY2, null);
    }

    // Paint Fire Truck
    private void paintFireTruck(Graphics2D g2, int W, int H) {
        if (imgFireTruck == null) {
            return;
        }

        // Scale the truck
        int truckWidth = 500;
        int truckHeight = 250;

        // Position it on the far left
        int truckX = -200;

        // Align the bottom of the truck with the NPC/Hero ground line
        // Your groundY is H * 0.87, so we match that:
        int groundY = (int) (H * 0.93);
        int truckY = groundY - truckHeight + 10; // +10 to sink tires slightly into "ground"

        g2.drawImage(imgFireTruck, truckX, truckY, truckWidth, truckHeight, null);
    }

    // ── NPCs ─────────────────────────────────────────────────────────────────
    private void paintNPCS(Graphics2D g2, int W, int H) {
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

    private void paintTopBanner(Graphics2D g2, int W) {
        if (imgBanner == null) {
            return;
        }

        // 1. Banner Dimensions
        int bannerW = 320;
        int bannerH = 100;
        int bannerX = (W - bannerW) / 2;
        int bannerY = 5;

        // 2. Draw the Image
        g2.drawImage(imgBanner, bannerX, bannerY, bannerW, bannerH, null);

        // 3. Configure Font
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(new Color(0x3B2A1A));
        FontMetrics fm = g2.getFontMetrics();

        // 4. Percentage-Based Vertical Alignment
        String scoreText = "SCORE: " + Game.getInstance().getScore();

        // Horizontal center remains the same
        int textX = bannerX + (bannerW - fm.stringWidth(scoreText)) / 2;

        float verticalPercent = 0.34f;
        int textY = bannerY + (int) (bannerH * verticalPercent) + (fm.getAscent() / 2);

        g2.drawString(scoreText, textX, textY);
    }

    // Paint input box
    private void paintInputBox(Graphics2D g2, int W, int H, int boxY, int boxH) {
        String targetWord = model.getCurrentWord();
        if (targetWord == null) {
            return;
        }

        int charW = 35;
        int gap = 12;
        int totalW = targetWord.length() * (charW + gap);
        int startX = (W - totalW) / 2;

        // ADJUST THIS: Center vertically within the box
        // boxY + (boxH/2) puts us in the middle; we adjust slightly for font height
        int textY = boxY + (boxH / 2) + 10;
        int lineY = textY + 15; // Underscore is below the text

        g2.setFont(new Font("Monospaced", Font.BOLD, 36));

        for (int i = 0; i < targetWord.length(); i++) {
            int curX = startX + i * (charW + gap);

            // 1. Draw the Underscore (Slot)
            g2.setColor(new Color(0, 0, 0, 80)); // Darker grey for visibility
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(curX, lineY, curX + charW, lineY);

            // 2. Draw the Character
            if (i < currentInput.length()) {
                String letter = String.valueOf(currentInput.charAt(i)).toUpperCase();

                if (i < hintCount) {
                    // Hint letters: Golden/Orange to stand out
                    g2.setColor(new Color(0xD97706));
                } else {
                    // User typed letters: Solid Black
                    g2.setColor(Color.BLACK);
                }

                FontMetrics fm = g2.getFontMetrics();
                int lx = curX + (charW - fm.stringWidth(letter)) / 2;
                g2.drawString(letter, lx, textY);
            }
        }
    }

// ── Game UI: floating tiles + word box + buttons ───────────────────────
    private void paintGameUI(Graphics2D g2, int W, int H) {
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
        int boxY = H - boxH - 40;

        // Draw outer_box tiled as the container
        paintOuterBox(g2, boxX, boxY, boxW, boxH);

        paintInputBox(g2, W, H, boxY, boxH);

        int btnSize = 56;
        int spacing = 10;

        // 1. Back button (Left of box)
        int backX = boxX - btnSize - spacing;
        int backY = boxY + boxH / 2 - btnSize / 2;
        paintIconBox(g2, backX, backY, btnSize, imgBack, "back_btn");

        // 2. Check button (Right of box)
        int checkX = boxX + boxW + spacing;
        int checkY = backY;
        paintIconBox(g2, checkX, checkY, btnSize, imgCheck, "check_btn");
    }  // end paintGameUI

    private void paintFinishOverlay(Graphics2D g2, int W, int H) {
        // 1. Draw a semi-transparent dark background for the text/button area
        g2.setColor(new Color(0f, 0f, 0f, 0.6f));
        // We make this taller (160 instead of 80) to fit the button
        g2.fillRect(0, H / 2 - 80, W, 160);

        // 2. Draw the "All fires are out!" text
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(UITheme.TEXT_BRIGHT);
        drawCentredString(g2, "All fires are out!", W / 2, H / 2 - 10);

        // 3. Draw the "Back to Map" button below the text
        int btnW = 200;
        int btnH = 45;
        int btnX = (W - btnW) / 2;
        int btnY = H / 2 + 25; // Positioned below the text

        // We use "back_to_map" as the zoneId so it triggers the existing map logic
        paintOuterBoxButton(g2, btnX, btnY, btnW, btnH, "Back to Map", "back_to_map");
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
     * Draw a clickable button box with an icon image. Also registers a click
     * region so mouseClicked can detect it.
     */
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

    // ── Feedback message ──────────────────────────────────────────────────────
    private void paintFeedback(Graphics2D g2, int W, int H) {
        if (feedbackMsg == null || feedbackMsg.isEmpty()) {
            return; // Don't draw anything if there's no message
        }

        int fw = Math.min(W - 60, 700);
        int fh = 54;   // was 36 — taller box
        int fx = (W - fw) / 2;
        int fy = (int) (H * 0.70);
        paintOuterBoxButton(g2, fx, fy, fw, fh, "", "");  // use the nicer outer_box style
        g2.setFont(UITheme.FONT_HEADING);   // was FONT_SMALL — much bigger now
        g2.setColor(Color.BLACK);
        drawCentredString(g2, feedbackMsg, W / 2, fy + fh / 2 + 7);
    }

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

    private void drawCentredString(Graphics2D g2, String s, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy);
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

    // -- END PAINTING ------------------------------------------
    // Helper method to trigger word pronunciation (same as in the CatGame)
    private void speakWord(String w) {
        if (w == null || w.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                String os = System.getProperty("os.name", "").toLowerCase();

                if (os.contains("mac")) {
                    new ProcessBuilder("say", "-v", "Alex", "-r", "110", "[[volm 1.0]] " + w)
                            .start().waitFor();
                } else if (os.contains("win")) {
                    String ps = "Add-Type -AssemblyName System.Speech;"
                            + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer;"
                            + "$s.Rate = -2;"
                            + "$s.Volume = 100;"
                            + "$s.Speak('" + w.replace("'", "") + "');";
                    new ProcessBuilder("powershell", "-Command", ps)
                            .start().waitFor();
                } else {
                    new ProcessBuilder("espeak", "-s", "120", "-a", "200", w)
                            .start().waitFor();
                }
            } catch (Exception e) {
                System.out.println("[Fire] TTS not available: " + e.getMessage());
            }
        }, "tts-thread").start();
    }
}
