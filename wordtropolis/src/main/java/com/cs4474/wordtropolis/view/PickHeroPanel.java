package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pick Hero page – displayChar(), selectChar(), confirmChar().
 * Shows four hero cards; the selected card is highlighted.
 */
public class PickHeroPanel extends JPanel {

    private static final String[] HERO_LABELS = { "Alex",   "Mia",    "Zyx",    "Finn"   };
    private static final String[] HERO_DESC   = { "Brave",  "Wise",   "Fast",   "Clever" };
    private static final Color[]  HERO_COLORS = {
        new Color(0x4A90D9),
        new Color(0xA855F7),
        new Color(0x06D6A0),
        new Color(0xFFD166)
    };

    private int    selectedHero = 0;
    private JLabel confirmLabel;

    public PickHeroPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        buildUI();
    }

    private void buildUI() {
        add(UITheme.makeLabel("Choose Your Hero!", UITheme.FONT_TITLE, UITheme.ACCENT_YELLOW),
                BorderLayout.NORTH);

        // Four hero cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();

        for (int i = 0; i < HERO_LABELS.length; i++) {
            final int idx = i;
            JToggleButton card = makeHeroCard(idx);
            bg.add(card);
            if (i == 0) card.setSelected(true);
            card.addActionListener(e -> {
                selectedHero = idx;
                confirmLabel.setText("Hero: " + HERO_LABELS[idx]);
            });
            cardsRow.add(card);
        }
        add(cardsRow, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottom.setOpaque(false);

        confirmLabel = UITheme.makeLabel("Hero: " + HERO_LABELS[0],
                UITheme.FONT_HEADING, UITheme.ACCENT_TEAL);
        bottom.add(confirmLabel);

        JButton confirmBtn = UITheme.makePrimaryButton("Confirm Hero!");
        confirmBtn.setPreferredSize(new Dimension(200, 48));
        confirmBtn.addActionListener(e -> {
            Game.getInstance().setAvatarPath(HERO_LABELS[selectedHero]);
            Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
        });
        bottom.add(confirmBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private JToggleButton makeHeroCard(int idx) {
        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isSelected() ? HERO_COLORS[idx] : UITheme.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                if (isSelected()) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(3));
                    g2.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 20, 20));
                }
                // Hero initial (large letter)
                g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
                g2.setColor(isSelected() ? UITheme.BG_DARK : HERO_COLORS[idx]);
                String init = HERO_LABELS[idx].substring(0, 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (getWidth() - fm.stringWidth(init)) / 2, 75);
                // Name
                g2.setFont(UITheme.FONT_BUTTON);
                g2.setColor(isSelected() ? UITheme.BG_DARK : UITheme.TEXT_BRIGHT);
                String name = HERO_LABELS[idx];
                fm = g2.getFontMetrics();
                g2.drawString(name, (getWidth() - fm.stringWidth(name)) / 2, 110);
                // Desc
                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(isSelected() ? UITheme.BG_DARK : UITheme.TEXT_DIM);
                String desc = HERO_DESC[idx];
                fm = g2.getFontMetrics();
                g2.drawString(desc, (getWidth() - fm.stringWidth(desc)) / 2, 132);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 160));
        return btn;
    }
}
