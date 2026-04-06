/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cs4474.wordtropolis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author svand
 */
public class FireGameModel {

    private static final int BASE_SCORE = 50;

    private List<String> words;
    private final Random random = new Random();

    private String currentWord;
    private int correctCount;

    private int wrongCount; // Incorrect guesses on current word
    private int totalWrongCount; // Total number of incorrect guesses

    private int fireCounter; // Counter for completed fires
    private int fireIndex;

    private int streakCount; // For score multiplier

    private int score;

    /**
     * Constructor.
     *
     */
    public FireGameModel() {
        if (Game.getInstance().getWordList() == null || Game.getInstance().getWordList().isEmpty()) {
            throw new IllegalArgumentException("Word list cannot be null or empty.");
        }
        restartGame();
    }

    /**
     * Resets the game state.
     */
    public final void restartGame() {
        // 1. Fetch/re-fetch words from singleton to ensure updates are caught
        List<String> provided = Game.getInstance().getWordList();
        this.words = new ArrayList<>(provided);
        Collections.shuffle(words);

        // 2. Reset all counters
        correctCount = 0;
        wrongCount = 0;
        totalWrongCount = 0;
        fireCounter = 0;
        fireIndex = 0;
        streakCount = 0;

        // 3. New word
        pickNextWord();
    }

    /**
     * Picks a random word from the list for the player to type.
     */
    public void pickNextWord() {
        currentWord = words.get(random.nextInt(words.size()));
        wrongCount = 0;
    }

    /**
     * Returns the current word for TTS or display.
     *
     * @return current word
     */
    public String getCurrentWord() {
        return currentWord;
    }

    /**
     * Player attempts to type the word.
     *
     * @param playerInput the string typed by the player
     * @return true if correct, false otherwise
     */
    public boolean submitWord(String playerInput) {
        if (playerInput == null) {
            playerInput = "";
        }

        if (playerInput.equalsIgnoreCase(currentWord)) {
            wrongCount = 0;

            if (wrongCount == 0 || streakCount == 0) {
                streakCount++;
            }
            
            score += BASE_SCORE * streakCount;
            correctCount++;
            pickNextWord();

            return true;
        } else {
            streakCount = 0;
            wrongCount++;
            totalWrongCount++;
            return false;
        }
    }

// ── CORRECT COUNT ────────────────────────────────────────────────────────
    public int getCorrectCount() {
        return correctCount;
    }

    // ── WRONG COUNT (Current Word) ──────────────────────────────────────────
    public int getWrongCount() {
        return wrongCount;
    }

    public void resetWrongCount() {
        wrongCount = 0;
    }

    // ── TOTAL WRONG COUNT ────────────────────────────────────────────────────
    public int getTotalWrongCount() {
        return totalWrongCount;
    }

    // ── FIRE COUNTER ─────────────────────────────────────────────────────────
    public int getFireCounter() {
        return fireCounter;
    }

    public void incrementFireCounter() {
        fireCounter++;
    }

    public void decrementFireCounter() {
        if (fireCounter > 0) {
            fireCounter--;
        }
    }

    // ── FIRE INDEX ───────────────────────────────────────────────────────────
    public void setFireIndex(int val) {
        fireIndex = val;
    }

    public int getFireIndex() {
        return fireIndex;
    }

    public void incrementFireIndex() {
        fireIndex++;
    }

    public void decrementFireIndex() {
        if (fireIndex > 0) {
            fireIndex--;
        }
    }

    // ── STREAK COUNT ─────────────────────────────────────────────────────────
    public int getStreakCount() {
        return streakCount;
    }

    public void resetStreakCount() {
        streakCount = 0;
    }

    public int getScore() {
        return score;
    }
}
