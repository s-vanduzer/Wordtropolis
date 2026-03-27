package com.cs4474.wordtropolis.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Central design system for Wordtropolis.
 * Every panel uses these constants so the whole game stays visually consistent.
 *
 * Design principle: Consistency — same colours, fonts, and controls everywhere.
 */
public final class UITheme {

    // ── Colour palette ────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(0x0D1B2A);   // deep navy
    public static final Color BG_MID        = new Color(0x1B2F45);   // panel bg
    public static final Color BG_CARD       = new Color(0x243B55);   // card bg
    public static final Color ACCENT_YELLOW = new Color(0x654321);   // CTA / highlight
    public static final Color ACCENT_TEAL   = new Color(0x06D6A0);   // correct / success
    public static final Color ACCENT_RED    = new Color(0xEF476F);   // incorrect / danger
    public static final Color ACCENT_BLUE   = new Color(0x118AB2);   // info / neutral
    public static final Color TEXT_BRIGHT   = new Color(0xF8F9FA);
    public static final Color TEXT_DIM      = new Color(0x8EA8C3);

    // Scene-specific colours
    public static final Color LADDER_RUNG   = new Color(0xC8A97A);
    public static final Color LADDER_SIDE   = new Color(0x8B5E3C);
    public static final Color SKY_TOP       = new Color(0x1A3A5C);
    public static final Color SKY_BOT       = new Color(0x3D7AB5);
    public static final Color GRASS_COLOR   = new Color(0x2D6A2D);
    public static final Color TREE_TRUNK    = new Color(0x6B3A1F);
    public static final Color TREE_LEAVES   = new Color(0x2D8B2D);

    // ── Typography ────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  34);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_LETTER  = new Font("Courier New", Font.BOLD, 24);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Sizes ─────────────────────────────────────────────────────────────────
    public static final int TILE_SIZE     = 52;
    public static final int CORNER        = 14;

    private UITheme() {}

    // ── Button factory ────────────────────────────────────────────────────────

    public static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER, CORNER));
                g2.setFont(FONT_BUTTON);
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 46));
        return btn;
    }

    public static JButton makePrimaryButton(String text)   { return makeButton(text, ACCENT_YELLOW, BG_DARK);    }
    public static JButton makeSuccessButton(String text)   { return makeButton(text, ACCENT_TEAL,   BG_DARK);    }
    public static JButton makeDangerButton(String text)    { return makeButton(text, ACCENT_RED,    TEXT_BRIGHT); }
    public static JButton makeSecondaryButton(String text) { return makeButton(text, BG_CARD,       TEXT_BRIGHT); }

    // ── Label factory ─────────────────────────────────────────────────────────

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setOpaque(false);
        return lbl;
    }
}
