package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class StartPagePanel extends JPanel {

    private int baseTitleY = 120;
    private double time = 0;
    private JLabel titleLabel;

    public StartPagePanel() {
        setLayout(null);
        setPreferredSize(new Dimension(920, 700));

        // Background
        Image bg = new ImageIcon(getClass().getResource("/images/backgrounds/menu_background.png")).getImage();
        Image scaledBg = bg.getScaledInstance(920, 700, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(scaledBg));
        background.setBounds(0, 0, 920, 700);
        background.setLayout(null);
        add(background);

        // Clouds
        Image cloudRaw = new ImageIcon(getClass().getResource("/images/sprites/cloud.png")).getImage();
        Image cloudScaled = cloudRaw.getScaledInstance(360, 190, Image.SCALE_SMOOTH);
        ImageIcon cloudImg = new ImageIcon(cloudScaled);

        JLabel cloudA = new JLabel(cloudImg);
        cloudA.setBounds(-360, 10, 360, 190);
        background.add(cloudA);

        JLabel cloudB = new JLabel(cloudImg);
        cloudB.setBounds(920, 140, 360, 190);
        background.add(cloudB);

        // Title
        Image title = new ImageIcon(getClass().getResource("/images/sprites/wordtroplis_title.png")).getImage();
        Image scaledTitle = title.getScaledInstance(700, -1, Image.SCALE_SMOOTH);
        titleLabel = new JLabel(new ImageIcon(scaledTitle));
        int titleWidth = scaledTitle.getWidth(null);
        int titleHeight = scaledTitle.getHeight(null);
        int titleX = (920 - titleWidth) / 2;
        titleLabel.setBounds(titleX, baseTitleY, titleWidth, titleHeight);
        background.add(titleLabel);

        // --- UPDATED ROUNDED, TRANSPARENT DARK BLUE BOX ---
        int panelWidth = 500;
        int panelHeight = 180;
        int panelX = (920 - panelWidth) / 2;
        int panelY = 360;

        JPanel buttonPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Color #052957 with Alpha (200/255 for slight transparency)
                g2d.setColor(new Color(5, 41, 87, 200));

                // Draw the rounded rectangle
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Optional: add a border/outline in the same color (fully opaque)
                g2d.setColor(new Color(5, 41, 87));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);

                g2d.dispose();
            }
        };
        buttonPanel.setOpaque(false); // Must be false to see the rounded corners properly
        buttonPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        background.add(buttonPanel);

        // Buttons
        JButton teacherBtn = createStyledButton("I am a Teacher");
        JButton studentBtn = createStyledButton("I am a Student");

        teacherBtn.setBounds(50, 25, 400, 60);
        studentBtn.setBounds(50, 95, 400, 60);

        teacherBtn.setFont(loadGameFont(24));
        studentBtn.setFont(loadGameFont(24));

        Color gold = new Color(0xEC, 0xCB, 0x2D);
        teacherBtn.setForeground(gold);
        studentBtn.setForeground(gold);

        buttonPanel.add(teacherBtn);
        buttonPanel.add(studentBtn);

        // Triangle selector
        Image triangleRaw = new ImageIcon(getClass().getResource("/images/ui/triangle.png")).getImage();
        Image triangleScaled = triangleRaw.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        ImageIcon triangleIcon = new ImageIcon(triangleScaled);
        JLabel selector = new JLabel(triangleIcon);
        selector.setVisible(false);
        buttonPanel.add(selector);

        // Hover Logic
        teacherBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selector.setBounds(100, 42, 28, 28);
                selector.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selector.setVisible(false);
            }
        });

        studentBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selector.setBounds(100, 112, 28, 28);
                selector.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selector.setVisible(false);
            }
        });

        // Navigation & Timers
        teacherBtn.addActionListener(e -> {
            Wordtropolis.showScreen(Wordtropolis.SCREEN_TEACHER);
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        });
        studentBtn.addActionListener(e -> {
            Wordtropolis.showScreen(Wordtropolis.SCREEN_HERO_PICK);
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        });

        new Timer(60, e -> floatTitle()).start();
        new Timer(30, e -> {
            cloudA.setLocation(cloudA.getX() > 920 ? -360 : cloudA.getX() + 2, cloudA.getY());
            cloudB.setLocation(cloudB.getX() < -360 ? 920 : cloudB.getX() - 2, cloudB.getY());
        }).start();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                // Text Stroke
                g2d.setColor(new Color(5, 41, 87));
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i != 0 || j != 0) {
                            g2d.drawString(getText(), x + i, y + j);
                        }
                    }
                }
                g2d.setColor(getForeground());
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private void floatTitle() {
        time += 0.15;
        titleLabel.setLocation(titleLabel.getX(), baseTitleY + (int) (Math.sin(time) * 10));
    }

    private Font loadGameFont(int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf")).deriveFont(Font.PLAIN, size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, size);
        }
    }
}
