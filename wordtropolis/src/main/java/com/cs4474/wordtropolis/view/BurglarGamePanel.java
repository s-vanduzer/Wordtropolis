package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.BurglarGameModel;
import com.cs4474.wordtropolis.model.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.Timer;


/**
 * BurglarGamePanel.java
 * Custom painting panel for the burglar chase word game.
 * Players select missing letters to advance the hero and catch the burglar.
 */
public class BurglarGamePanel extends JPanel {

    // ── Model ─────────────────────────────────────────────────────────────────
    private final BurglarGameModel model;

    // ── Images ────────────────────────────────────────────────────────────────
    private BufferedImage imgBg, imgHero, imgRobber, imgProgressBar;
    private BufferedImage imgLetterBox, imgOuterBox;
    private BufferedImage imgCheck, imgBack, imgSparkle;
    
    // ── Sprite constants ──────────────────────────────────────────────────────
    private static final int SPKL_FRAMES = 14, SPKL_W = 32, SPKL_H = 32;
    private static final int PB_ROW_H = 24;
    private static final int PB_FILL_ROW = 1;  // Cyan fill row
    
    // ── Animation state ───────────────────────────────────────────────────────
    private int sparkleFrame = 0;
    private boolean showSparkle = false;
    private int sparkleX, sparkleY;
    private Timer sparkleTimer, feedbackTimer;
    
        // -- Sprite Animation State --
    private int animationFrame = 0;
    private Timer animationTimer;
    private static final int TOTAL_FRAMES = 4; // Both sheets have 4 frames
    private static final int FRAME_W = 160;     // Width of one sprite frame
    private static final int FRAME_H = 160;     // Height of one sprite frame
    
    // ── Floating letter tile positions ────────────────────────────────────────
    private int[] tileBaseX, tileBaseY;
    
    // ── State flags ───────────────────────────────────────────────────────────
    private boolean waitingForNext = false;
    
    // ── Feedback message ──────────────────────────────────────────────────────
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;
    
    // ── Drag & Drop state ─────────────────────────────────────────────────────
    private int dragTileIndex = -1;
    private boolean dragFromArr = false;
    private int dragCurrentX, dragCurrentY;
    private boolean isDragging = false;
    private int hoverAvailIdx = -1;
    private int hoverArrIdx = -1;
    private int cameraOffset = 0;
    
    private enum Screen { INTRO, GAME, WIN }
private Screen currentScreen = Screen.INTRO;
    
    // ── Click zones ───────────────────────────────────────────────────────────
    private java.util.Map<String, Rectangle> clickZones = new HashMap<>();
    
    // ── Constructor ───────────────────────────────────────────────────────────
    
    public BurglarGamePanel() {
        model = new BurglarGameModel(Game.getInstance().getWordList());
        setLayout(null);
        setBackground(Color.BLACK);
        loadImages();
        setupKeyboard();
        setupMouseInteraction();
        computeTilePositions();
        speakWord();
        new Timer(16, e -> {
    updateCamera();
    repaint();
}).start();
        
        
        animationTimer = new Timer(150, e -> {
        animationFrame = (animationFrame + 1) % TOTAL_FRAMES;
        repaint();
    });
    animationTimer.start();
    
    
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopAll();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }
    
    // ═══════════════════════════════ LOADING ═════════════════════════════════
    
    private void loadImages() {
        
        try {
        
        imgRobber = img("/images/burglar_game/robber.png");
        imgBg          = img("/images/burglar_game/street.png");//        
        imgRobber      = img("/images/burglar_game/robber.png");
        imgProgressBar = img("/images/cat_game/progress_bar.png");
        imgLetterBox   = img("/images/cat_game/individual_box.png");
        imgOuterBox    = img("/images/cat_game/outer_box.png");
        imgCheck       = img("/images/cat_game/check.png");
        imgBack        = img("/images/cat_game/back.png");
        imgSparkle     = img("/images/cat_game/blue_sparkle.png");
        
        // Pick hero sprite sheet based on what PickHeroPanel saved via setAvatarPath().
        // HERO_LABELS are "Mia" (girl), "Alex" (boy), "Zyx" (alien).
        String avatarPath = Game.getInstance().getAvatarPath();
        if ("Mia".equalsIgnoreCase(avatarPath)) {
            imgHero = img("/images/burglar_game/hero1_running.png");
        } else if ("Zyx".equalsIgnoreCase(avatarPath)) {
            imgHero = img("/images/burglar_game/alien_running.png");
        } else {
            // Default: Alex / boy hero
            imgHero = img("/images/burglar_game/hero2_running.png");
        }
        
        } catch (Exception e){
        System.err.println("Error loading Burglar Game sprites: " + e.getMessage());
    }
        
    }
    
    private BufferedImage img(String path) {
        try {
            URL r = getClass().getResource(path);
            if (r == null) { System.out.println("[Burglar] missing: " + path); return null; }
            return ImageIO.read(r);
        } catch (Exception e) { System.out.println("[Burglar] err: " + path); return null; }
    }
    
    
    // ═══════════════════════════════ UPDATE CAMERA ══════════════════════════════
    
    private void updateCamera() {
        int stepSize = 50;
        // 1. Calculate the robber's ABSOLUTE position in the world (0 to background width)
        int robberWorldX = 100 + (model.getRobberPosition() * stepSize);

        // 2. Define the "Trigger Point" (e.g., 60% of the screen width)
        // Using getWidth() - 150 often causes issues if the screen is small. 
        // Let's trigger scrolling once he passes the middle.
        int scrollTriggerX = getWidth() / 2;

        int targetOffset = 0;

        // 3. If the robber is past the trigger point, the camera follows him
        if (robberWorldX > scrollTriggerX) {
            targetOffset = robberWorldX - scrollTriggerX;
        }

        // 4. Smooth movement (Interpolation)
        double diff = targetOffset - cameraOffset;
        if (Math.abs(diff) > 0.5) { 
            cameraOffset += diff * 0.1; // Adjust 0.1 for speed (lower = smoother)
        } else {
            cameraOffset = targetOffset; 
        }

        // 5. Clamp to background limits
        if (imgBg != null) {
            int maxOffset = imgBg.getWidth() - getWidth();
            cameraOffset = Math.max(0, Math.min(cameraOffset, maxOffset));
        }
    }
    
    // ═══════════════════════════════ ANIMATIONS ══════════════════════════════
    
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
            for (Timer t : new Timer[]{sparkleTimer, feedbackTimer, animationTimer}) {
                if (t != null) t.stop();
            }
}
    
    // ═══════════════════════════════ KEYBOARD ════════════════════════════════
    
    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!model.isGameActive() || waitingForNext) return;
                char key = Character.toUpperCase(e.getKeyChar());
                if (!Character.isLetter(key)) return;
                
                // Find the letter in available pool
                for (int i = 0; i < model.getAvailableLetters().size(); i++) {
                    if (model.getAvailableLetters().get(i) == key) {
                        model.selectLetter(i);
                        BurglarSoundManager.play(BurglarSoundManager.SFX_TILE_CLICK);
                        triggerSparkle(getWidth() / 2, getHeight() / 2 - 60);
                        computeTilePositions();
                        repaint();
                        return;
                    }
                }
                
                // Letter not available
                BurglarSoundManager.play(BurglarSoundManager.SFX_ERROR);
                showFeedback("'" + key + "' is not available!", UITheme.ACCENT_RED);
                shakeEffect();
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Key Pressed: " + e.getKeyCode());
                if (!model.isGameActive() || waitingForNext) return;
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_BACK_SPACE && model.getPlayerArrangement().size() > 0) {
                    model.removeLetter(model.getPlayerArrangement().size() - 1);
                    BurglarSoundManager.play(BurglarSoundManager.SFX_TILE_CLICK);
                    computeTilePositions();
                    repaint();
                } else if (c == KeyEvent.VK_ENTER) {
                    handleSubmit();
                } else if (c == KeyEvent.VK_ESCAPE) {
                    model.clearArrangement();
                    computeTilePositions();
                    repaint();
                }
            }
        });
    }
    
    // ═══════════════════════════════ TILE POSITIONS ═══════════════════════════
    
    private void computeTilePositions() {
        int count = model.getAvailableLetters().size();
        tileBaseX = new int[count];
        tileBaseY = new int[count];
        
        int W = getWidth(), H = getHeight();
        int tileSize = 64;
        int totalW = count * (tileSize + 10) - 10;
        int startX = (W - totalW) / 2;
        int baseY = 120;  // Above the word box
        
        for (int i = 0; i < count; i++) {
            tileBaseX[i] = startX + i * (tileSize + 10);
            // Gentle arc
            double angle = 0;
            tileBaseY[i] = baseY - (int)(Math.sin(angle) * 15);
        }
    }
    
    // ═══════════════════════════════ GAME LOGIC ══════════════════════════════
    
    private void handleSubmit() {
        if (waitingForNext || !model.isGameActive()) return;
        
        // Check if all missing letters are selected
        if (model.getPlayerArrangement().size() < model.getCurrentQuestion().missingLetters.length()) {
            showFeedback("Select all missing letters first!", UITheme.ACCENT_YELLOW);
            return;
        }
        
        boolean correct = model.checkAnswer();
        
        if (correct) {
            showFeedback("Correct! The hero moves forward!", UITheme.ACCENT_TEAL);
            BurglarSoundManager.playFrom(BurglarSoundManager.SFX_CORRECT, 1.0, 1.5);
            System.out.println("Hero position: " + model.getHeroPosition());
            int points = BurglarGameModel.POINTS_PER_CORRECT;
            Game.getInstance().addScore(points);
            triggerSparkle(getWidth() / 2, getHeight() / 2);
            repaint();
            
            if (model.isComplete()) {
                gameComplete();
            } else if (model.isGameActive()) {
                waitingForNext = true;
                new Timer(1400, e -> {
                    model.loadNextWord();
                    refreshGame();
                    ((Timer)e.getSource()).stop();
                }).start();
            }
        } else {
            showFeedback("Incorrect! The he's is getting away!", UITheme.ACCENT_RED);

            BurglarSoundManager.play(BurglarSoundManager.SFX_ERROR);
            shakeEffect();
            repaint();
            
            if (model.isFailed()) {
                gameFailed();
            } else if (model.isGameActive()) {
                waitingForNext = true;
                new Timer(1400, e -> {
                    model.loadNextWord();
                    refreshGame();
                    ((Timer)e.getSource()).stop();
                }).start();
            }
        }
        
        if (model.isComplete()) {
       // 1. Mark as completed in the global state
       Game.getInstance().setBurgularCompleted(true); // This enables the "OK" badge on the map
    

       // 2. Play a win sound or show a "Mission Accomplished" screen
       // BurglarSoundManager.play(BurglarSoundManager.SFX_WIN);

       // 3. Return to the Map (which will now show the "OK" badge)
//       JOptionPane.showMessageDialog(this, "You caught the burglar! City safe!");
       Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
}
    }
    
    private void refreshGame() {
        waitingForNext = false;
        computeTilePositions();
        speakWord();
        repaint();
    }

    private void gameComplete() {
        // 1. Mark as completed in the global singleton
        Game.getInstance().setBurgularCompleted(true); 

//        showFeedback("Hero caught the burglar! Mission Complete!", UITheme.ACCENT_TEAL);
        BurglarSoundManager.play(BurglarSoundManager.SFX_LEVEL_COMPLETE);

        new Timer(3000, e -> {
            showResultDialog(true);
            ((Timer)e.getSource()).stop();
        }).start();
    }
    
    private void gameFailed() {
        showFeedback("The burglar escaped! Game Over!", UITheme.ACCENT_RED);
        BurglarSoundManager.play(BurglarSoundManager.SFX_ERROR);
        
        new Timer(3000, e -> {
            showResultDialog(false);
            ((Timer)e.getSource()).stop();
        }).start();
    }
    
    private void showResultDialog(boolean won) {
       BurglarSoundManager.stopMusic();
        stopAll();
        
        String title = won ? "Victory!" : "Game Over!";
        String message = "<html><div style='font-size:14px;text-align:center;padding:10px'>"
            + "<b>" + title + "</b><br><br>"
            + "Score: <b>" + model.getTotalScore() + "</b><br>"
            + "Words completed: " + model.getWordsCompleted()  + "<br>"
            + "Wrong attempts: " + model.getIncorrectAttempts()
            + "</div></html>";
        
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
        Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
    }
    
    private void speakWord() {
        BurglarGameModel.WordQuestion q = model.getCurrentQuestion();
        if (q == null) return;
        
        // Speak the word with missing letters as blanks
        String speakText = q.displayWord.replace('_', '?');
        
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name", "").toLowerCase();
                
                if (os.contains("mac")) {
                    new ProcessBuilder("say", "-v", "Alex", "-r", "110", 
                        "Spell the word: " + speakText).start().waitFor();
                } else if (os.contains("win")) {
                    String ps = "Add-Type -AssemblyName System.Speech;"
                              + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer;"
                              + "$s.Rate = -2;"
                              + "$s.Volume = 100;"
                              + "$s.Speak('Spell the word: " + speakText.replace("'", "") + "');";
                    new ProcessBuilder("powershell", "-Command", ps).start().waitFor();
                } else {
                    new ProcessBuilder("espeak", "-s", "120", "-a", "200", 
                        "Spell the word: " + speakText).start().waitFor();
                }
            } catch (Exception e) {
                System.out.println("[Burglar] TTS not available: " + e.getMessage());
            }
        }, "tts-thread").start();
    }
    
    private void showFeedback(String msg, Color c) {
        feedbackMsg = msg;
        feedbackColor = c;
        if (feedbackTimer != null) feedbackTimer.stop();
        feedbackTimer = new Timer(3000, e -> { feedbackMsg = ""; repaint(); ((Timer)e.getSource()).stop(); });
        feedbackTimer.start();
        repaint();
    }
    
    // ── Shake effect ──────────────────────────────────────────────────────────
    private Timer shakeTimer;
    private int shakeTick;
    
    private void shakeEffect() {
        if (shakeTimer != null && shakeTimer.isRunning()) return;
        shakeTick = 0;
        shakeTimer = new Timer(40, e -> {
            if (++shakeTick > 8) { shakeTimer.stop(); repaint(); }
            else repaint();
        });
        shakeTimer.start();
    }
    
        
    @Override
    protected void paintComponent(Graphics g) {

        
super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    int W = getWidth(), H = getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      
        
        // 1. Background
        paintBackground(g2, W, H);
    // Always paint the background so the start screen has the street behind it

    if (currentScreen == Screen.INTRO) {
        paintStartScreen(g2, W, H);
    } else {
        // Your existing game drawing logic
        paintProgressBar(g2, W, H);
        paintCharacters(g2, W, H);
        paintWordDisplay(g2, W, H);
//        paintAvailableLetters(g2);
        
        // 4. Sparkle effect
        if (showSparkle) paintSparkle(g2);
        
        // 5. Word display with blanks
        paintWordDisplay(g2, W, H);
        
        // 6. Floating letter tiles
        paintLetterTiles(g2);
        
        // 7. Score display
        paintScoreBox(g2, W);
        
        // 8. Back button
        paintBackButton(g2);
        
        // 9. Check button
        paintCheckButton(g2, W, H);
        
        // 10. Feedback message
        if (!feedbackMsg.isEmpty()) paintFeedback(g2, W, H);
        
        // 11. Drag ghost
        if (isDragging && dragTileIndex >= 0) paintDragGhost(g2);
        
        // 12. Shake flash
        if (shakeTimer != null && shakeTimer.isRunning() && shakeTick % 2 == 0) {
            g2.setColor(new Color(1f, 0f, 0f, 0.12f));
            g2.fillRect(0, 0, W, H);
        }
        
        g2.dispose();
    }
    }
    
    private void paintBackground(Graphics2D g2, int W, int H) {
        if (imgBg != null) {
            int imgW = imgBg.getWidth();
            int imgH = imgBg.getHeight();

            // 1. Calculate the scale factor based on the current panel height
            // This ensures the street fills the screen vertically.
            double scale = (double) H / imgH;
            int scaledWidth = (int) (imgW * scale);

            // 2. Calculate the xOffset for the loop
            // We use cameraOffset % scaledWidth to ensure it wraps around.
            int xOffset = (int) (cameraOffset % scaledWidth);

            // 3. Draw the first instance of the background
            g2.drawImage(imgBg, -xOffset, 0, scaledWidth, H, null);

            // 4. Draw the second instance for a seamless loop
            // This appears as the first one slides off the left side.
            if (xOffset > 0) {
                g2.drawImage(imgBg, scaledWidth - xOffset, 0, scaledWidth, H, null);
            }
        } else {
            // Fallback if image fails to load
            g2.setPaint(new GradientPaint(0, 0, new Color(0x87CEEB), 0, H, new Color(0xE0F6FF)));
            g2.fillRect(0, 0, W, H);
        }
    }
    
    private void paintProgressBar(Graphics2D g2, int W, int H) {
        int barX = W / 2 - 200;
        int barY = 60;
        int barW = 400;
        int barH = 24;
        
        // Background
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(barX - 3, barY - 3, barW + 6, barH + 6, 8, 8);
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
        
        // Fill based on hero position progress
        double progress = model.getProgress();
        int fillW = (int)(barW * progress);
        
        if (imgProgressBar != null && fillW > 0) {
            int srcX1 = 10, srcX2 = 35;
            int srcY1 = (PB_FILL_ROW * PB_ROW_H) + 15;
            int srcY2 = ((PB_FILL_ROW + 1) * PB_ROW_H) - 10;
            g2.drawImage(imgProgressBar,
                    barX, barY, barX + fillW, barY + barH,
                    srcX1, srcY1, srcX2, srcY2, null);
        } else if (fillW > 0) {
            g2.setColor(UITheme.ACCENT_TEAL);
            g2.fillRect(barX, barY, fillW, barH);
        }
        
        // Labels
g2.setFont(UITheme.FONT_SMALL);
    g2.setColor(UITheme.TEXT_BRIGHT);
    
    // Change "START" and "CATCH!" to reflect the chase
    drawCentredString(g2, "CHASE START", barX + 40, barY + barH + 15);
    drawCentredString(g2, "CAUGHT!", barX + barW - 40, barY + barH + 15);
    
    }
    
        // Inside paintCharacters(Graphics2D g2, int W, int H)
private void paintCharacters(Graphics2D g2, int W, int H) {
    int groundY = (int)(H * 0.85);
    int charSize = 105; 
    int stepSize = 50; 

    // Use a consistent screen-space calculation
    int heroX = 100 + (model.getHeroPosition() * stepSize) - (int)cameraOffset;
    int robberX = 100 + (model.getRobberPosition() * stepSize) - (int)cameraOffset;

    //Calculate source coordinates precisely
    int sx1 = animationFrame * FRAME_W;
    int sx2 = sx1 + FRAME_W; 
    int sy1 = 0;
    int sy2 = FRAME_H;

    // Draw Hero
    if (imgHero != null) {
        g2.drawImage(imgHero, 
            heroX, groundY - charSize, heroX + charSize, groundY, // Destination
            sx1, sy1, sx2, sy2,                                   // Source
            null);
    }

    // Draw Robber
    if (imgRobber != null) {
        g2.drawImage(imgRobber, 
            robberX, groundY - charSize, robberX + charSize, groundY, 
            sx1, sy1, sx2, sy2, 
            null);
    }
}
        

    private void paintStartScreen(Graphics2D g2, int W, int H) {
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
        drawCentredString(g2, "Help catch the burglar!", W / 2, cardY + 48);

        // Description text inside card — two lines if needed
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(new Color(0x5A4A3A));
        drawCentredString(g2,
                "Fill in the missing letters. Each correct word makes you faster!",
                W / 2, cardY + 84);
        drawCentredString(g2,
                "Type with keyboard or drag tiles into the box.",
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
  
        private void paintWordDisplay(Graphics2D g2, int W, int H) {
        BurglarGameModel.WordQuestion q = model.getCurrentQuestion();
        if (q == null) return;
        
        int slotSize = 64;
        int slotGap = 10;
        int wordLen = q.displayWord.length();
        int totalW = wordLen * (slotSize + slotGap) - slotGap;
        int boxPad = 20;
        int boxW = totalW + boxPad * 2;
        int boxH = slotSize + boxPad;
        int boxX = (W - boxW) / 2;
        int boxY = 200;
        
        // Draw outer box container
        paintOuterBox(g2, boxX, boxY, boxW, boxH);
        
        // Draw each character slot
        int slotStartX = boxX + boxPad;
        List<Character> arr = model.getPlayerArrangement();
        int placedCount = 0;
        
        for (int i = 0; i < wordLen; i++) {
            int sx = slotStartX + i * (slotSize + slotGap);
            int sy = boxY + boxPad / 2;
            char c = q.displayWord.charAt(i);
            
            if (c == '_') {
                // This is a blank slot
                if (placedCount < arr.size()) {
                    paintLetterTile(g2, sx, sy, slotSize, String.valueOf(arr.get(placedCount)), true, placedCount);
                } else {
                    paintEmptySlot(g2, sx, sy, slotSize);
                }
                placedCount++;
            } else {
                // Fixed letter - no interaction
                paintLetterTile(g2, sx, sy, slotSize, String.valueOf(c), false, -1);
            }
        }
    }
    
    private void paintLetterTiles(Graphics2D g2) {
        List<Character> avail = model.getAvailableLetters();
        if (tileBaseX == null || tileBaseX.length != avail.size()) {
            computeTilePositions();
        }
        
        for (int i = 0; i < avail.size(); i++) {
            int x = tileBaseX[i];
            int y = tileBaseY[i];
            paintLetterTile(g2, x, y, 64, String.valueOf(avail.get(i)), false, i);
        }
    }
    
    private void paintLetterTile(Graphics2D g2, int x, int y, int size, 
                                  String letter, boolean placed, int index) {
        // Skip if dragging this tile
        if (isDragging && dragTileIndex == index && dragFromArr == placed) return;
        
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
        
        g2.setFont(new Font("Monospaced", Font.BOLD, size / 2 + 2));
        g2.setColor(placed ? UITheme.TEXT_BRIGHT : new Color(0x3B2A1A));
        drawCentredString(g2, letter, x + size / 2, y + size / 2 + 6);
    }
    
    private void paintEmptySlot(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x, y, size, size, 6, 6);
        if (imgLetterBox != null) {
            g2.setColor(new Color(1f, 1f, 1f, 0.2f));
            g2.fillRect(x, y, size, size);
        }
        g2.setColor(new Color(1f, 1f, 1f, 0.4f));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                0, new float[]{4, 4}, 0));
        g2.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 6, 6);
        g2.setStroke(new BasicStroke(1));
        
        g2.setFont(new Font("Monospaced", Font.BOLD, size / 2 + 2));
        g2.setColor(new Color(0xAAAAAA));
        drawCentredString(g2, "?", x + size / 2, y + size / 2 + 6);
    }
    
    private void paintScoreBox(Graphics2D g2, int W) {
        
         // FIX 2: taller bar + zoom into ONE bar from the sprite sheet
        int tbX = 160, tbY = 8;
        int tbH = 40;
        // Leave 160px on the right for the score box (140px wide + 10px gap + 10px margin)
        int tbW = W - 320;
             // ── Score box: same height as timer, flush to the RIGHT edge ──────────
        // Uses paintOuterBoxButton so it gets the same outer_box.png border
        // as the Back button — cream fill + top/bottom strip borders
        int scoreW = 140, scoreH = tbH;
        int scoreX = tbX + tbW + 20;   // 10px gap after timer bar right edge
        int scoreY = tbY;
        paintOuterBoxButton(g2, scoreX, scoreY, scoreW, scoreH,
                "Score: " + model.getTotalScore(), "");
        
    }
    
    private void paintBackButton(Graphics2D g2) {
        int btnSize = 56;
        int panelX = 14;
        int x = 14;
        int y = 12;
        
//        if (imgBack != null) {
//            int pad = 6;
//            g2.drawImage(imgBack,
//                    x + pad + (btnSize - pad * 2), y + pad,
//                    x + pad, y + pad + (btnSize - pad * 2),
//                    0, 0, imgBack.getWidth(), imgBack.getHeight(), null);
//        }
//        clickZones.put("back_btn", new Rectangle(x, y, btnSize, btnSize));
        
                        
 
        // NEW CHANGE B: replace banner with outer_box "Back" button
        // Uses only the outer_box.png image (top + bottom strips, centre filled)
        // Clicking it returns the player to the map screen
        int backBtnW = 120, backBtnH = 40;
        paintOuterBoxButton(g2, panelX, 12, backBtnW, backBtnH, "< Back", "back_to_map");
        clickZones.put("back_btn", new Rectangle(x, y, backBtnW, backBtnW));
    }
    
    private void paintCheckButton(Graphics2D g2, int W, int H) {
        int btnSize = 56;
        int x = W - btnSize - 14;
        int y = H - btnSize - 80;
        
        if (imgCheck != null) {
            int pad = 6;
            g2.drawImage(imgCheck, x + pad, y + pad, btnSize - pad * 2, btnSize - pad * 2, null);
        }
        clickZones.put("check_btn", new Rectangle(x, y, btnSize, btnSize));
    }
    
    private void paintSparkle(Graphics2D g2) {
        if (imgSparkle == null || !showSparkle) return;
        int scale = 3;
        int sx = sparkleFrame * SPKL_W;
        g2.drawImage(imgSparkle,
                sparkleX - SPKL_W, sparkleY - SPKL_H,
                sparkleX + SPKL_W * scale, sparkleY + SPKL_H * scale,
                sx, 0, sx + SPKL_W, SPKL_H, null);
    }
    
    private void paintDragGhost(Graphics2D g2) {
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
    
    private void paintFeedback(Graphics2D g2, int W, int H) {
        int fw = Math.min(W - 60, 500);
        int fh = 48;
        int fx = (W - fw) / 2;
        int fy = H - 100;
        paintOuterBox(g2, fx, fy, fw, fh);
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(feedbackColor);
        drawCentredString(g2, feedbackMsg, W / 2, fy + fh / 2 + 7);
    }
    
    // ── UI Helpers ────────────────────────────────────────────────────────────
    
    private void paintOuterBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(229, 226, 207));
        g2.fillRect(x, y, w, h);
        
        if (imgOuterBox != null) {
            int stripH = Math.min(8, h / 2);
            g2.drawImage(imgOuterBox, x, y, x + w, y + stripH,
                    0, 0, imgOuterBox.getWidth(), imgOuterBox.getHeight(), null);
            g2.drawImage(imgOuterBox, x, y + h - stripH, x + w, y + h,
                    0, imgOuterBox.getHeight(), imgOuterBox.getWidth(), 0, null);
        } else {
            g2.setColor(new Color(0xC8A97A));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }
    }
    
    private void paintLabelBox(Graphics2D g2, int x, int y, int w, int h,
                                String text, Font font, Color textColor) {
        g2.setFont(font);
        g2.setColor(textColor);
        drawCentredString(g2, text, x + w / 2, y + h / 2 + 5);
    }
    
//    private void drawCentredString(Graphics2D g2, String s, int cx, int cy) {
//        FontMetrics fm = g2.getFontMetrics();
//        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy);
//    }
//    
    // ═══════════════════════════════ MOUSE INTERACTION ════════════════════════
    
    private Rectangle wordBoxGeo() {
        BurglarGameModel.WordQuestion q = model.getCurrentQuestion();
        if (q == null) return new Rectangle(0, 0, 0, 0);
        
        int slotSize = 64, slotGap = 10;
        int wordLen = q.displayWord.length();
        int totalW = wordLen * (slotSize + slotGap) - slotGap;
        int boxPad = 20;
        int boxW = totalW + boxPad * 2;
        int boxH = slotSize + boxPad;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = 200;
        return new Rectangle(boxX, boxY, boxW, boxH);
    }
    
    private void setupMouseInteraction() {
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
               if (currentScreen == Screen.INTRO) {
        int W = getWidth(), H = getHeight();
        int btnW = 240, btnH = 60;
        int btnX = (W - btnW) / 2;
        int btnY = H / 2;
        
        Rectangle btnRect = new Rectangle(btnX, btnY, btnW, btnH);
        if (btnRect.contains(e.getPoint())) {
            startGame();
        }
        return; // Don't process game clicks while in Intro
    }
                int mx = e.getX(), my = e.getY();
                if (!model.isGameActive() || waitingForNext) return;
                
                int tileSize = 64;
                List<Character> avail = model.getAvailableLetters();
                
                // Check available tiles
                if (tileBaseX != null) {
                    for (int i = 0; i < avail.size(); i++) {
                        int tx = tileBaseX[i];
                        int ty = tileBaseY[i];
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
                
                // Check arrangement tiles
                Rectangle box = wordBoxGeo();
                int slotSize = 64, slotGap = 10, boxPad = 20;
                int slotStartX = box.x + boxPad;
                List<Character> arr = model.getPlayerArrangement();
                int placedCount = 0;
                String displayWord = model.getCurrentQuestion().displayWord;
                
                for (int i = 0; i < displayWord.length(); i++) {
                    if (displayWord.charAt(i) == '_') {
                        int sx = slotStartX + i * (slotSize + slotGap);
                        int sy = box.y + boxPad / 2;
                        if (placedCount < arr.size() && mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
                            isDragging = true;
                            dragFromArr = true;
                            dragTileIndex = placedCount;
                            dragCurrentX = mx;
                            dragCurrentY = my;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                        placedCount++;
                    }
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                
                // Button clicks
                Rectangle back = clickZones.get("back_btn");
                if (back != null && back.contains(mx, my) && !isDragging) {
                    BurglarSoundManager.stopMusic();
                    Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
                    return;
                }
                
                if (!model.isGameActive() || waitingForNext) {
                    isDragging = false;
                    return;
                }
                
                if (!isDragging) {
                    Rectangle check = clickZones.get("check_btn");
                    if (check != null && check.contains(mx, my)) {
                        handleSubmit();
                    }
                    return;
                }
                
                // Drop logic
                Rectangle box = wordBoxGeo();
                int slotSize = 64, slotGap = 10, boxPad = 20;
                boolean inBox = box.contains(mx, my);
                
                if (!dragFromArr && inBox) {
                    // Available tile dropped into word box
                    BurglarSoundManager.play(BurglarSoundManager.SFX_TILE_CLICK);
                    model.selectLetter(dragTileIndex);
                    computeTilePositions();
                    repaint();
                } else if (dragFromArr && !inBox) {
                    // Arrangement tile dragged out
                    BurglarSoundManager.play(BurglarSoundManager.SFX_TILE_CLICK);
                    model.removeLetter(dragTileIndex);
                    computeTilePositions();
                    repaint();
                } else if (dragFromArr && inBox) {
                    // Swapping within arrangement
                    int slotStartX = box.x + boxPad;
                    String displayWord = model.getCurrentQuestion().displayWord;
                    int targetSlot = -1;
                    int placedCount = 0;
                    
                    for (int i = 0; i < displayWord.length(); i++) {
                        if (displayWord.charAt(i) == '_') {
                            int sx = slotStartX + i * (slotSize + slotGap);
                            int sy = box.y + boxPad / 2;
                            if (placedCount != dragTileIndex && mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
                                targetSlot = placedCount;
                                break;
                            }
                            placedCount++;
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
                if (!model.isGameActive()) return;
                int mx = e.getX(), my = e.getY();
                int tileSize = 64;
                int newHoverAvail = -1, newHoverArr = -1;
                
                // Check available tiles
                List<Character> avail = model.getAvailableLetters();
                if (tileBaseX != null) {
                    for (int i = 0; i < avail.size(); i++) {
                        int tx = tileBaseX[i];
                        int ty = tileBaseY[i];
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
                String displayWord = model.getCurrentQuestion().displayWord;
                int placedCount = 0;
                
                for (int i = 0; i < displayWord.length(); i++) {
                    if (displayWord.charAt(i) == '_') {
                        int sx = slotStartX + i * (slotSize + slotGap);
                        int sy = box.y + boxPad / 2;
                        if (placedCount < arr.size() && mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
                            newHoverArr = placedCount;
                            break;
                        }
                        placedCount++;
                    }
                }
                
                boolean changed = (newHoverAvail != hoverAvailIdx || newHoverArr != hoverArrIdx);
                hoverAvailIdx = newHoverAvail;
                hoverArrIdx = newHoverArr;
                setCursor((newHoverAvail >= 0 || newHoverArr >= 0)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                if (changed) repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }
    

    

    
    private void startGame() {
    BurglarSoundManager.play(BurglarSoundManager.SFX_GAME_START);
    BurglarSoundManager.startMusic();
    
    currentScreen = Screen.GAME;
        // Show instruction dialog
    
//    // Show instruction dialog exactly like Cat Game
//    JOptionPane.showMessageDialog(this,
//        "<html><div style='font-size:14px;text-align:center;padding:10px'>"
//        + "<b>How to Play</b><br><br>"
//        + "Spell the words correctly to catch the burglar!<br>"
//        + "Drag the letters into the box below.<br><br>"
//        + "Each correct word moves the hero closer."
//        + "</div></html>",
//        "How to Play", JOptionPane.INFORMATION_MESSAGE);
        
    currentScreen = Screen.GAME;
    computeTilePositions();
    repaint();
    }
    
    
    
    
    
    
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        computeTilePositions();
    }    
    // ── Helpers ───────────────────────────────────────────────────────────────
    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}