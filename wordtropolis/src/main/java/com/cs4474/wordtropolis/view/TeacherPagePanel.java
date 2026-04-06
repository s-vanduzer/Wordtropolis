package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher screen – addWords(), viewWords(), removeWords(). Lets a teacher build
 * a custom word list before students play.
 */
public class TeacherPagePanel extends JPanel {

    private DefaultListModel<String> wordListModel;
    private JList<String> wordListView;
    private JTextField wordInput;
    private JLabel statusLabel;
    private JLabel duplicateWarningLabel;
    private Image backgroundImage;

    public TeacherPagePanel() {
        loadBackgroundImage();
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        loadWordsFromFile();
        buildUI();
    }

    private void loadBackgroundImage() {
        try {
            java.net.URL imgUrl = getClass().getResource("/images/general/BG.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                backgroundImage = icon.getImage();
            }
        } catch (Exception e) {
            System.out.println("Could not load background image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            setBackground(UITheme.BG_DARK);
        }
    }

    private void loadWordsFromFile() {
        wordListModel = new DefaultListModel<>();
        try (InputStream is = getClass().getResourceAsStream("/wordlist/WordListGr1.txt")) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toUpperCase();
                    if (!word.isEmpty() && word.matches("[A-Z]+")) {
                        wordListModel.addElement(word);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        syncToGame();
    }

    // Helper method to create rounded buttons
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor, Color textColor) {
        JButton button = new JButton(text) {
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
                String btnText = getText();
                int textWidth = fm.stringWidth(btnText);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(getForeground());
                g2d.drawString(btnText, x, y);

                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {

            }
        };

        button.setFont(UITheme.FONT_BUTTON);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.repaint();
            }
        });

        return button;
    }

    private void buildUI() {
        // ── Header with Centered Wordtropolis Title and Teacher Title ──────────
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel wordtropolisTitle = new JLabel();
        try {
            ImageIcon titleIcon = new ImageIcon(getClass().getResource("/images/general/wordtropiatitle2.png"));
            Image scaledImg = titleIcon.getImage().getScaledInstance(500, 100, Image.SCALE_SMOOTH);
            wordtropolisTitle.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            wordtropolisTitle.setText("WORDTROPOLIS");
            wordtropolisTitle.setForeground(Color.WHITE);
            wordtropolisTitle.setFont(new Font("Arial", Font.BOLD, 24));
        }
        wordtropolisTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(wordtropolisTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel teacherTitle = new JLabel("Teacher Custom Word List") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(new Color(0x0F4D58));
                g2d.setStroke(new BasicStroke(3));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx, y + dy);
                        }
                    }
                }

                g2d.setColor(new Color(0xECCB2D));
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        teacherTitle.setFont(UITheme.FONT_TITLE);
        teacherTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        teacherTitle.setPreferredSize(new Dimension(500, 40));
        teacherTitle.setMaximumSize(new Dimension(500, 40));
        headerPanel.add(teacherTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ── Centre: list + input ─────────────────────────────────────────────
        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.setOpaque(false);

        wordListView = new JList<>(wordListModel);
        wordListView.setFont(UITheme.FONT_LETTER);
        wordListView.setBackground(UITheme.BG_CARD);
        wordListView.setForeground(UITheme.TEXT_BRIGHT);
        wordListView.setSelectionBackground(UITheme.ACCENT_BLUE);
        wordListView.setFixedCellHeight(30);

        JScrollPane scroll = new JScrollPane(wordListView);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_BLUE, 1));
        scroll.setPreferredSize(new Dimension(500, 120));
        centre.add(scroll, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        inputRow.setOpaque(false);

        wordInput = new JTextField(14);
        wordInput.setFont(UITheme.FONT_HEADING);
        wordInput.setBackground(UITheme.BG_CARD);
        wordInput.setForeground(UITheme.TEXT_BRIGHT);
        wordInput.setCaretColor(UITheme.ACCENT_YELLOW);
        wordInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_YELLOW, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        wordInput.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            addWord();
        });

        // SOUND WHEN CLICKING THE BOX
        wordInput.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.play(SoundManager.GAME_BTN_CLICK);
            }
        });

        JButton addBtn = createStyledButton("+ Add", new Color(0x4CAF50), new Color(0x66BB6A), Color.WHITE);
        addBtn.setPreferredSize(new Dimension(110, 35));
        addBtn.addActionListener(e -> {
            addWord();
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        });

        JButton removeBtn = createStyledButton("Remove", new Color(0xFF6B6B), new Color(0xFF8888), Color.WHITE);
        removeBtn.setPreferredSize(new Dimension(110, 35));
        removeBtn.addActionListener(e -> {
            removeWord();
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        });

        JButton clearBtn = createStyledButton("Clear All", new Color(0x4A4A4A), new Color(0x6A6A6A), Color.WHITE);
        clearBtn.setPreferredSize(new Dimension(110, 35));
        clearBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            wordListModel.clear();
            syncToGame();
            duplicateWarningLabel.setText(" ");
        });

        inputRow.add(wordInput);
        inputRow.add(addBtn);
        inputRow.add(removeBtn);
        inputRow.add(clearBtn);
        centre.add(inputRow, BorderLayout.SOUTH);

        add(centre, BorderLayout.CENTER);

        // ── Bottom: duplicate warning + status + nav ──────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        duplicateWarningLabel = new JLabel(" ");
        duplicateWarningLabel.setFont(UITheme.FONT_BODY);
        duplicateWarningLabel.setForeground(new Color(0xECCB2D)); // Yellow color
        duplicateWarningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(duplicateWarningLabel, BorderLayout.NORTH);

        statusLabel = UITheme.makeLabel(" ", UITheme.FONT_BODY, UITheme.ACCENT_TEAL);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        navRow.setOpaque(false);

        JButton backBtn = createStyledButton("Back", new Color(0x4A4A4A), new Color(0x6A6A6A), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(160, 40));
        backBtn.addActionListener(e -> {
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
        });

        JButton saveBtn = createStyledButton("Save & Return", new Color(0xECCB2D), new Color(0xFFD700), Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(200, 40));
        saveBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.GAME_BTN_CLICK);
            syncToGame();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });

        navRow.add(backBtn);
        navRow.add(saveBtn);
        bottom.add(navRow, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addWord() {
        String word = wordInput.getText().trim().toUpperCase();

        duplicateWarningLabel.setText(" ");

        if (word.isEmpty()) {
            status("Please enter a word.", UITheme.ACCENT_RED);
            return;
        }
        if (word.length() < 2) {
            status("Word must be at least 2 letters.", UITheme.ACCENT_RED);
            return;
        }
        if (!word.matches("[A-Z]+")) {
            status("Letters only — no numbers/symbols.", UITheme.ACCENT_RED);
            return;
        }
        if (wordListModel.contains(word)) {
            duplicateWarningLabel.setText("That word has already been added!");
            return;
        }
        wordListModel.addElement(word);
        wordInput.setText("");
        syncToGame();
        status("Added: " + word, UITheme.ACCENT_TEAL);
    }

    private void removeWord() {
        duplicateWarningLabel.setText(" ");
        int idx = wordListView.getSelectedIndex();
        if (idx < 0) {
            status("Select a word to remove.", UITheme.ACCENT_RED);
            return;
        }
        String removed = wordListModel.remove(idx);
        syncToGame();
        status("Removed: " + removed, Color.RED);
    }

    private void syncToGame() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < wordListModel.size(); i++) {
            list.add(wordListModel.get(i));
        }
        Game.getInstance().setWordList(list);
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
        Timer timer = new Timer(2000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
}
