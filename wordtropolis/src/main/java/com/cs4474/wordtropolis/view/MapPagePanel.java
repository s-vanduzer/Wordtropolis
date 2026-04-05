package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * City map – displaymap(), current_stage().
 * Shows 4 activity buttons + Final Boss unlock.
 * Implements Refreshable so score and completion badges update on every visit.
 */
public class MapPagePanel extends JPanel implements Refreshable {

    private static final String[] ACT_LABELS  = {
        "Cat in Tree", "Fire Rescue", "Fix the Bridge", "Burglar Chase"
    };
    private static final String[] ACT_SCREENS = {
        Wordtropolis.SCREEN_CAT_GAME,
        Wordtropolis.SCREEN_FIRE_GAME,
        Wordtropolis.SCREEN_BRIDGE_GAME,
        Wordtropolis.SCREEN_BURGLAR_GAME
    };
    private static final Color[] ACT_COLORS = {
        new Color(0x2D8B2D),
        new Color(0xE05A1B),
        new Color(0x118AB2),
        new Color(0x6B4C9A)
    };

    private JLabel   playerLabel;
    private JLabel   scoreLabel;
    private JButton  bossBtn;
    private JButton[] actButtons = new JButton[4];

    public MapPagePanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 10, 28));

        JLabel title = UITheme.makeLabel("City Map  –  Choose a Mission",
                UITheme.FONT_TITLE, UITheme.ACCENT_YELLOW);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        playerLabel = UITheme.makeLabel("Hero: —", UITheme.FONT_BODY, UITheme.TEXT_BRIGHT);
        playerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        scoreLabel  = UITheme.makeLabel("Score: 0", UITheme.FONT_BODY, UITheme.ACCENT_YELLOW);
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        info.add(playerLabel);
        info.add(scoreLabel);
        header.add(info, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2×2 activity grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(8, 36, 8, 36));

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            actButtons[i] = makeActivityButton(idx);
            actButtons[i].addActionListener(e -> {
                // Play click sound conditionally (only if we're on map screen)
                SoundManager.playConditional(SoundManager.BOSS_HINT_PLACE, 
                    SoundManager.GameActivity.MAP);
                
                if (idx == 0) {
                    // Rebuild Cat Game when the first button is clicked
                    Wordtropolis.replaceScreen(Wordtropolis.SCREEN_CAT_GAME, new CatGamePanel());
                } 
                else if (idx == 3) {
                    // Rebuild Burglar Game when the fourth button is clicked
                    Wordtropolis.replaceScreen(Wordtropolis.SCREEN_BURGLAR_GAME, new BurglarGamePanel());
                } 
                else if (idx == 2){
                    // link the brige game here
                    // replace with this: Wordtropolis.replaceScreen(Wordtropolis.SCREEN_BRIDGE_GAME, new BossGamePanel());
                    Wordtropolis.showScreen(ACT_SCREENS[idx]);
                    
                   // if u want to try the boss game uncomment this and click the brige game placeholder
                   //Wordtropolis.replaceScreen(Wordtropolis.SCREEN_BOSS_GAME, new BossGamePanel());
                }
                else {
                    Wordtropolis.replaceScreen(Wordtropolis.SCREEN_FIRE_GAME, new FireGamePanel());
                    // link the fire game here
                    // replace with this: Wordtropolis.replaceScreen(Wordtropolis.SCREEN_FIRE_GAME, new BossGamePanel());
                    //Wordtropolis.showScreen(ACT_SCREENS[idx]);
                    
                }
            });
            grid.add(actButtons[i]);
        }
        add(grid, BorderLayout.CENTER);

        // Bottom – boss button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        bottom.setOpaque(false);

        bossBtn = UITheme.makeDangerButton("Fight the Final Boss!");
        bossBtn.setPreferredSize(new Dimension(270, 52));
        bossBtn.setEnabled(false);
        bossBtn.addActionListener(e -> {
            // Play boss fight click sound conditionally
            SoundManager.playConditional(SoundManager.BOSS_HINT_PLACE, 
                SoundManager.GameActivity.MAP);
            Wordtropolis.showScreen(Wordtropolis.SCREEN_BOSS_GAME);
        });
        bottom.add(bossBtn);
        bottom.add(UITheme.makeLabel("Complete all 4 missions to unlock",
                UITheme.FONT_SMALL, UITheme.TEXT_DIM));
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton makeActivityButton(int idx) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = ACT_COLORS[idx];
                if (getModel().isRollover()) base = base.brighter();
                if (getModel().isPressed())  base = base.darker();
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setFont(UITheme.FONT_HEADING);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String label = ACT_LABELS[idx];
                g2.drawString(label,
                        (getWidth() - fm.stringWidth(label)) / 2,
                        getHeight() / 2 + fm.getAscent() / 2);
                // Tick badge when done
                if (isActivityDone(idx)) {
                    g2.setColor(UITheme.ACCENT_TEAL);
                    g2.fillOval(getWidth() - 42, 8, 34, 34);
                    g2.setFont(UITheme.FONT_BUTTON);
                    g2.setColor(UITheme.BG_DARK);
                    g2.drawString("OK", getWidth() - 36, 30);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private boolean isActivityDone(int idx) {
        Game g = Game.getInstance();
        switch (idx) {
            case 0: return g.isCatCompleted();
            case 1: return g.isFireCompleted();
            case 2: return g.isBridgeCompleted();
            case 3: return g.isBurgularCompleted();
            default: return false;
        }
    }
    
    private void endGame(boolean win) {
        if (win) {
            JOptionPane.showMessageDialog(this, "City Saved! Final Score: " + Game.getInstance().getScore());
        }
        Wordtropolis.showScreen(Wordtropolis.SCREEN_MAP);
    }

    @Override
    public void refresh() {
        Game g = Game.getInstance();
        if (playerLabel != null) playerLabel.setText("Hero: " + g.getPlayerName());
        if (scoreLabel  != null) scoreLabel .setText("Score: " + g.getScore());
        if (bossBtn     != null) bossBtn    .setEnabled(g.allActivitiesCompleted());
        for (JButton b : actButtons) if (b != null) b.repaint();
        
        if (bossBtn != null) {
            boolean unlocked = g.allActivitiesCompleted();
            bossBtn.setEnabled(unlocked);
            bossBtn.setText(unlocked ? "FIGHT BOSS" : "LOCKED");
            
            // Add the listener if unlocked
            if (unlocked && bossBtn.getActionListeners().length == 0) {
                bossBtn.addActionListener(e -> {
                    // Rebuild the Boss panel to ensure it has the latest missed words
                    Wordtropolis.replaceScreen(Wordtropolis.SCREEN_BOSS_GAME, new BossGamePanel());
                });
            }
        }
    }
}