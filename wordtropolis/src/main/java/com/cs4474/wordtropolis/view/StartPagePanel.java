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
        Image bg = new ImageIcon(getClass().getResource("/images/general/BG.png")).getImage();
        Image scaledBg = bg.getScaledInstance(920, 700, Image.SCALE_SMOOTH);

        JLabel background = new JLabel(new ImageIcon(scaledBg));
        background.setBounds(0, 0, 920, 700);
        background.setLayout(null);
        add(background);

        // Clouds
        Image cloudRaw = new ImageIcon(getClass().getResource("/images/general/cloud.png")).getImage();
        Image cloudScaled = cloudRaw.getScaledInstance(360, 190, Image.SCALE_SMOOTH);
        ImageIcon cloudImg = new ImageIcon(cloudScaled);

        JLabel cloudA = new JLabel(cloudImg);
        cloudA.setBounds(-360, 10, 360, 190);
        background.add(cloudA);

        JLabel cloudB = new JLabel(cloudImg);
        cloudB.setBounds(920, 140, 360, 190);
        background.add(cloudB);

        // Title
        Image title = new ImageIcon(getClass().getResource("/images/general/wordtropiatitle2.png")).getImage();
        Image scaledTitle = title.getScaledInstance(700, -1, Image.SCALE_SMOOTH);

        titleLabel = new JLabel(new ImageIcon(scaledTitle));
        int titleWidth = scaledTitle.getWidth(null);
        int titleHeight = scaledTitle.getHeight(null);
        int titleX = (920 - titleWidth) / 2;

        titleLabel.setBounds(titleX, baseTitleY, titleWidth, titleHeight);
        background.add(titleLabel);

        // Floating title animation
        Timer floatTimer = new Timer(60, e -> floatTitle());
        floatTimer.start();

        // Cloud animation
        Timer cloudTimer = new Timer(30, e -> {
            int ax = cloudA.getX() + 2;
            if (ax > 920) ax = -360;
            cloudA.setLocation(ax, cloudA.getY());

            int bx = cloudB.getX() - 2;
            if (bx < -360) bx = 920;
            cloudB.setLocation(bx, cloudB.getY());
        });
        cloudTimer.start();

        // Button panel
        JPanel buttonPanel = new JPanel(null);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(210, 350, 500, 150);
        background.add(buttonPanel);

        // Create custom buttons with stroke
        JButton teacherBtn = new JButton("I am a Teacher") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw stroke (outline)
                g2d.setColor(new Color(0x0F, 0x4D, 0x58)); // #0F4D58
                g2d.setStroke(new BasicStroke(3));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                // Draw stroke by drawing the text multiple times
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                // Draw the actual text
                g2d.setColor(getForeground());
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        JButton studentBtn = new JButton("I am a Student") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw stroke (outline)
                g2d.setColor(new Color(0x0F, 0x4D, 0x58)); // #0F4D58
                g2d.setStroke(new BasicStroke(3));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                // Draw stroke by drawing the text multiple times
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }
                
                // Draw the actual text
                g2d.setColor(getForeground());
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };

        teacherBtn.setBounds(80, -20, 400, 70);
        studentBtn.setBounds(80, 60, 400, 70);

        teacherBtn.setFont(loadGameFont(24));
        studentBtn.setFont(loadGameFont(24));

        teacherBtn.setContentAreaFilled(false);
        teacherBtn.setBorderPainted(false);
        teacherBtn.setFocusPainted(false);

        studentBtn.setContentAreaFilled(false);
        studentBtn.setBorderPainted(false);
        studentBtn.setFocusPainted(false);

        Color gold = new Color(0xEC, 0xCB, 0x2D);
        teacherBtn.setForeground(gold);
        studentBtn.setForeground(gold);

        buttonPanel.add(teacherBtn);
        buttonPanel.add(studentBtn);

        // Triangle selector - teacher triangle moved up another 5 pixels
        Image triangleRaw = new ImageIcon(
                getClass().getResource("/images/general/triangle.png")
        ).getImage();

        Image triangleScaled = triangleRaw.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        ImageIcon triangleIcon = new ImageIcon(triangleScaled);

        JLabel selector = new JLabel(triangleIcon);
        selector.setBounds(149, 5, 28, 28);
        selector.setVisible(false);
        buttonPanel.add(selector);

        // Hover Teacher
        teacherBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selector.setLocation(149, 0);
                teacherBtn.setLocation(80, -25);
                selector.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                teacherBtn.setLocation(80, -20);
                selector.setVisible(false);
            }
        });

        // Hover Student
        studentBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selector.setLocation(149, 76);
                studentBtn.setLocation(80, 55);
                selector.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                studentBtn.setLocation(80, 60);
                selector.setVisible(false);
            }
        });

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                selector.setVisible(false);
            }
        });

        // Navigation
        teacherBtn.addActionListener(e ->
                Wordtropolis.showScreen(Wordtropolis.SCREEN_TEACHER));

        studentBtn.addActionListener(e ->
                Wordtropolis.showScreen(Wordtropolis.SCREEN_HERO_PICK));
    }

    private void floatTitle() {
        int bounceRange = 10;
        double speed = 0.15;

        time += speed;
        int newY = baseTitleY + (int)(Math.sin(time) * bounceRange);
        titleLabel.setLocation(titleLabel.getX(), newY);
    }

    private Font loadGameFont(int size) {
        try {
            Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream(
                            "/assets/fonts/PressStart2P-Regular.ttf")
            );
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            return new Font("Arial", Font.PLAIN, size);
        }
    }
}