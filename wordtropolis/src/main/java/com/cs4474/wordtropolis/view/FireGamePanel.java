/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.model.FireGameModel;
import com.cs4474.wordtropolis.model.Game;
import java.awt.*;
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

    private static final int REIGNITE_LIMIT = 4;
    private static final int NUMBER_OF_FIRES = 6;
    
    private final FireGameModel model;
    private BufferedImage imgBg, imgHero, imgFireTruck;
    private BufferedImage imgFireStart, imgFireLoop, imgFireEnd;

    private final Random random = new Random();

    private static final int HERO_COLS = 4;
    private static final int HERO_FW = 210;   // frame width  (843 / 4)
    private static final int HERO_FH = 316;   // frame height (1264 / 4)
    private static final int HERO_BACK_ROW = 3;    // last row = back-facing walk
    private int heroFrame = 0;
    private Timer heroTimer;

    // 6 FlameLabel instances in a 3x2 grid for fire animations
    private List<FlameLabel> flames;

    private int fireCounter = 0; // Counter for completed fires
    private int fireIndex = 0;

    private StringBuilder currentInput = new StringBuilder();
    private String feedbackMsg = "";

    public FireGamePanel() {
        model = new FireGameModel(Game.getInstance().getWordList());
        loadImages();
        initFires();
        setUpKeyboard();
        speakWord("The word on the window is " + model.getCurrentWord());
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
            FlameLabel flame = new FlameLabel(imgFireStart, imgFireLoop, imgFireEnd);
            flames.add(flame);
            flame.ignite();
        }

        SoundManager.play(SoundManager.FIRE_IGNITE);
    }

    private void setUpKeyboard() {
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (Character.isLetter(c)) {
                    currentInput.append(Character.toUpperCase(c));
                    repaint();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    repaint();
                }

                if (code == KeyEvent.VK_ENTER) {
                    handleSubmit();
                }
            }
        });
    }

    private void handleSubmit() {
        if (model.submitWord(currentInput.toString())) {
            feedbackMsg = "Fire extinguished!";
            extinguishFire();

            if (fireCounter == NUMBER_OF_FIRES) {
                speakWord("Congradulations. You did it.");
                SoundManager.play(SoundManager.FIRE_END_GAME);

            } else {
                model.pickNextWord();

                speakWord("The word on the window is " + model.getCurrentWord());
            }

        } else {
            feedbackMsg = "Try again!";
            SoundManager.play(SoundManager.FIRE_ERROR);

            if (model.getWrongCount() > REIGNITE_LIMIT && fireCounter > 0) {
                reigniteFire();
                model.resetWrongCount();
            }
        }
        currentInput.setLength(0);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // ── 1. Background ─────────────────────────────────────────────
        paintBackground(g2, W, H);

        // ── 2. Flames ────────────────────────────────────────────────
        paintFlames(g2, W, H);

        // ── 3. Hero ───────────────────────────────────────────────────
        paintHero(g2, W, H);

        // ── 4. Fire Truck ─────────────────────────────────────────────
        paintFireTruck(g2, W, H);

        // ── 5. Fire counter ──────────────────────────────────────────
        paintFireCounter(g2, W, H);

        paintInputBox(g2, W, H);

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

    // Paint Flames
    private void paintFlames(Graphics2D g2, int W, int H) {
        int flameWidth = 64, flameHeight = 64;
        for (int i = 0; i < flames.size(); i++) {
            int x = (i % 3) * (flameWidth + 10) + 100; // 3 columns
            int y = (i / 3) * (flameHeight + 10) + 100; // 2 rows
            flames.get(i).setBounds(x, y, flameWidth, flameHeight);
            flames.get(i).paint(g2);
        }
    }

    // Paint Hero
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
                srcX1, srcY1 + 70, srcX2, srcY2, null);
    }

    // Paint Fire Truck
    private void paintFireTruck(Graphics2D g2, int W, int H) {
        if (imgFireTruck == null) {
            return;
        }

        int truckWidth = 200;
        int truckHeight = 100;
        int truckX = 10;
        int truckY = H - truckHeight - 10;

        g2.drawImage(imgFireTruck, truckX, truckY, truckWidth, truckHeight, null);
    }

    // Paint fire counter (e.g., 0/6 fires completed)
    private void paintFireCounter(Graphics2D g2, int W, int H) {
        String counterText = fireCounter + "/6 fires completed";

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(counterText);
        int textX = (W - textWidth) / 2;
        int textY = H - 40;

        g2.drawString(counterText, textX, textY);
    }

    // Paint input box
    private void paintInputBox(Graphics2D g2, int W, int H) {
        int boxW = 300;
        int boxH = 60;

        int x = (W - boxW) / 2;
        int y = H - 100;

        // Background box
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x, y, boxW, boxH, 10, 10);

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, boxW, boxH, 10, 10);

        // Input text
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2.setColor(Color.WHITE);

        String text = currentInput.toString();

        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (boxW - fm.stringWidth(text)) / 2;
        int textY = y + boxH / 2 + fm.getAscent() / 2 - 4;

        g2.drawString(text, textX, textY);

        // Optional feedback message
        if (!feedbackMsg.isEmpty()) {
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.setColor(Color.YELLOW);

            int fw = fm.stringWidth(feedbackMsg);
            g2.drawString(feedbackMsg, (W - fw) / 2, y - 10);
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
