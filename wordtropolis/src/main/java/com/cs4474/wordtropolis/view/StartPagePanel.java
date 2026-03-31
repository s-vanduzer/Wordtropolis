package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * Start page – player chooses "I am a Student" or "I am a Teacher".
 * Features an animated city skyline with twinkling stars.
 */
public class StartPagePanel extends JPanel {

    private final float[] starX;
    private final float[] starY;
    private final float[] starSize;
    private float shimmer = 0f;
    private Timer animTimer;

    public StartPagePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        starX    = new float[60];
        starY    = new float[60];
        starSize = new float[60];
        Random rnd = new Random(42);
        for (int i = 0; i < 60; i++) {
            starX[i]    = rnd.nextFloat() * 900;
            starY[i]    = rnd.nextFloat() * 280;
            starSize[i] = 1f + rnd.nextFloat() * 2.5f;
        }

        buildUI();

        animTimer = new Timer(50, e -> { shimmer += 0.05f; repaint(); });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sky gradient
        g2.setPaint(new GradientPaint(0, 0, UITheme.SKY_TOP, 0, getHeight() * 0.65f, UITheme.SKY_BOT));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Stars
        for (int i = 0; i < 60; i++) {
            float alpha = 0.4f + 0.6f * Math.abs((float) Math.sin(shimmer + i * 0.3f));
            g2.setColor(new Color(1f, 1f, 1f, alpha));
            g2.fill(new Ellipse2D.Float(starX[i] % getWidth(), starY[i], starSize[i], starSize[i]));
        }

        // City silhouette
        int baseY = getHeight() - 55;
        g2.setColor(new Color(0x0A1628));
        int[] bw = {55, 75, 45, 85, 65, 50, 70, 80, 60, 95, 50, 65};
        int[] bh = {90, 130, 75, 150, 115, 85, 125, 145, 105, 170, 90, 110};
        int bx = 0;
        for (int i = 0; i < bw.length; i++) {
            g2.fillRect(bx, baseY - bh[i], bw[i], bh[i]);
            bx += bw[i] + 6;
        }

        // Grass strip
        g2.setColor(UITheme.GRASS_COLOR);
        g2.fillRect(0, baseY, getWidth(), getHeight() - baseY);

        g2.dispose();
    }

    private void buildUI() {
        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets   = new Insets(10, 20, 10, 20);
        gbc.gridx    = 0;
        gbc.fill     = GridBagConstraints.NONE;
        gbc.anchor   = GridBagConstraints.CENTER;

        // Title
        JLabel title = new JLabel("WORDTROPOLIS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 46));
        title.setForeground(UITheme.ACCENT_YELLOW);
        gbc.gridy = 0; gbc.gridwidth = 2;
        overlay.add(title, gbc);

        // Subtitle
        JLabel sub = UITheme.makeLabel("Save the City from the Word Tangle Spell!",
                UITheme.FONT_HEADING, UITheme.TEXT_DIM);
        gbc.gridy = 1;
        overlay.add(sub, gbc);

        // Spacer
        gbc.gridy = 2;
        overlay.add(Box.createVerticalStrut(28), gbc);

        // Buttons side by side
        JButton studentBtn = UITheme.makePrimaryButton("  I am a Student");
        studentBtn.setPreferredSize(new Dimension(250, 56));
        studentBtn.addActionListener(e -> {
            Game.resetInstance();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_STUDENT);
        });

        JButton teacherBtn = UITheme.makeSecondaryButton("  I am a Teacher");
        teacherBtn.setPreferredSize(new Dimension(250, 56));
        teacherBtn.addActionListener(e -> Wordtropolis.showScreen(Wordtropolis.SCREEN_TEACHER));

        gbc.gridy = 3; gbc.gridwidth = 1;
        overlay.add(studentBtn, gbc);
        gbc.gridx = 1;
        overlay.add(teacherBtn, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel ver = UITheme.makeLabel("CS 4474 Group Project  |  v1.0",
                UITheme.FONT_SMALL, new Color(0x445566));
        overlay.add(ver, gbc);

        add(overlay, BorderLayout.CENTER);
    }
}
