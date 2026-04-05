/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cs4474.wordtropolis.model;

import java.util.List;
import java.util.Random;

/**
 *
 * @author svand
 */
public class FireGameModel {

    private final List<String> words;
    private final Random random = new Random();

    private String currentWord;
    private int correctCount;
    private int wrongCount;

    /**
     * Constructor.
     *
     * @param providedWords list of words to be used in the game
     */
    public FireGameModel(List<String> providedWords) {
        if (providedWords == null || providedWords.isEmpty()) {
            throw new IllegalArgumentException("Word list cannot be null or empty.");
        }
        this.words = providedWords;
        resetGame();
    }

    /**
     * Resets the game state.
     */
    public void resetGame() {
        correctCount = 0;
        wrongCount = 0;
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
            correctCount++;
            pickNextWord();  // Move to the next word
            return true;
        } else {
            wrongCount++;
            return false;
        }
    }

    /**
     * Returns how many times the player has gotten the current word wrong.
     * @return number of times player got word wrong
     */
    public int getWrongCount() {
        return wrongCount;
    }

    /**
     * Returns the number of words the player has gotten correct.
     * @return number of words gotten correct
     */
    public int getCorrectCount() {
        return correctCount;
    }

    /**
     * Resets wrongCount
     */
    public void resetWrongCount() {
        wrongCount = 0;
    }
}
