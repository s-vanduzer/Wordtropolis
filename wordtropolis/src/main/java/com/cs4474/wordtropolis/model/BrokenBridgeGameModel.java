package com.cs4474.wordtropolis.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Model for the Broken Bridge mini-game.
 *
 * Timer behaviour: when it hits zero the panel clears the selection and
 * resets the timer (same as CatGamePanel "Time's up – try again").
 * Game ends only when bridgeProgress reaches TOTAL_ROUNDS.
 *
 * Word count increases as the bridge is repaired:
 *   Rounds 1–2  →  3 words
 *   Rounds 3–4  →  4 words
 *   Rounds 5–6  →  5 words
 */
public class BrokenBridgeGameModel {

    public static final int TOTAL_ROUNDS       = 6;
    public static final int ROUND_DURATION_SEC = 60;
    public static final int POINTS_CORRECT     = 50;

    /** Maximum words ever shown — used only to guarantee wordPool is large enough. */
    private static final int MAX_WORDS = 5;

    public enum Difficulty { GRADE1, GRADE2 }

    // ── State ─────────────────────────────────────────────────────────────────
    private final Difficulty   difficulty;
    private final List<String> wordPool = new ArrayList<>();

    private List<String>       displayWords = new ArrayList<>();
    private List<String>       correctOrder = new ArrayList<>();
    private final List<String> selected     = new ArrayList<>();

    private int score;
    private int bridgeProgress;
    private int timeRemaining;
    private int incorrectAttempts;

    // ── Construction ──────────────────────────────────────────────────────────

    public BrokenBridgeGameModel(Difficulty difficulty) {
        this.difficulty = difficulty;
        loadWordPool();
        resetGame();
    }

    // ── Difficulty ramp ───────────────────────────────────────────────────────

    /**
     * How many words to order this round — increases as the bridge gets repaired.
     *   bridgeProgress 0–1  →  3 words  (rounds 1–2)
     *   bridgeProgress 2–3  →  4 words  (rounds 3–4)
     *   bridgeProgress 4–5  →  5 words  (rounds 5–6)
     */
    public int getWordsThisRound() {
        if (bridgeProgress <= 1) return 3;
        if (bridgeProgress <= 3) return 4;
        return 5;
    }

    // ── Word pool ─────────────────────────────────────────────────────────────

    private void loadWordPool() {
        List<String> shared = Game.getInstance().getWordList();
        if (shared != null && !shared.isEmpty()) {
            for (String w : shared) {
                String lower = w.trim().toLowerCase();
                if (!lower.isEmpty()) wordPool.add(lower);
            }
        }
        // Fallback — guarantee at least MAX_WORDS entries
        if (wordPool.size() < MAX_WORDS) {
            wordPool.addAll(Arrays.asList(
                "ant","bear","cat","dog","elk","fox","hen","jay",
                "lynx","mole","owl","pug","rat","toad","yak","zebra",
                "apple","bridge","cloud","drum","eagle","flame","grape"
            ));
        }
        Collections.shuffle(wordPool);
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void resetGame() {
        score             = 0;
        bridgeProgress    = 0;
        timeRemaining     = ROUND_DURATION_SEC;
        incorrectAttempts = 0;
        newRound();
    }

    /** Reset the countdown to a full round. Called on timeout or after a correct round. */
    public void resetTimer() {
        timeRemaining = ROUND_DURATION_SEC;
    }

    // ── Round management ──────────────────────────────────────────────────────

    public void newRound() {
        selected.clear();
        List<String> pool = new ArrayList<>(wordPool);
        Collections.shuffle(pool);

        // Use the dynamic count — not a fixed constant
        int count = Math.min(getWordsThisRound(), pool.size());
        correctOrder = pool.subList(0, count).stream().sorted().collect(Collectors.toList());
        displayWords = new ArrayList<>(correctOrder);

        int guard = 0;
        while (displayWords.equals(correctOrder) && guard++ < 12)
            Collections.shuffle(displayWords);
    }

    // ── Player interaction ────────────────────────────────────────────────────

    public boolean selectWord(String word) {
        if (selected.contains(word)) return false;
        selected.add(word);
        return true;
    }

    public boolean deselectWord(String word) { return selected.remove(word); }

    public void clearSelection() { selected.clear(); }

    /**
     * Checks whether the full selection matches the correct alphabetical order.
     * Uses getWordsThisRound() so the check always matches the current word count.
     */
    public boolean isAnswerCorrect() {
        if (selected.size() != getWordsThisRound()) return false;
        for (int i = 0; i < selected.size(); i++)
            if (!selected.get(i).equals(correctOrder.get(i))) return false;
        return true;
    }

    public void applyCorrectRound() {
        score         += POINTS_CORRECT;
        bridgeProgress = Math.min(bridgeProgress + 1, TOTAL_ROUNDS);
    }

    public void recordIncorrect() { incorrectAttempts++; }

    // ── Timer ─────────────────────────────────────────────────────────────────

    /** Decrement by one second. Returns true if time just hit zero. */
    public boolean tickSecond() {
        if (timeRemaining > 0) { timeRemaining--; return timeRemaining == 0; }
        return false;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<String> getDisplayWords()     { return Collections.unmodifiableList(displayWords); }
    public List<String> getCorrectOrder()     { return Collections.unmodifiableList(correctOrder); }
    public List<String> getSelected()         { return Collections.unmodifiableList(selected); }
    public int          getScore()            { return score; }
    public int          getBridgeProgress()   { return bridgeProgress; }
    public int          getTimeRemaining()    { return timeRemaining; }
    public int          getIncorrectAttempts(){ return incorrectAttempts; }
    public boolean      isComplete()          { return bridgeProgress >= TOTAL_ROUNDS; }
    public Difficulty   getDifficulty()       { return difficulty; }
}