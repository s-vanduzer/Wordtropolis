package com.cs4474.wordtropolis.view;

import com.cs4474.wordtropolis.Wordtropolis;
import com.cs4474.wordtropolis.model.Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Teacher screen – addWords(), viewWords(), removeWords().
 * Lets a teacher build a custom word list before students play.
 */
public class TeacherPagePanel extends JPanel {

    private DefaultListModel<String> wordListModel;
    private JList<String>            wordListView;
    private JTextField               wordInput;
    private JLabel                   statusLabel;

    public TeacherPagePanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        buildUI();
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────
        JLabel title = UITheme.makeLabel("Teacher Word List Manager",
                UITheme.FONT_TITLE, UITheme.ACCENT_YELLOW);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);

        // ── Centre: list + input ─────────────────────────────
        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.setOpaque(false);

        wordListModel = new DefaultListModel<>();
        for (String w : Game.getInstance().getWordList()) wordListModel.addElement(w);

        wordListView = new JList<>(wordListModel);
        wordListView.setFont(UITheme.FONT_LETTER);
        wordListView.setBackground(UITheme.BG_CARD);
        wordListView.setForeground(UITheme.TEXT_BRIGHT);
        wordListView.setSelectionBackground(UITheme.ACCENT_BLUE);
        wordListView.setFixedCellHeight(38);

        JScrollPane scroll = new JScrollPane(wordListView);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_BLUE, 1));
        centre.add(scroll, BorderLayout.CENTER);

        // Input row
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        inputRow.setOpaque(false);

        wordInput = new JTextField(14);
        wordInput.setFont(UITheme.FONT_HEADING);
        wordInput.setBackground(UITheme.BG_CARD);
        wordInput.setForeground(UITheme.TEXT_BRIGHT);
        wordInput.setCaretColor(UITheme.ACCENT_YELLOW);
        wordInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_YELLOW, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        wordInput.addActionListener(e -> addWord());

        JButton addBtn    = UITheme.makeSuccessButton("+ Add");
        addBtn.setPreferredSize(new Dimension(110, 40));
        addBtn.addActionListener(e -> addWord());

        JButton removeBtn = UITheme.makeDangerButton("Remove");
        removeBtn.setPreferredSize(new Dimension(110, 40));
        removeBtn.addActionListener(e -> removeWord());

        JButton clearBtn  = UITheme.makeSecondaryButton("Clear All");
        clearBtn.setPreferredSize(new Dimension(110, 40));
        clearBtn.addActionListener(e -> { wordListModel.clear(); syncToGame(); });

        inputRow.add(wordInput);
        inputRow.add(addBtn);
        inputRow.add(removeBtn);
        inputRow.add(clearBtn);
        centre.add(inputRow, BorderLayout.SOUTH);

        add(centre, BorderLayout.CENTER);

        // ── Bottom: status + nav ──────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        statusLabel = UITheme.makeLabel(" ", UITheme.FONT_BODY, UITheme.ACCENT_TEAL);
        bottom.add(statusLabel, BorderLayout.NORTH);

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        navRow.setOpaque(false);

        JButton backBtn = UITheme.makeSecondaryButton("Back");
        backBtn.addActionListener(e -> Wordtropolis.showScreen(Wordtropolis.SCREEN_START));

        JButton saveBtn = UITheme.makePrimaryButton("Save & Return");
        saveBtn.setPreferredSize(new Dimension(200, 46));
        saveBtn.addActionListener(e -> {
            syncToGame();
            Wordtropolis.showScreen(Wordtropolis.SCREEN_START);
        });

        navRow.add(backBtn);
        navRow.add(saveBtn);
        bottom.add(navRow, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addWord() {
        String word = wordInput.getText().trim().toUpperCase();
        if (word.isEmpty())              { status("Please enter a word.",            UITheme.ACCENT_RED);    return; }
        if (word.length() < 2)           { status("Word must be at least 2 letters.",UITheme.ACCENT_RED);    return; }
        if (!word.matches("[A-Z]+"))      { status("Letters only — no numbers/symbols.",UITheme.ACCENT_RED); return; }
        if (wordListModel.contains(word)){ status("Word already in the list.",       UITheme.ACCENT_YELLOW); return; }
        wordListModel.addElement(word);
        wordInput.setText("");
        syncToGame();
        status("Added: " + word, UITheme.ACCENT_TEAL);
    }

    private void removeWord() {
        int idx = wordListView.getSelectedIndex();
        if (idx < 0) { status("Select a word to remove.", UITheme.ACCENT_RED); return; }
        String removed = wordListModel.remove(idx);
        syncToGame();
        status("Removed: " + removed, UITheme.ACCENT_YELLOW);
    }

    private void syncToGame() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < wordListModel.size(); i++) list.add(wordListModel.get(i));
        if (list.isEmpty()) {
            // Restore defaults if teacher cleared everything
            list = new ArrayList<>(Arrays.asList(
                    "CAT","TREE","JUMP","CITY","HERO","BRAVE","CLIMB","SPELL","MAGIC","RESCUE"));
            list.forEach(wordListModel::addElement);
        }
        Game.getInstance().setWordList(list);
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
