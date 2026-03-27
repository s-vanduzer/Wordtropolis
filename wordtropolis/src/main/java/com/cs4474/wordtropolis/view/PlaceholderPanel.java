package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Stub panel for activities not yet implemented by other team members.
 * Replace this by registering your real panel in Wordtropolis.java.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String message, Color accentColor) {
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 24, 14, 24);
        gbc.gridx  = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Card
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 240));

        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx = 0; cg.gridy = 0;
        cg.insets = new Insets(20, 20, 8, 20);

        JLabel msgLbl = new JLabel(
                "<html><div style='text-align:center;'>"
                + message.replace("\n", "<br>") + "</div></html>",
                SwingConstants.CENTER);
        msgLbl.setFont(UITheme.FONT_TITLE);
        msgLbl.setForeground(accentColor);
        card.add(msgLbl, cg);

        cg.gridy = 1; cg.insets = new Insets(0, 20, 16, 20);
        card.add(UITheme.makeLabel("Activity coming soon – built by your teammate.",
                UITheme.FONT_BODY, UITheme.TEXT_DIM), cg);

        gbc.gridy = 0; add(card, gbc);

        JButton backBtn = UITheme.makePrimaryButton("Back to Map");
        backBtn.setPreferredSize(new Dimension(200, 46));
        backBtn.addActionListener(e -> Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP));
        gbc.gridy = 1; add(backBtn, gbc);
    }
}
