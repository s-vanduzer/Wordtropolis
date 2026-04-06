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
public class FireGamePanel extends JPanel implements Refreshable {

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

    private final Game game;

    private BufferedImage imgBg, imgHero, imgFireTruck, imgNpc;
    private BufferedImage imgFireStart, imgFireLoop, imgFireEnd;
    private BufferedImage imgBack, imgCheck;
    private BufferedImage imgPanel, imgOuterBox;

    private final Random random = new Random();

    private static final int HERO_COLS = 4;
    private static final int HERO_FW = 210;   // frame width  (843 / 4)
    private static final int HERO_FH = 316;   // frame height (1264 / 4)
    private static final int HERO_BACK_ROW = 3;    // last row = back-facing walk

    private int npcFrame = 0;
    private int heroFrame = 0;

    private int hintCount; // Number of letters revealed as hints

    private Timer heroTimer, npcTimer, feedbackTimer;

    private java.util.Map<String, Rectangle> clickZones = new java.util.HashMap<>();

    // 6 FlameLabel instances in a 3x2 grid for fire animations
    private List<FlameLabel> flames;

    private StringBuilder currentInput = new StringBuilder();
    private String feedbackMsg = "";

    public FireGamePanel() {
        this.game = Game.getInstance();
        model = new FireGameModel();

        hintCount = 0; // Number of letters revealed as hints

        loadImages();
        setupKeyboard();
        setupMouseInteraction();

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    int prevVol = SoundManager.getSfxVolume();
                    SoundManager.setSfxVolume(15);
                    SoundManager.play(SoundManager.FIRE_TRUCK);
                    SoundManager.setSfxVolume(prevVol);
                }
            }
        });

    }

    @Override
    public void grabFocus() {
        // We use invokeLater to ensure the component is fully 
        // realized in the UI tree before requesting focus
        SwingUtilities.invokeLater(() -> {
            this.setFocusable(true);
            this.requestFocusInWindow();
        });
    }

    @Override
    public void refresh() {
        grabFocus();

        model.restartGame();
        clickZones.clear();
        currentScreen = Screen.INTRO;
        feedbackMsg = "";
        imgHero = loadHeroImage();
        hintCount = 0;

        currentInput.setLength(0);

        stopAll();
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
    }

    private BufferedImage loadHeroImage() {
        try {
            String avatar = Game.getInstance().getAvatarPath();
            if ("Mia".equalsIgnoreCase(avatar)) {
                return img("/images/general/hero1_standing.png");
            }
            if ("Zyx".equalsIgnoreCase(avatar)) {
                return img("/images/general/alien_standing.png");
            }
            return img("/images/general/hero2_standing.png");
        } catch (Exception ignored) {
            System.out.println("[Fire] Error loading hero");
        }
        return null;
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
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char key = e.getKeyChar();

                if (!Character.isLetter(key) || currentScreen != Screen.GAME) {
                    return;
                }
                SoundManager.play(SoundManager.GAME_KEY_CLICK);

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
        SoundManager.play(SoundManager.GAME_BTN_CLICK);

        if (currentInput.toString().length() < model.getCurrentWord().length()) {
            showFeedback("Complete the word!");
            return;
        }

        if (model.submitWord(currentInput.toString())) {
            if (model.getStreakCount() > 1) {
                showFeedback("Fire extinguished! Score bonus :D");
            } else {
                showFeedback("Fire extinguished!");
            }

            hintCount = 0;
            extinguishFire();

            game.addScore(BASE_SCORE * model.getStreakCount());

            if (model.getFireCounter() < NUMBER_OF_FIRES) {
                model.pickNextWord();

                playWord();

            } else {
                speakWord("Congratulations. You did it hero!");
                stopAll();
                SoundManager.startMusic(SoundManager.FIRE_BGM);
                currentScreen = Screen.FINISH;
                SoundManager.play(SoundManager.GAME_FINISH);
                game.setFireCompleted(true);
            }

        } else {
            if (model.getWrongCount() % REIGNITE_LIMIT == 0 && model.getFireCounter() > 0) {
                showFeedback("Oh no! A fire relit!");
                reigniteFire();
                game.addMisspelledWord(model.getCurrentWord());
            } else if (model.getStreakCount() > 1) {
                showFeedback("Score bonus lost, try again!");
            } else {
                showFeedback("Try again! Some words sound the same but are spelled different.");
            }

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
        }
        resetInputWithHints();
        repaint();
    }

    private void handleDelete() {
        // Prevent deleting if the current length is at or below the hintCount
        SoundManager.play(SoundManager.GAME_BTN_CLICK);
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
            model.incrementFireCounter();
        } else {
            model.decrementFireCounter();
        }

        int startSearch = (model.getFireIndex() + 1) % NUMBER_OF_FIRES;

        for (int i = 0; i < NUMBER_OF_FIRES; i++) {
            int checkIndex = (startSearch + i) % NUMBER_OF_FIRES;

            if (flames.get(checkIndex).isActive()) {
                model.setFireIndex(checkIndex);
                break;
            }
        }
        repaint();
    }

    // Handle fire extinguishing logic (extinguish the fire when necessary)
    private void extinguishFire() {
        if (model.getFireIndex() >= 0 && model.getFireIndex() < flames.size()) {
            flames.get(model.getFireIndex()).extinguish(); // Play the end animation for this fire

            updateFireCounter(1); // Increment the counter when the fire is extinguished
            SoundManager.play(SoundManager.FIRE_HOSE);
        }
    }

    // Handle reigniting logic (allow fires to be reignited after extinguishing)
    private void reigniteFire() {
        updateFireCounter(-1);

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
        startAnimations();
        SoundManager.startMusic(SoundManager.FIRE_BGM_FIRE);

        currentScreen = Screen.GAME;
        grabFocus();

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
        SoundManager.stopAll();
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
                    SoundManager.play(SoundManager.GAME_BTN_CLICK);
                    Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
                    return;
                }

                if (currentScreen == Screen.INTRO) {
                    Rectangle start = clickZones.get("start_btn");
                    if (start != null && start.contains(mx, my)) {
                        SoundManager.play(SoundManager.GAME_BTN_CLICK);
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
                    SoundManager.play(SoundManager.GAME_BTN_CLICK);
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

        // ── 2. NPCs — placed at the bottom ground line ────────────────────────
        paintNPCS(g2, W, H);

        // ── 3. Fire Truck — positioned on the left ────────────────────────────
        paintFireTruck(g2, W, H);

        // ── 4. Hero — centre-facing toward the building ───────────────────────
        paintHero(g2, W, H);

        // ── 5. Left Panel — back button and status display ────────────────────
        paintLeftPanel(g2, H);

        // ── 6. Screen Overlays — handles Intro, Game, and Finish states ───────
        if (null == currentScreen) {
            paintFinishOverlay(g2, W, H);
        } else {
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

        // ── 7. Feedback — floating message alerts ─────────────────────────────
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

        // Increased height to 300 to fit the extra text lines comfortably
        int cardW = Math.min(W - 80, 680);
        int cardH = 300;
        int cardX = W / 2 - cardW / 2;
        int cardY = H / 2 - cardH / 2;

        // Draw the outer_box as the card background
        paintOuterBoxButton(g2, cardX, cardY, cardW, cardH, "", "");

        // Title text inside card
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(new Color(0x3B2A1A));
        drawCentredString(g2, "The Pet Shop is on fire!", W / 2, cardY + 45);

        // Description text - using a slightly larger font for readability
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(new Color(0x5A4A3A));

        // Line 1
        drawCentredString(g2,
                "Listen to the word spoken aloud and then type the word on your keyboard.",
                W / 2, cardY + 85);

        // Line 2
        drawCentredString(g2,
                "Each correct word puts out a fire.",
                W / 2, cardY + 110);

        // Line 3
        drawCentredString(g2,
                "Be careful, the fire will spread if you guess wrong 4 times.",
                W / 2, cardY + 135);

        // Line 4
        g2.setFont(new Font("Monospaced", Font.BOLD, 16)); // Slightly emphasized
        drawCentredString(g2, "Good luck Hero!", W / 2, cardY + 170);

        // Divider line shifted down to accommodate more text
        g2.setColor(new Color(0xC8A97A));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cardX + 40, cardY + 195, cardX + cardW - 40, cardY + 195);
        g2.setStroke(new BasicStroke(1));

        // Start Rescue button adjusted toward the bottom of the taller card
        int btnW = 260, btnH = 58;
        int btnX = W / 2 - btnW / 2;
        int btnY = cardY + cardH - btnH - 25;
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
            int firesLeft = NUMBER_OF_FIRES - model.getFireCounter();
            int guessesLeft = REIGNITE_LIMIT - (model.getWrongCount() % REIGNITE_LIMIT);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));

            // Draw Status Text inside the panel
            int textX = panelX + 20;
            g2.drawString("STATUS", textX, panelY + 40);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString("Fires Left: " + firesLeft, textX, panelY + 75);

            // Conditional logic for the flare warning
            if (model.getFireCounter() == 0) {
                // If no fires have been put out yet, there's nothing to "relight"
                g2.setFont(new Font("Monospaced", Font.ITALIC, 14));
                g2.drawString("All the fires", textX, panelY + 110);
                g2.drawString("are lit!", textX, panelY + 130);
            } else {
                // Standard warning when there are extinguished fires that could relight
                g2.drawString("Next flare in:", textX, panelY + 110);
                g2.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2.drawString(guessesLeft + " tries", textX, panelY + 130);
            }

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
    // Paint Hero
    private void paintHero(Graphics2D g2, int W, int H) {
        if (imgHero == null) {
            System.out.println("[Fire] Unable to paint hero");
            return;
        }

        // Drawn size — scale down to look proportional next to NPCs
        int drawW = 160;
        int drawH = (int) ((double) HERO_FH / HERO_FW * drawW);  // keep aspect ratio

        // Ground line matches NPC ground Y
        int groundY = (int) (H * 0.85) - drawH;

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

    // Paint Fire Truck
    private void paintFireTruck(Graphics2D g2, int W, int H) {
        if (imgFireTruck == null) {
            return;
        }

        // Scale the truck
        int truckWidth = 475;
        int truckHeight = 225;

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
        // 1. Box Dimensions - Decreased width from 280 to 200
        int boxW = 200;
        int boxH = 50;
        int boxX = (W - boxW) / 2; // Centers the box on the screen
        int boxY = 15;

        // 2. Draw the Outer Box background and borders
        paintOuterBox(g2, boxX, boxY, boxW, boxH);

        // 3. Configure Font
        g2.setFont(new Font("Monospaced", Font.BOLD, 18)); // Slightly smaller font for a smaller box
        g2.setColor(new Color(0x3B2A1A));
        FontMetrics fm = g2.getFontMetrics();

        // 4. Horizontal Centering Logic
        String scoreText = "SCORE: " + game.getScore();

        // Calculate the X coordinate: (Box Width - Text Width) / 2 + Box's X Position
        int textX = boxX + (boxW - fm.stringWidth(scoreText)) / 2;

        // 5. Vertical Centering Logic
        int textY = boxY + (boxH / 2) + (fm.getAscent() / 2) - 2;

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

// ── Game UI: word box + buttons ───────────────────────
    private void paintGameUI(Graphics2D g2, int W, int H) {
        // ── Bottom: outer_box word display row ────────────────────────────────
        String word = model.getCurrentWord();
        int wordLen = word.length();
        int slotSize = 64, slotGap = 10;
        int totalSlotW = wordLen * (slotSize + slotGap) - slotGap;

        // The outer box spans the word slots
        int boxPad = 25;
        int boxW = totalSlotW + boxPad * 2;
        int boxH = slotSize + boxPad;
        int boxX = (W - boxW) / 2;
        int boxY = H - boxH - 40;

        // Draw outer_box tiled as the container
        paintOuterBox(g2, boxX, boxY, boxW, boxH);

        paintInputBox(g2, W, H, boxY, boxH);

        int btnSize = 56;
        int spacing = 30;

        // 1. Back button (Left of box) + Label
        int backX = boxX - btnSize - spacing;
        int backY = boxY + boxH / 2 - btnSize / 2;
        paintLabeledIcon(g2, backX, backY, btnSize, imgBack, "BACKSPACE", "back_btn");

        // 2. Check button (Right of box) + Label
        int checkX = boxX + boxW + spacing;
        int checkY = backY;
        paintLabeledIcon(g2, checkX, checkY, btnSize, imgCheck, "ENTER", "check_btn");
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
        drawCentredString(g2, "Total Score: " + game.getScore(), W / 2, statsStartY);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCentredString(g2, "Correct Guesses: " + model.getCorrectCount(), W / 2, statsStartY + 35);
        drawCentredString(g2, "Mistakes Made: " + model.getTotalWrongCount(), W / 2, statsStartY + 65);

        // 6. Final Praise
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(new Color(0x2D5A27)); // Dark green for a "win" feel
        drawCentredString(g2, "The Pet Shop is safe thanks to you!", W / 2, cardY + 210);

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

    private void paintLabeledIcon(Graphics2D g2, int x, int y, int size,
            BufferedImage icon, String label, String zoneId) {
        // 1. Draw the Icon itself (from your existing code)
        paintIconBox(g2, x, y, size, icon, zoneId);

        // 2. Setup Larger Font and Metrics
        // Increased from 11 to 14
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        int textW = fm.stringWidth(label);
        int textH = fm.getHeight();

        // 3. Positioning and Sizing the background
        int hPadding = 8;  // Side padding
        int vPadding = 4;  // Top/Bottom padding
        int bgW = textW + (hPadding * 2);
        int bgH = textH + vPadding;

        // Center horizontally relative to the icon
        int bgX = x + (size / 2) - (bgW / 2);
        // Positioned 8 pixels below the icon
        int bgY = y + size + 8;

        // 4. Draw Background "Pill" (Dark for high contrast)
        g2.setColor(new Color(0, 0, 0, 190)); // Slightly more opaque for visibility
        g2.fillRoundRect(bgX, bgY, bgW, bgH, 10, 10);

        // 5. Draw the Text in White
        g2.setColor(Color.WHITE);
        // fm.getAscent() ensures the text sits correctly inside the pill
        g2.drawString(label, bgX + hPadding, bgY + fm.getAscent() + (vPadding / 2) - 2);

        // 6. Update the Click Zone so the text is also clickable
        clickZones.put(zoneId, new Rectangle(x, y, size, size + bgH + 8));
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
