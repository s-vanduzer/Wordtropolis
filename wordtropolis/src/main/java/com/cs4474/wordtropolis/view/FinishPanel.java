package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Finish screen – shown after the Final Boss is defeated.
 * Offers replay (back to start) or exit.
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
            java.net.URL bgUrl = getClass().getResource("/images/general/BG.png");
            if (bgUrl != null) {
                ImageIcon icon = new ImageIcon(bgUrl);
                backgroundImage = icon.getImage();
            }
            
            java.net.URL trophyUrl = getClass().getResource("/images/general/trophy.png");
            if (trophyUrl != null) {
                ImageIcon icon = new ImageIcon(trophyUrl);
                trophyImage = icon.getImage();
            }
            
            java.net.URL cloudUrl = getClass().getResource("/images/general/cloud.png");
            if (cloudUrl != null) {
                ImageIcon icon = new ImageIcon(cloudUrl);
                cloudImage = icon.getImage();
            }
        } catch (Exception e) {
            System.out.println("Could not load images: " + e.getMessage());
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

    private void buildUI() {
        int W = 920, H = 700;

        // ── Background container ───────────────────────────────────────────────
        JLabel background = new JLabel();
        background.setBounds(0, 0, W, H);
        background.setLayout(null);
        add(background);

        // ── Clouds (same as StartPagePanel) ────────────────────────────────────
        if (cloudImage != null) {
            Image cloudScaled = cloudImage.getScaledInstance(360, 190, Image.SCALE_SMOOTH);
            ImageIcon cloudImg = new ImageIcon(cloudScaled);

            cloudA = new JLabel(cloudImg);
            cloudA.setBounds(-360, 10, 360, 190);
            background.add(cloudA);

            cloudB = new JLabel(cloudImg);
            cloudB.setBounds(920, 140, 360, 190);
            background.add(cloudB);
        }

        // ── Wordtropolis Title Image (big and centered at the top) ─────────────
        titleLabel = new JLabel();
        try {
            ImageIcon titleIcon = new ImageIcon(getClass().getResource("/images/general/wordtropiatitle2.png"));
            Image scaledImg = titleIcon.getImage().getScaledInstance(600, 120, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            titleLabel.setText("WORDTROPOLIS");
            titleLabel.setFont(UITheme.FONT_TITLE);
            titleLabel.setForeground(UITheme.ACCENT_YELLOW);
        }
        int titleWidth = 600;
        int titleHeight = 120;
        int titleX = (W - titleWidth) / 2;
        titleLabel.setBounds(titleX, 40, titleWidth, titleHeight);
        background.add(titleLabel);

        // ── Trophy Image (bigger) ──────────────────────────────────────────────
        JLabel trophyLabel = new JLabel();
        if (trophyImage != null) {
            Image scaledTrophy = trophyImage.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            trophyLabel.setIcon(new ImageIcon(scaledTrophy));
        } else {
            trophyLabel.setText("🏆");
            trophyLabel.setFont(new Font("Segoe UI", Font.BOLD, 100));
            trophyLabel.setForeground(UITheme.ACCENT_YELLOW);
        }
        int trophyWidth = 160;
        int trophyHeight = 160;
        int trophyX = (W - trophyWidth) / 2;
        trophyLabel.setBounds(trophyX, 200, trophyWidth, trophyHeight);
        background.add(trophyLabel);

        // ── Game Over Text (yellow with stroke) ────────────────────────────────
        JLabel gameOverLabel = new JLabel("GAME OVER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(3));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        gameOverLabel.setFont(UITheme.FONT_TITLE);
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameOverLabel.setBounds(0, 370, W, 40);
        background.add(gameOverLabel);

        // ── Victory Message (yellow with stroke) ───────────────────────────────
        JLabel messageLabel = new JLabel("You are a true Spelling Hero!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(3));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        messageLabel.setFont(UITheme.FONT_TITLE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(0, 420, W, 40);
        background.add(messageLabel);

        // ── User Name Label ────────────────────────────────────────────────────
        userNameLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = getText();
                if (text == null || text.isEmpty()) {
                    g2d.dispose();
                    return;
                }
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(2));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        userNameLabel.setFont(UITheme.FONT_HEADING);
        userNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userNameLabel.setBounds(0, 470, W, 30);
        background.add(userNameLabel);

        // ── Score Label ────────────────────────────────────────────────────────
        scoreLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(2));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        scoreLabel.setFont(UITheme.FONT_HEADING);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setBounds(0, 510, W, 30);
        background.add(scoreLabel);

        // ── Buttons (Yellow background, white text, at the bottom) ──────────────
        // Exit Button
        JButton exitBtn = new JButton("Exit");
        exitBtn.setFont(UITheme.FONT_BUTTON);
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(0xECCB2D));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorder(BorderFactory.createLineBorder(new Color(0x0F4D58), 2));
        exitBtn.setBounds(300, 580, 130, 50);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> System.exit(0));
        
        exitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitBtn.setBackground(new Color(0xFFD700));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitBtn.setBackground(new Color(0xECCB2D));
            }
        });

        // Play Again Button
        JButton replayBtn = new JButton("Play Again");
        replayBtn.setFont(UITheme.FONT_BUTTON);
        replayBtn.setForeground(Color.WHITE);
        replayBtn.setBackground(new Color(0xECCB2D));
        replayBtn.setFocusPainted(false);
        replayBtn.setBorder(BorderFactory.createLineBorder(new Color(0x0F4D58), 2));
        replayBtn.setBounds(490, 580, 170, 50);
        replayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        replayBtn.addActionListener(e -> {
            Game.resetInstance();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });
        
        replayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                replayBtn.setBackground(new Color(0xFFD700));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                replayBtn.setBackground(new Color(0xECCB2D));
            }
        });

        background.add(exitBtn);
        background.add(replayBtn);
    }

    private void startCloudAnimation() {
        if (cloudA != null && cloudB != null) {
            cloudTimer = new Timer(30, e -> {
                int ax = cloudA.getX() + 2;
                if (ax > 920) ax = -360;
                cloudA.setLocation(ax, cloudA.getY());

                int bx = cloudB.getX() - 2;
                if (bx < -360) bx = 920;
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
            if (x > 920) x = -size;
            if (x < -size) x = 920;
            if (y > 700) y = -size;
            if (y < -size) y = 700;
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
        
        if (scoreLabel != null) scoreLabel.setText("Final Score: " + g.getScore());
    }
}