package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;

/**
 * Finish screen – shown after the Final Boss is defeated.
 * Offers replay (back to start) or exit.
 */
public class FinishPanel extends JPanel implements Refreshable {

    private JLabel heroLabel;
    private JLabel scoreLabel;

    public FinishPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 24, 14, 24);
        gbc.gridx  = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel trophy = new JLabel("TROPHY", SwingConstants.CENTER);
        trophy.setFont(new Font("Segoe UI", Font.BOLD, 72));
        trophy.setForeground(UITheme.ACCENT_YELLOW);
        gbc.gridy = 0; add(trophy, gbc);

        gbc.gridy = 1;
        add(UITheme.makeLabel("City Saved!  You are a true Spelling Hero!",
                UITheme.FONT_TITLE, UITheme.ACCENT_YELLOW), gbc);

        heroLabel  = UITheme.makeLabel("", UITheme.FONT_HEADING, UITheme.TEXT_BRIGHT);
        scoreLabel = UITheme.makeLabel("", UITheme.FONT_HEADING, UITheme.ACCENT_TEAL);
        gbc.gridy = 2; add(heroLabel,  gbc);
        gbc.gridy = 3; add(scoreLabel, gbc);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setOpaque(false);

        JButton replayBtn = UITheme.makePrimaryButton("Play Again");
        replayBtn.setPreferredSize(new Dimension(170, 50));
        replayBtn.addActionListener(e -> {
            Game.resetInstance();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });

        JButton exitBtn = UITheme.makeDangerButton("Exit");
        exitBtn.setPreferredSize(new Dimension(130, 50));
        exitBtn.addActionListener(e -> System.exit(0));

        row.add(replayBtn);
        row.add(exitBtn);
        gbc.gridy = 4; add(row, gbc);
    }

    @Override
    public void refresh() {
        Game g = Game.getInstance();
        if (heroLabel  != null) heroLabel .setText("Hero: "         + g.getPlayerName());
        if (scoreLabel != null) scoreLabel.setText("Final Score: "  + g.getScore());
    }
}
