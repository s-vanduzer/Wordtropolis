package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

/**
 * Pick Hero page – displays hero images with card-style selection. Shows three
 * hero cards with floating animation and selection highlight.
 */
public class PickHeroPanel extends JPanel {

    private static final String[] HERO_LABELS = {"Mia", "Alex", "Zyx"};
    private static final String[] HERO_IMAGES = {
        "/images/general/girlhero.png", // Mia - girl hero
        "/images/general/boyhero.png", // Alex - boy hero
        "/images/general/alien.png" // Zyx - alien
    };
    private static final Color[] HERO_COLORS = {
        new Color(0xFF6B9D), // Pink for Mia
        new Color(0x4A90D9), // Blue for Alex
        new Color(0x06D6A0) // Green for Zyx
    };

    private int selectedHero = 0;
    private JLabel confirmLabel;
    private JTextField nameField;
    private JLabel[] heroLabels = new JLabel[3];
    private int floatY = 0;
    private int floatDir = 1;
    private int[] xPositions = {120, 360, 600};

    public PickHeroPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(920, 700));
        setBackground(Color.BLACK);

        // BG
        Image bg = new ImageIcon(getClass().getResource("/images/general/BG.png")).getImage();
        Image scaledBg = bg.getScaledInstance(920, 700, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(scaledBg));
        background.setBounds(0, 0, 920, 700);
        background.setLayout(null);
        add(background);

        Image titleImg = new ImageIcon(getClass().getResource("/images/general/wordtropiatitle2.png")).getImage();
        Image scaledTitle = titleImg.getScaledInstance(600, -1, Image.SCALE_SMOOTH);
        JLabel gameTitle = new JLabel(new ImageIcon(scaledTitle));
        gameTitle.setBounds((920 - 600) / 2, 10, 600, 200);
        background.add(gameTitle);

        JLabel title = new JLabel("Choose Your Hero!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String text = getText();
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(new Color(0x0F, 0x4D, 0x58));
                g2d.setStroke(new BasicStroke(3));

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }

                g2d.setColor(getForeground());
                g2d.drawString(text, x, y);

                g2d.dispose();
            }
        };
        title.setFont(loadGameFont(28));
        title.setForeground(new Color(0xEC, 0xCB, 0x2D));
        title.setBounds(0, 185, 920, 50);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        background.add(title);

        for (int i = 0; i < HERO_LABELS.length; i++) {
            final int idx = i;
            JPanel card = createHeroCard(i);
            card.setBounds(xPositions[i], 270, 180, 220);
            card.addMouseListener(new CardMouseAdapter(card, idx));
            background.add(card);

            heroLabels[i] = new JLabel();
            heroLabels[i].setBounds(xPositions[i], 270, 180, 220);
        }

        Timer floatTimer = new Timer(60, e -> {
            floatY += floatDir;
            if (floatY > 6 || floatY < -6) {
                floatDir *= -1;
            }

            for (int i = 0; i < heroLabels.length; i++) {
                Component[] components = background.getComponents();
                for (Component comp : components) {
                    if (comp.getBounds().x == xPositions[i] && comp.getBounds().y == 270 + floatY) {
                        comp.setLocation(xPositions[i], 270 + floatY);
                    }
                }
            }
        });
        floatTimer.start();

        nameField = new JTextField("Enter Name Here...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0x2C2C2C));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2d.setColor(new Color(0x0F, 0x4D, 0x58));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));

                super.paintComponent(g);
                g2d.dispose();
            }
        };

        nameField.setFont(loadGameFont(18));
        nameField.setForeground(Color.WHITE);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        nameField.setCaretColor(new Color(0xEC, 0xCB, 0x2D));
        nameField.setBounds(260, 520, 400, 50);

        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Check if the character is a letter (A-Z or a-z)
                if (Character.isLetter(e.getKeyChar())) {
                    // Only play if we aren't currently showing the placeholder text
                    if (!nameField.getText().equals("Enter Name Here...")) {
                        SoundManager.play(SoundManager.GAME_KEY_CLICK);
                    }
                }
            }
        });

        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (nameField.getText().equals("Enter Name Here...")) {
                    nameField.setText("");
                    nameField.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("Enter Name Here...");
                    nameField.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        background.add(nameField);

        confirmLabel = new JLabel("Selected: " + HERO_LABELS[0]);
        confirmLabel.setFont(loadGameFont(14));
        confirmLabel.setForeground(new Color(0xEC, 0xCB, 0x2D));
        confirmLabel.setBounds(0, 485, 920, 30);
        confirmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        background.add(confirmLabel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBounds(0, 590, 920, 80);

        JButton backBtn = new JButton("Back") {
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
        backBtn.setFont(loadGameFont(18));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(0x4A4A4A)); // Dark gray
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setOpaque(false);
        backBtn.setPreferredSize(new Dimension(150, 45));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(0x6A6A6A));
                backBtn.repaint();
            }

            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(new Color(0x4A4A4A));
                backBtn.repaint();
            }
        });

        backBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            Game.getInstance().setAvatarPath(null);
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });

        // Start Adventure button - yellow with rounded corners, WHITE text
        JButton startBtn = new JButton("Start Adventure") {
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

            @Override
            protected void paintBorder(Graphics g) {

            }
        };
        startBtn.setFont(loadGameFont(18));
        startBtn.setForeground(Color.WHITE); // Changed to WHITE
        startBtn.setBackground(new Color(0xECCB2D)); // Yellow/gold color
        startBtn.setFocusPainted(false);
        startBtn.setBorderPainted(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setOpaque(false);
        startBtn.setPreferredSize(new Dimension(200, 45));
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        startBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                startBtn.setBackground(new Color(0xFFD700));
                startBtn.repaint();
            }

            public void mouseExited(MouseEvent e) {
                startBtn.setBackground(new Color(0xECCB2D));
                startBtn.repaint();
            }
        });

        startBtn.addActionListener(e -> {
            String heroName = nameField.getText().trim();
            if (heroName.isEmpty() || heroName.equals("Enter Name Here...")) {
                heroName = HERO_LABELS[selectedHero];
            }
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            Game.getInstance().setAvatarPath(HERO_LABELS[selectedHero]);
            Game.getInstance().setPlayerName(heroName);
            Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
        });

        bottomPanel.add(backBtn);
        bottomPanel.add(startBtn);
        background.add(bottomPanel);

        selectHero(0, false);
    }

    private JPanel createHeroCard(int idx) {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(HERO_COLORS[idx]);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                if (selectedHero == idx) {
                    g2d.setColor(new Color(0xEC, 0xCB, 0x2D));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 18, 18));
                }

                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //the heros images
        Image img = new ImageIcon(getClass().getResource(HERO_IMAGES[idx])).getImage();
        Image scaledImg = img.getScaledInstance(120, 135, Image.SCALE_SMOOTH);
        JLabel heroImage = new JLabel(new ImageIcon(scaledImg));
        heroImage.setBounds(30, 20, 120, 120);
        card.add(heroImage);

        // the heroes names
        JLabel nameLabel = new JLabel(HERO_LABELS[idx]);
        nameLabel.setFont(loadGameFont(18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(0, 150, 180, 30);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(nameLabel);

        // the heroes description
        String[] desc = {"Brave Warrior", "Wise Mage", "Swift Rogue"};
        JLabel descLabel = new JLabel(desc[idx]);
        descLabel.setFont(loadGameFont(12));
        descLabel.setForeground(new Color(0xDDDDDD));
        descLabel.setBounds(0, 180, 180, 25);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(descLabel);

        return card;
    }

    private class CardMouseAdapter extends MouseAdapter {

        private JPanel card;
        private int index;

        public CardMouseAdapter(JPanel card, int index) {
            this.card = card;
            this.index = index;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            card.setSize(190, 230);
            card.setLocation(card.getX() - 5, card.getY() - 5);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            card.setSize(180, 220);
            card.setLocation(card.getX() + 5, card.getY() + 5);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            selectHero(index, true);
        }
    }

    private void selectHero(int index, boolean playSound) {
        if (playSound) {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        }
        selectedHero = index;
        confirmLabel.setText("Selected: " + HERO_LABELS[index]);

        Container parent = getParent();
        if (parent != null) {
            parent.repaint();
        }
        repaint();
    }

    private Font loadGameFont(int size) {
        try {
            Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf")
            );
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            return new Font("Arial", Font.PLAIN, size);
        }
    }
}
