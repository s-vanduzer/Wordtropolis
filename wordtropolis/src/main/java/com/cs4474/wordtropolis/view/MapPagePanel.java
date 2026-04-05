package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class MapPagePanel extends JPanel implements Refreshable {

    // ============================================================
    // LINEAR PROGRESSION TOGGLE
    // Set to false for presentation (all levels unlocked)
    // Set to true for submission (linear progression enabled)
    // ============================================================
    private static final boolean LINEAR_PROGRESSION_ENABLED = false;
    // change to true for submission ( linear)
    
    private static final String[] ACT_LABELS = {
        "Cat in Tree", "Burglar Chase", "Fix the Bridge", "Fire Rescue"
    };
    private static final String[] ACT_SCREENS = {
        Wordtropolis.SCREEN_CAT_GAME,
        Wordtropolis.SCREEN_BURGLAR_GAME,
        Wordtropolis.SCREEN_BRIDGE_GAME,
        Wordtropolis.SCREEN_FIRE_GAME
    };
    private static final String[] ACT_ICONS = {
        "/images/general/CatTreeicon.png",
        "/images/general/Burglaricon.png",
        "/images/general/Bridgeicon.png",
        "/images/general/Fireicon.png"
    };
    
    
    private static final int[][] BUTTON_POSITIONS = {
        {150, 250},   // Cat in Tree
        {200, 450},   // Burglar Chase
        {550, 200},   // Fix the Bridge
        {700, 450},   // Fire Rescue
        {730, 80}     // Final Boss
    };

    private JLabel playerLabel;
    private JLabel scoreLabel;
    private JButton bossBtn;
    private JButton backBtn;
    private JButton[] actButtons = new JButton[4];
    private Image backgroundImage;
    private Image[] iconImages = new Image[4];
    private Image checkImage;
    private Image bossIcon;

    public MapPagePanel() {
        loadImages();
        setLayout(null);
        buildUI();
        setupTooltips();
    }

    private void loadImages() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/general/city.png")).getImage();
            checkImage = new ImageIcon(getClass().getResource("/images/bridge_game/check.png")).getImage();
            bossIcon = new ImageIcon(getClass().getResource("/images/general/FinalBossicon.png")).getImage();
            
            for (int i = 0; i < ACT_ICONS.length; i++) {
                iconImages[i] = new ImageIcon(getClass().getResource(ACT_ICONS[i])).getImage();
            }
        } catch (Exception e) {
            System.out.println("Could not load images: " + e);
        }
    }
    
    private void setupTooltips() {
        
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void buildUI() {
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBounds(0, 0, 920, 80);
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 10, 28));

        JLabel title = new JLabel("City Map – Choose a Mission") {
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
                
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        title.setFont(UITheme.FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        
        
        playerLabel = new JLabel("Hero: —") {
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
                
                
                g2d.setColor(UITheme.TEXT_BRIGHT);
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        playerLabel.setFont(UITheme.FONT_BODY);
        playerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        
        scoreLabel = new JLabel("Score: 0") {
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
                
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        scoreLabel.setFont(UITheme.FONT_BODY);
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        info.add(playerLabel);
        info.add(scoreLabel);
        header.add(info, BorderLayout.EAST);
        add(header);

        
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            actButtons[i] = createCircularButton(idx, 80);
            actButtons[i].setBounds(BUTTON_POSITIONS[i][0], BUTTON_POSITIONS[i][1], 80, 80);
            
           
            updateButtonTooltip(idx);
            
            actButtons[i].addActionListener(e -> {
                if (isLevelUnlocked(idx)) {
                    Wordtropolis.replaceScreen(ACT_SCREENS[idx], getGamePanel(idx));
                } else {
                    String message = getUnlockMessage(idx);
                    JOptionPane.showMessageDialog(this, message, "Locked", JOptionPane.WARNING_MESSAGE);
                }
            });
            add(actButtons[i]);
        }

        
        bossBtn = createCircularBossButton(80);
        bossBtn.setBounds(BUTTON_POSITIONS[4][0], BUTTON_POSITIONS[4][1], 80, 80);
        
        
        updateBossTooltip();
        
        bossBtn.addActionListener(e -> {
            if (isBossUnlocked()) {
                Wordtropolis.replaceScreen(Wordtropolis.SCREEN_BOSS_GAME, new BossGamePanel());
            } else {
                JOptionPane.showMessageDialog(this, "Complete all 4 missions to unlock the Final Boss!", "Locked", JOptionPane.WARNING_MESSAGE);
            }
        });
        add(bossBtn);
        
        
        backBtn = new JButton("Back") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(getForeground());
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        backBtn.setText("Back");
        backBtn.setFont(UITheme.FONT_BUTTON);
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(0x4A4A4A));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setOpaque(false);
        backBtn.setBounds(20, 600, 120, 45);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> Wordtropolis.showScreen(Wordtropolis.SCREEN_HERO_PICK));
        backBtn.setToolTipText("Return to Hero Selection (progress saved)");
        
        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                backBtn.setBackground(new Color(0x6A6A6A));
                backBtn.repaint();
            }
            public void mouseExited(MouseEvent evt) {
                backBtn.setBackground(new Color(0x4A4A4A));
                backBtn.repaint();
            }
        });
        
        add(backBtn);
        
        
        JLabel unlockLabel = UITheme.makeLabel("Complete all 4 missions to unlock",
                UITheme.FONT_SMALL, UITheme.TEXT_DIM);
        unlockLabel.setBounds(310, 670, 300, 20);
        add(unlockLabel);
    }
    
    private void updateButtonTooltip(int idx) {
        if (actButtons[idx] == null) return;
        
        if (!isLevelUnlocked(idx)) {
            switch (idx) {
                case 1:
                    actButtons[idx].setToolTipText("🔒 Complete Cat in Tree to unlock!");
                    break;
                case 2:
                    actButtons[idx].setToolTipText("🔒 Complete Burglar Chase to unlock!");
                    break;
                case 3:
                    actButtons[idx].setToolTipText("🔒 Complete Fix the Bridge to unlock!");
                    break;
                default:
                    actButtons[idx].setToolTipText(null);
            }
        } else {
            actButtons[idx].setToolTipText(null);
        }
    }
    
    private void updateBossTooltip() {
        if (bossBtn == null) return;
        
        if (!isBossUnlocked()) {
            bossBtn.setToolTipText("🔒 Complete all 4 missions to unlock the Final Boss!");
        } else {
            bossBtn.setToolTipText("Final Boss - Ready to Fight!");
        }
    }

    private String getUnlockMessage(int idx) {
        switch (idx) {
            case 1: return "Complete the Cat in Tree mission first!";
            case 2: return "Complete the Burglar Chase mission first!";
            case 3: return "Complete the Fix the Bridge mission first!";
            default: return "Complete the previous mission first!";
        }
    }

    private JPanel getGamePanel(int idx) {
        switch (idx) {
            case 0: return new CatGamePanel();
            case 1: return new BurglarGamePanel();
            case 2: return new BrokenBridgeGamePanel();
            case 3: return new FireGamePanel(); // Replace with FireGamePanel when ready
            default: return new CatGamePanel();
        }
    }

    private boolean isLevelUnlocked(int idx) {
        
        if (!LINEAR_PROGRESSION_ENABLED) {
            return true;
        }
        
       
        Game g = Game.getInstance();
        switch (idx) {
            case 0: return true; // Cat always unlocked
            case 1: return g.isCatCompleted(); // Burglar unlocks after Cat
            case 2: return g.isBurgularCompleted(); // Bridge unlocks after Burglar
            case 3: return g.isBridgeCompleted(); // Fire unlocks after Bridge
            default: return false;
        }
    }

    private boolean isBossUnlocked() {
        // If linear progression is disabled, boss is always unlocked
        if (!LINEAR_PROGRESSION_ENABLED) {
            return true;
        }
        
        // Original linear progression code
        Game g = Game.getInstance();
        return g.isCatCompleted() && g.isBurgularCompleted() && 
               g.isBridgeCompleted() && g.isFireCompleted();
    }

    private JButton createCircularButton(int idx, int size) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean isUnlocked = isLevelUnlocked(idx);
                boolean isCompleted = isActivityDone(idx);
                
                // Color logic
                if (isCompleted) {
                    g2.setColor(new Color(0x4CAF50)); 
                } else if (isUnlocked) {
                    g2.setColor(new Color(0xECCB2D)); 
                } else {
                    g2.setColor(new Color(0x4A4A4A)); 
                }
                g2.fill(new Ellipse2D.Float(0, 0, size, size));
                
                // Border
                g2.setColor(new Color(0x0F4D58));
                g2.setStroke(new BasicStroke(3));
                g2.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));
                
                // Icon (40px)
                if (iconImages[idx] != null) {
                    int iconSize = 40;
                    int iconX = (size - iconSize) / 2;
                    int iconY = (size - iconSize) / 2;
                    g2.drawImage(iconImages[idx], iconX, iconY, iconSize, iconSize, this);
                }
                
                // Checkmark in center if completed
                if (isCompleted && checkImage != null) {
                    int checkSize = 30;
                    int checkX = (size - checkSize) / 2;
                    int checkY = (size - checkSize) / 2;
                    g2.drawImage(checkImage, checkX, checkY, checkSize, checkSize, this);
                }
                
                // Dark overlay if locked
                if (!isUnlocked && !isCompleted) {
                    g2.setColor(new Color(0, 0, 0, 180));
                    g2.fill(new Ellipse2D.Float(0, 0, size, size));
                }
                
                g2.dispose();
            }
        };
        
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        
        btn.addMouseListener(new MouseAdapter() {
            private int originalY = -1;
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isLevelUnlocked(idx)) {
                    if (originalY == -1) originalY = btn.getY();
                    btn.setLocation(btn.getX(), originalY - 8);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (originalY != -1) btn.setLocation(btn.getX(), originalY);
            }
        });
        
        return btn;
    }

    private JButton createCircularBossButton(int size) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean isUnlocked = isBossUnlocked();
                
                if (isUnlocked) {
                    g2.setColor(new Color(0xECCB2D));
                } else {
                    g2.setColor(new Color(0x4A4A4A));
                }
                g2.fill(new Ellipse2D.Float(0, 0, size, size));
                
                
                g2.setColor(new Color(0x0F4D58));
                g2.setStroke(new BasicStroke(3));
                g2.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));
                
                
                if (bossIcon != null) {
                    int iconSize = 40;
                    int iconX = (size - iconSize) / 2;
                    int iconY = (size - iconSize) / 2;
                    g2.drawImage(bossIcon, iconX, iconY, iconSize, iconSize, this);
                }
                
               
                if (!isUnlocked) {
                    g2.setColor(new Color(0, 0, 0, 180));
                    g2.fill(new Ellipse2D.Float(0, 0, size, size));
                }
                
                g2.dispose();
            }
        };
        
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            private int originalY = -1;
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isBossUnlocked()) {
                    if (originalY == -1) originalY = btn.getY();
                    btn.setLocation(btn.getX(), originalY - 8);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (originalY != -1) btn.setLocation(btn.getX(), originalY);
            }
        });
        
        return btn;
    }

    private boolean isActivityDone(int idx) {
        Game g = Game.getInstance();
        switch (idx) {
            case 0: return g.isCatCompleted();
            case 1: return g.isBurgularCompleted();
            case 2: return g.isBridgeCompleted();
            case 3: return g.isFireCompleted();
            default: return false;
        }
    }

    @Override
    public void refresh() {
        Game g = Game.getInstance();
        if (playerLabel != null) playerLabel.setText("Hero: " + g.getPlayerName());
        if (scoreLabel != null) scoreLabel.setText("Score: " + g.getScore());
        
        
        for (int i = 0; i < actButtons.length; i++) {
            if (actButtons[i] != null) {
                updateButtonTooltip(i);
                actButtons[i].repaint();
            }
        }
        
        if (bossBtn != null) {
            updateBossTooltip();
            bossBtn.repaint();
        }
    }
}