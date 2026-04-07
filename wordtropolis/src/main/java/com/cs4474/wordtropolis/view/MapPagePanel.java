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
    private static final boolean LINEAR_PROGRESSION_ENABLED = true;
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
        "/images/icons/CatTreeicon.png",
        "/images/icons/Burglaricon.png",
        "/images/icons/Bridgeicon.png",
        "/images/icons/Fireicon.png"
    };

    private static final int[][] BUTTON_POSITIONS = {
        {150, 250}, // Cat in Tree
        {200, 450}, // Burglar Chase
        {550, 200}, // Fix the Bridge
        {700, 450}, // Fire Rescue
        {730, 80} // Final Boss
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
    private Image headerImage;

    public MapPagePanel() {
        loadImages();
        setLayout(null);
        buildUI();
        setupTooltips();
    }

    private void loadImages() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/backgrounds/map_backgroud.png")).getImage();
            checkImage = new ImageIcon(getClass().getResource("/images/ui/check.png")).getImage();
            bossIcon = new ImageIcon(getClass().getResource("/images/icons/FinalBossicon.png")).getImage();
            headerImage = new ImageIcon(getClass().getResource("/images/ui/header.png")).getImage();

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
            // Make the image 10 pixels bigger and move UP by 20 pixels
            int newWidth = getWidth() + 10;
            int newHeight = getHeight() + 10;
            int x = -5;
            int y = -25;  // Moved up 20 pixels (from -5 to -25)
            g.drawImage(backgroundImage, x, y, newWidth, newHeight, this);
        }
    }

    private void buildUI() {
        // 1. Create the main header container
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Increased height to 220 to ensure the infoBox has plenty of vertical room
        header.setBounds(0, 0, 920, 220);
        header.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));

        // 2. Create a vertical container for Title + Player Info
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        // --- TITLE ---
        JLabel title = new JLabel("City Map – Choose a Mission") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);

                // 1. Draw the header image to fill the label's width
                if (headerImage != null) {
                    g2d.drawImage(headerImage, 0, 0, getWidth(), getHeight(), this);
                }

                // 2. Center the text perfectly within the label bounds
                // We subtract fm.getDescent() to center based on visual height rather than baseline
                int x = (getWidth() - textWidth) / 2;
                int yOffset = 9; 
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - yOffset;

                // 3. Draw Shadow/Outline
                g2d.setColor(new Color(0x0F4D58));
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }

                // 4. Draw Main Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };

        title.setFont(UITheme.FONT_TITLE);

        // DYNAMIC SIZE CALCULATION
        FontMetrics titleFm = title.getFontMetrics(UITheme.FONT_TITLE);
        int textWidth = titleFm.stringWidth(title.getText());

        // Increased horizontal buffer to 120 for better edge clearance
        int horizontalBuffer = 120;
        int headerHeight = 75;

        Dimension titleSize = new Dimension(textWidth + horizontalBuffer, headerHeight);
        title.setPreferredSize(titleSize);
        title.setMinimumSize(titleSize);
        title.setMaximumSize(titleSize);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftColumn.add(title);

        // --- SPACE BETWEEN TITLE AND INFO BOX ---
        leftColumn.add(Box.createVerticalStrut(15));

        // --- INFO BOX ---
        // We add a preferred size here to prevent BoxLayout from squishing it
        JPanel infoBox = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 77, 88, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        infoBox.setOpaque(false);
        // Reduced top/bottom padding slightly to save vertical space if needed
        infoBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        playerLabel = createShadowLabel("Hero: —", UITheme.FONT_BODY, UITheme.TEXT_BRIGHT);
        scoreLabel = createShadowLabel("Score: 0", UITheme.FONT_BODY, Color.WHITE);

        infoBox.add(playerLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        infoBox.add(scoreLabel, gbc);

        // Wrap the info box in a panel that doesn't restrict its height
        JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoWrapper.setOpaque(false);
        infoWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoWrapper.add(infoBox);

        leftColumn.add(infoWrapper);
        header.add(leftColumn, BorderLayout.WEST);
        add(header);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            actButtons[i] = createCircularButton(idx, 80);
            actButtons[i].setBounds(BUTTON_POSITIONS[i][0], BUTTON_POSITIONS[i][1], 80, 80);

            updateButtonTooltip(idx);

            // SIMPLIFIED: Just call the registered screen
            actButtons[i].addActionListener(e -> {
                SoundManager.play(SoundManager.GAME_BTN_CLICK);
                if (isLevelUnlocked(idx)) {
                    Wordtropolis.showScreen(ACT_SCREENS[idx]);
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
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            if (isBossUnlocked()) {
                Wordtropolis.showScreen(Wordtropolis.SCREEN_BOSS_GAME);
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
        backBtn.setToolTipText("Return to Hero Selection (progress saved)");
        backBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            Wordtropolis.showScreen(Wordtropolis.SCREEN_HERO_PICK);
        });

        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                backBtn.setBackground(new Color(0x6A6A6A));
                backBtn.repaint();
            }

            @Override
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

    private JLabel createShadowLabel(String text, Font font, Color textColor) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int y = fm.getAscent();

                g2d.setColor(new Color(0x0F4D58));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(getText(), dx, y + dy);
                        }
                    }
                }
                g2d.setColor(textColor);
                g2d.drawString(getText(), 0, y);
                g2d.dispose();
            }
        };
        label.setFont(font);
        return label;
    }

    private void updateButtonTooltip(int idx) {
        if (actButtons[idx] == null) {
            return;
        }

        if (!isLevelUnlocked(idx)) {
            switch (idx) {
                case 1 ->
                    actButtons[idx].setToolTipText("🔒 Complete Cat in Tree to unlock!");
                case 2 ->
                    actButtons[idx].setToolTipText("🔒 Complete Burglar Chase to unlock!");
                case 3 ->
                    actButtons[idx].setToolTipText("🔒 Complete Fix the Bridge to unlock!");
                default ->
                    actButtons[idx].setToolTipText(null);
            }
        } else {
            actButtons[idx].setToolTipText(null);
        }
    }

    private void updateBossTooltip() {
        if (bossBtn == null) {
            return;
        }

        if (!isBossUnlocked()) {
            bossBtn.setToolTipText("🔒 Complete all 4 missions to unlock the Final Boss!");
        } else {
            bossBtn.setToolTipText("Final Boss - Ready to Fight!");
        }
    }

    private String getUnlockMessage(int idx) {
        return switch (idx) {
            case 1 ->
                "Complete the Cat in Tree mission first!";
            case 2 ->
                "Complete the Burglar Chase mission first!";
            case 3 ->
                "Complete the Fix the Bridge mission first!";
            default ->
                "Complete the previous mission first!";
        };
    }

    private boolean isLevelUnlocked(int idx) {

        if (!LINEAR_PROGRESSION_ENABLED) {
            return true;
        }

        Game g = Game.getInstance();
        return switch (idx) {
            case 0 ->
                true;
            case 1 ->
                g.isCatCompleted();
            case 2 ->
                g.isBurgularCompleted();
            case 3 ->
                g.isBridgeCompleted();
            default ->
                false;
        }; // Cat always unlocked
        // Burglar unlocks after Cat
        // Bridge unlocks after Burglar
        // Fire unlocks after Bridge
    }

    private boolean isBossUnlocked() {
        // if linear progression is disabled, boss is always unlocked
        if (!LINEAR_PROGRESSION_ENABLED) {
            return true;
        }

        // og linear progression code
        Game g = Game.getInstance();
        return g.isCatCompleted() && g.isBurgularCompleted()
                && g.isBridgeCompleted() && g.isFireCompleted();
    }

    private JButton createCircularButton(int idx, int size) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isUnlocked = isLevelUnlocked(idx);
                boolean isCompleted = isActivityDone(idx);

                if (isCompleted) {
                    g2.setColor(new Color(0x4CAF50));
                } else if (isUnlocked) {
                    g2.setColor(new Color(0xECCB2D));
                } else {
                    g2.setColor(new Color(0x4A4A4A));
                }
                g2.fill(new Ellipse2D.Float(0, 0, size, size));

                g2.setColor(new Color(0x0F4D58));
                g2.setStroke(new BasicStroke(3));
                g2.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));

                if (iconImages[idx] != null) {
                    int iconSize = 40;
                    int iconX = (size - iconSize) / 2;
                    int iconY = (size - iconSize) / 2;
                    g2.drawImage(iconImages[idx], iconX, iconY, iconSize, iconSize, this);
                }

                if (isCompleted && checkImage != null) {
                    int checkSize = 30;
                    int checkX = (size - checkSize) / 2;
                    int checkY = (size - checkSize) / 2;
                    g2.drawImage(checkImage, checkX, checkY, checkSize, checkSize, this);
                }

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
                    if (originalY == -1) {
                        originalY = btn.getY();
                    }
                    btn.setLocation(btn.getX(), originalY - 8);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (originalY != -1) {
                    btn.setLocation(btn.getX(), originalY);
                }
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
                    if (originalY == -1) {
                        originalY = btn.getY();
                    }
                    btn.setLocation(btn.getX(), originalY - 8);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (originalY != -1) {
                    btn.setLocation(btn.getX(), originalY);
                }
            }
        });

        return btn;
    }

    private boolean isActivityDone(int idx) {
        Game g = Game.getInstance();
        return switch (idx) {
            case 0 ->
                g.isCatCompleted();
            case 1 ->
                g.isBurgularCompleted();
            case 2 ->
                g.isBridgeCompleted();
            case 3 ->
                g.isFireCompleted();
            default ->
                false;
        };
    }

    @Override
    public void refresh() {
        SoundManager.stopAll();
        Game g = Game.getInstance();
        if (playerLabel != null) {
            playerLabel.setText("Hero: " + g.getPlayerName());
        }
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + g.getScore());
        }

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

    @Override
    public void grabFocus() {
        // We use invokeLater to ensure the component is fully 
        // realized in the UI tree before requesting focus
        SwingUtilities.invokeLater(() -> {
            this.setFocusable(true);
            this.requestFocusInWindow();
        });
    }
}
