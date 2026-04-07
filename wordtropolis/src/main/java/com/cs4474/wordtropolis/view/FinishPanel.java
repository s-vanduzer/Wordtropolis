package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * Finish screen – shown after the Final Boss is defeated. Offers replay (back
 * to start) or exit.
 */
public class FinishPanel extends JPanel implements Refreshable {

    private JLabel scoreLabel;
    private JLabel userNameLabel;
    private Image backgroundImage;
    private Image trophyImage;
    private Image cloudImage;
    private JLabel cloudA;
    private JLabel cloudB;
    private Timer cloudTimer;
    private JLabel titleLabel;

    // Confetti variables
    private ArrayList<Confetti> confettiList;
    private Timer confettiTimer;
    private Random random;

    public FinishPanel() {
        loadImages();
        setLayout(null);
        setPreferredSize(new Dimension(920, 700));
        buildUI();
        startCloudAnimation();
        startConfetti();
    }

    private void loadImages() {
        try {
            java.net.URL bgUrl = getClass().getResource("/images/backgrounds/menu_background.png");
            if (bgUrl != null) {
                ImageIcon icon = new ImageIcon(bgUrl);
                backgroundImage = icon.getImage();
            }

            java.net.URL trophyUrl = getClass().getResource("/images/sprites/trophy.png");
            if (trophyUrl != null) {
                ImageIcon icon = new ImageIcon(trophyUrl);
                trophyImage = icon.getImage();
            }

            java.net.URL cloudUrl = getClass().getResource("/images/sprites/cloud.png");
            if (cloudUrl != null) {
                ImageIcon icon = new ImageIcon(cloudUrl);
                cloudImage = icon.getImage();
            }
        } catch (Exception e) {
            System.out.println("[Final] Could not load images: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(UITheme.BG_DARK);
        }

        // Draw confetti
        if (confettiList != null) {
            for (Confetti c : confettiList) {
                c.draw(g);
            }
        }
    }

    private JButton createRoundedButton(String text, Color bgColor, Color hoverColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded background
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                // Draw border
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));

                // Draw text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String btnText = getText();
                int textWidth = fm.stringWidth(btnText);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(getForeground());
                g2d.drawString(btnText, x, y);

                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Empty
            }
        };

        button.setFont(UITheme.FONT_BUTTON);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.repaint();
            }
        });

        return button;
    }

    private void buildUI() {
        int W = 920, H = 700;

        // ── Background container ───────────────────────────────────────────────
        JLabel background = new JLabel();
        background.setBounds(0, 0, W, H);
        background.setLayout(null);
        add(background);

        // ── Clouds (Added FIRST so they are at the back) ──────────────────────
        if (cloudImage != null) {
            Image cloudScaled = cloudImage.getScaledInstance(360, 190, Image.SCALE_SMOOTH);
            ImageIcon cloudImg = new ImageIcon(cloudScaled);
            
            cloudA = new JLabel(cloudImg);
            cloudA.setBounds(-360, 10, 360, 190);
            background.add(cloudA); // Added first = lowest Z-index
            
            cloudB = new JLabel(cloudImg);
            cloudB.setBounds(920, 140, 360, 190);
            background.add(cloudB);
        }

        // ── Wordtropolis Title Image ──────────────────────────────────────────
        titleLabel = new JLabel();
        try {
            ImageIcon titleIcon = new ImageIcon(getClass().getResource("/images/sprites/wordtroplis_title.png"));
            Image scaledImg = titleIcon.getImage().getScaledInstance(600, 120, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            titleLabel.setText("WORDTROPOLIS");
            titleLabel.setFont(UITheme.FONT_TITLE);
            titleLabel.setForeground(UITheme.ACCENT_YELLOW);
        }
        titleLabel.setBounds((W - 600) / 2, 30, 600, 120);
        background.add(titleLabel);

        // ── THE COLOURED BOX CONTAINER ────────────────────────────────────────
        int boxW = 650;
        int boxH = 400;
        int boxX = (W - boxW) / 2;
        int boxY = 160;

        JPanel contentBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark Blue Transparent Background
                g2d.setColor(new Color(5, 41, 87, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // Opaque Border
                g2d.setColor(new Color(5, 41, 87));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                g2d.dispose();
            }
        };
        contentBox.setOpaque(false);
        contentBox.setBounds(boxX, boxY, boxW, boxH);
        background.add(contentBox); // Added after clouds = draws over clouds

        // ── Trophy (Inside contentBox) ────────────────────────────────────
        JLabel trophyLabel = new JLabel();
        if (trophyImage != null) {
            Image scaledTrophy = trophyImage.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            trophyLabel.setIcon(new ImageIcon(scaledTrophy));
        } else {
            trophyLabel.setText("🏆");
            trophyLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        }
        trophyLabel.setBounds((boxW - 140) / 2, 20, 140, 140);
        contentBox.add(trophyLabel);

        // ── Labels (Inside contentBox) ────────────────────────────────────────
        JLabel gameOverLabel = createStrokedLabel("GAME OVER", UITheme.FONT_TITLE);
        gameOverLabel.setBounds(0, 170, boxW, 40);
        contentBox.add(gameOverLabel);

        JLabel messageLabel = createStrokedLabel("You are a true Spelling Hero!", UITheme.FONT_TITLE);
        messageLabel.setBounds(0, 220, boxW, 40);
        contentBox.add(messageLabel);

        userNameLabel = createStrokedLabel("", UITheme.FONT_HEADING);
        userNameLabel.setBounds(0, 280, boxW, 30);
        contentBox.add(userNameLabel);

        scoreLabel = createStrokedLabel("Final Score: 0", UITheme.FONT_HEADING);
        scoreLabel.setBounds(0, 320, boxW, 30);
        contentBox.add(scoreLabel);

        // ── Buttons (Outside the box) ─────────────────────────────────────────
        JButton exitBtn = createRoundedButton("Exit", new Color(0x4A4A4A), new Color(0x6A6A6A), Color.WHITE);
        exitBtn.setBounds(300, 580, 130, 50);
        exitBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            System.exit(0);
        });

        JButton replayBtn = createRoundedButton("Play Again", new Color(0xECCB2D), new Color(0xFFD700), Color.WHITE);
        replayBtn.setBounds(490, 580, 170, 50);
        replayBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            SoundManager.stopAll();
            Game.resetInstance();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });

        background.add(exitBtn);
        background.add(replayBtn);
    }

    // Helper to keep the buildUI code clean since all your labels used the same stroke logic
    private JLabel createStrokedLabel(String text, Font font) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String t = getText();
                if (t == null || t.isEmpty()) return;
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(t)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(new Color(0x0F4D58));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) g2d.drawString(t, x + dx, y + dy);
                    }
                }
                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(t, x, y);
                g2d.dispose();
            }
        };
        label.setFont(font);
        return label;
    }

    private void startCloudAnimation() {
        if (cloudA != null && cloudB != null) {
            cloudTimer = new Timer(30, e -> {
                int ax = cloudA.getX() + 2;
                if (ax > 920) {
                    ax = -360;
                }
                cloudA.setLocation(ax, cloudA.getY());

                int bx = cloudB.getX() - 2;
                if (bx < -360) {
                    bx = 920;
                }
                cloudB.setLocation(bx, cloudB.getY());
            });
            cloudTimer.start();
        }
    }

    private void startConfetti() {
        random = new Random();
        confettiList = new ArrayList<>();

        // creates the 100 confettis 
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(920);
            int y = random.nextInt(700);
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            int size = 5 + random.nextInt(8);
            int speedY = 2 + random.nextInt(5);
            int speedX = -2 + random.nextInt(5);
            confettiList.add(new Confetti(x, y, color, size, speedX, speedY));
        }

        confettiTimer = new Timer(30, e -> {
            for (Confetti c : confettiList) {
                c.update();
            }
            repaint();
        });
        confettiTimer.start();
    }

    // confetti 
    private class Confetti {

        int x, y;
        Color color;
        int size;
        int speedX, speedY;

        public Confetti(int x, int y, Color color, int size, int speedX, int speedY) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = size;
            this.speedX = speedX;
            this.speedY = speedY;
        }

        public void update() {
            x += speedX;
            y += speedY;

            // Reset when off screen
            if (x > 920) {
                x = -size;
            }
            if (x < -size) {
                x = 920;
            }
            if (y > 700) {
                y = -size;
            }
            if (y < -size) {
                y = 700;
            }
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(x, y, size, size);
        }
    }

    @Override
    public void refresh() {
        Game g = Game.getInstance();
        String playerName = g.getPlayerName();

        if (playerName != null && !playerName.isEmpty() && !playerName.equals("Enter Name Here...")) {
            userNameLabel.setText(playerName);
        } else {
            userNameLabel.setText("");
        }

        if (scoreLabel != null) {
            scoreLabel.setText("Final Score: " + g.getScore());
        }
    }
}
