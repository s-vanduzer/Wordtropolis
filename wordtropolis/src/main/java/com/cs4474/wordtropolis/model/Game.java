package com.cs4474.wordtropolis.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;

/**
 * Core Game model – singleton holding all shared state across the application.
 * Matches the Game class from the flowchart diagram.
 *
 * Fields: playerName, wordList, avatarPath, misspelledWorldList, score,
 * currentStage, musicVol, sfxVol
 */
public class Game {

    public enum CurrentStage {
        CAT, BURGULAR, FIRE, BRIDGE, BOSS
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    private Difficulty difficulty;

    private String playerName;
    private List<String> wordList;
    private String avatarPath;
    private List<String> misspelledWorldList;
    private int score;
    private CurrentStage currentStage;
    private int musicVol;
    private int sfxVol;

    // Add this near your other private fields (like playerName, wordList, etc.)
    private boolean isCustomListActive = false;

    // Activity completion flags
    private boolean catCompleted;
    private boolean burgularCompleted;
    private boolean fireCompleted;
    private boolean bridgeCompleted;

    private static Game instance;

    private Game() {
        wordList = new ArrayList<>();
        misspelledWorldList = new ArrayList<>();
        score = 0;
        musicVol = 70;
        sfxVol = 80;
        currentStage = CurrentStage.CAT;

        // Default word list used when no teacher list is loaded
        wordList.add("CAT");
        wordList.add("TREE");
        wordList.add("JUMP");
        wordList.add("CITY");
        wordList.add("HERO");
        wordList.add("BRAVE");
        wordList.add("CLIMB");
        wordList.add("SPELL");
        wordList.add("MAGIC");
        wordList.add("RESCUE");
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    /**
     * Full reset – used when the player chooses "Play Again".
     */
    public static void resetInstance() {
        instance = new Game();
    }

    public void loadWordsFromFile(String filename) {
        List<String> loadedWords = new ArrayList<>();

        // 1. Construct the path - ensure it matches your src/main/resources folder structure
        String path = "/wordlist/" + filename;

        // 2. Open the stream first to check for null
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[Game] Critical Error: Could not find " + path + " in JAR resources.");
                return;
            }

            // 3. Wrap in a Scanner
            try (Scanner sc = new Scanner(is)) {
                while (sc.hasNextLine()) {
                    String word = sc.nextLine().trim().toUpperCase();
                    if (!word.isEmpty() && word.matches("[A-Z]+")) {
                        loadedWords.add(word);
                    }
                }
            }

            Collections.shuffle(loadedWords);

            if (!loadedWords.isEmpty()) {
                this.wordList = loadedWords;
                System.out.println("[Game] Successfully loaded " + loadedWords.size() + " words from " + filename);
            }

        } catch (Exception e) {
            System.err.println("Error processing word file: " + filename);
            e.printStackTrace();
        }
    }

    public void loadWordsForDifficulty() {
        // If a teacher has provided a custom list, exit immediately to avoid overwriting it
        if (isCustomListActive) {
            System.out.println("Custom word list is active. Skipping difficulty-based file load.");
            return;
        }

        if (difficulty == null) {
            difficulty = Difficulty.HARD;
        }

        switch (difficulty) {
            case EASY:
                loadWordsFromFile("Wordlist.txt");
                break;
            case MEDIUM:
                loadWordsFromFile("WordListGr1.txt");
                break;
            case HARD:
                loadWordsFromFile("WordListGr2.txt");
                break;
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> list) {
        this.wordList = list;
        this.isCustomListActive = true; // Mark that a teacher/user has provided these words
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public List<String> getMisspelledWorldList() {
        return misspelledWorldList;
    }

    public void addMisspelledWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        String upper = word.toUpperCase().trim();
        if (!misspelledWorldList.contains(upper)) {
            misspelledWorldList.add(upper);

            // DEBUG PRINT LINE
            System.out.println("--- DEBUG: Misspelled List Updated ---");
            System.out.println("Added: " + upper);
            System.out.println("Current List: " + misspelledWorldList);
            System.out.println("---------------------------------------");
        }
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public CurrentStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(CurrentStage s) {
        this.currentStage = s;
    }

    public int getMusicVol() {
        return musicVol;
    }

    public void setMusicVol(int v) {
        this.musicVol = v;
    }

    public int getSfxVol() {
        return sfxVol;
    }

    public void setSfxVol(int v) {
        this.sfxVol = v;
    }

    public boolean isCatCompleted() {
        return catCompleted;
    }

    public void setCatCompleted(boolean v) {
        this.catCompleted = v;
    }

    public boolean isBurgularCompleted() {
        return burgularCompleted;
    }

    public void setBurgularCompleted(boolean v) {
        this.burgularCompleted = v;
    }

    public boolean isFireCompleted() {
        return fireCompleted;
    }

    public void setFireCompleted(boolean v) {
        this.fireCompleted = v;
    }

    public boolean isBridgeCompleted() {
        return bridgeCompleted;
    }

    public void setBridgeCompleted(boolean v) {
        this.bridgeCompleted = v;
    }

    public List<String> getMisspelledWordList() {
        return misspelledWorldList != null ? new ArrayList<>(misspelledWorldList) : new ArrayList<>();
    }

    public boolean allActivitiesCompleted() {
        return catCompleted && burgularCompleted && fireCompleted && bridgeCompleted;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty d) {
        this.difficulty = d;
    }
}
