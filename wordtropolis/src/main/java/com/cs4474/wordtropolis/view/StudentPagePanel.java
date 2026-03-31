package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;

/**
 * Student page – enterName(), startGame().
 */
public class StudentPagePanel extends JPanel {

    private JTextField nameField;
    private JLabel     statusLabel;

    public StudentPagePanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(12, 24, 12, 24);
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.NONE;
        gbc.anchor  = GridBagConstraints.CENTER;

        JLabel icon = new JLabel("hero", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 64));
        icon.setForeground(UITheme.ACCENT_YELLOW);
        gbc.gridy = 0; add(icon, gbc);

        gbc.gridy = 1;
        add(UITheme.makeLabel("Enter Your Hero Name!", UITheme.FONT_TITLE, UITheme.ACCENT_YELLOW), gbc);

        gbc.gridy = 2;
        add(UITheme.makeLabel("The city needs your help!", UITheme.FONT_BODY, UITheme.TEXT_DIM), gbc);

        nameField = new JTextField(16);
        nameField.setFont(UITheme.FONT_HEADING);
        nameField.setBackground(UITheme.BG_CARD);
        nameField.setForeground(UITheme.TEXT_BRIGHT);
        nameField.setCaretColor(UITheme.ACCENT_YELLOW);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_YELLOW, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        nameField.addActionListener(e -> startGame());
        gbc.gridy = 3; add(nameField, gbc);

        statusLabel = UITheme.makeLabel(" ", UITheme.FONT_BODY, UITheme.ACCENT_RED);
        gbc.gridy = 4; add(statusLabel, gbc);

        JButton startBtn = UITheme.makePrimaryButton("Start Adventure!");
        startBtn.setPreferredSize(new Dimension(240, 52));
        startBtn.addActionListener(e -> startGame());
        gbc.gridy = 5; add(startBtn, gbc);

        JButton backBtn = UITheme.makeSecondaryButton("Back");
        backBtn.addActionListener(e -> Wordtropolis.showScreen(Wordtropolis.SCREEN_START));
        gbc.gridy = 6; add(backBtn, gbc);
    }

    private void startGame() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Please enter your name first!");
            return;
        }
        if (name.length() > 20) {
            statusLabel.setText("Name too long – max 20 characters.");
            return;
        }
        Game.getInstance().setPlayerName(name);
        Wordtropolis.showScreen(Wordtropolis.SCREEN_HERO_PICK);
    }
}
