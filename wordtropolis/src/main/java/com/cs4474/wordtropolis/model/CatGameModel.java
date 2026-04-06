package com.cs4474.wordtropolis.model;

import java.util.*;

/**
 * CatGameModel.java
 */
public class CatGameModel {

    // ── Difficulty ────────────────────────────────────────────────────────────
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    /**
     * Result codes returned by typeKey(). CatGamePanel uses these to decide
     * what feedback to show.
     */
    public enum TypeResult {
        /**
         * Letter found in pool → tile moved, play tile-click sound + effect
         */
        TYPED_OK,
        /**
         * Letter NOT in pool → shake input area, show "not an option" msg
         */
        TYPED_NOT_AVAILABLE,
        /**
         * Arrangement already full → nudge player to submit
         */
        TYPED_FULL
    }

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final int TOTAL_WORDS = 6;
    public static final int POINTS_PER_CORRECT = 50;
//    public static final int PENALTY_INCORRECT  = 10;

    private static final char[] DISTRACTOR_POOL = "XQZVWBJK".toCharArray();

    // ── State ─────────────────────────────────────────────────────────────────
    private List<String> words;
    private List<String> wordQueue;
    private String currentWord;
    private List<Character> playerArrangement;
    private List<Character> availableLetters;

    private int wordsCompleted;
    private int incorrectAttempts;
    private int totalScore;
    private Difficulty currentDifficulty;

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * Create the model using the word list provided by the teacher via
     * Game.getInstance().getWordList().
     *
     * We do NOT inject extra default words on top — we use exactly what is
     * provided. The only exception is if the list arrives completely empty, in
     * which case an emergency set is used so the game never crashes.
     *
     * @param providedWords list from Game.getWordList()
     */
    public CatGameModel(List<String> providedWords) {
        this.words = providedWords;
        restartGame();
    }

    public final void restartGame() {
        // 1. Fetch/re-fetch words from singleton to ensure updates are caught
        List<String> provided = Game.getInstance().getWordList();
        this.words = new ArrayList<>(provided);
        Collections.shuffle(words);

        // 2. Existing reset logic
        wordQueue = buildWordQueue(this.words);
        wordsCompleted = 0;
        incorrectAttempts = 0;
        totalScore = 0;
        currentDifficulty = Difficulty.EASY;

        // 3. Start the game
        loadNextWord();
    }

    // ── Word queue ────────────────────────────────────────────────────────────
    /**
     * Build the ordered word queue from the provided list.
     *
     * Rules (in order): 1. Uppercase + trim every entry, skip anything
     * non-alphabetic 2. Remove duplicates (keep first occurrence) 3. Sort
     * shortest → longest (easier words first) 4. Cap at TOTAL_WORDS entries 5.
     * If fewer than TOTAL_WORDS, cycle the list to fill the gap 6. Emergency
     * fallback only if list was completely empty
     */
    private List<String> buildWordQueue(List<String> provided) {
        // Step 1 + 2 — clean, uppercase, deduplicate
        List<String> clean = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String w : provided) {
            if (w == null) {
                continue;
            }
            String upper = w.trim().toUpperCase();
            if (!upper.isEmpty() && upper.matches("[A-Z]+") && seen.add(upper)) {
                clean.add(upper);
            }
        }

        // Step 6 — emergency fallback
        if (clean.isEmpty()) {
            System.out.println("[CatGameModel] WARNING: word list empty, using emergency defaults.");
            clean = new ArrayList<>(Arrays.asList(
                    "CAT", "TREE", "BRAVE", "CLIMB", "SPELL", "RESCUE"));
        }

        // Step 3 — sort shortest first
        clean.sort(Comparator.comparingInt(String::length));

        // Step 4 — cap at TOTAL_WORDS
        if (clean.size() > TOTAL_WORDS) {
            clean = clean.subList(0, TOTAL_WORDS);
        }

        // Step 5 — pad by cycling if fewer than TOTAL_WORDS
        List<String> base = new ArrayList<>(clean);
        int i = 0;
        while (clean.size() < TOTAL_WORDS) {
            clean.add(base.get(i++ % base.size()));
        }

        return new ArrayList<>(clean);
    }

    // ── Word loading ──────────────────────────────────────────────────────────
    /**
     * Advance to the next word. Returns false when all words are done.
     */
    public boolean loadNextWord() {
        if (wordQueue.isEmpty()) {
            return false;
        }
        currentWord = wordQueue.remove(0);
        availableLetters = buildAvailableLetters(currentWord, currentDifficulty);
        playerArrangement = new ArrayList<>();
        updateDifficulty();
        return true;
    }

    private List<Character> buildAvailableLetters(String word, Difficulty diff) {
        List<Character> letters = new ArrayList<>();
        for (char c : word.toCharArray()) {
            letters.add(c);
        }

        // Add distractor letters at MEDIUM / HARD
        int distractors = (diff == Difficulty.MEDIUM) ? 1
                : (diff == Difficulty.HARD) ? 2 : 0;
        Random rand = new Random();
        for (int i = 0; i < distractors; i++) {
            letters.add(DISTRACTOR_POOL[rand.nextInt(DISTRACTOR_POOL.length)]);
        }

        // Shuffle until different from original word order
        int tries = 0;
        do {
            Collections.shuffle(letters);
            tries++;
        } while (tries < 20 && spellsOriginal(letters, word));

        return letters;
    }

    private boolean spellsOriginal(List<Character> list, String word) {
        if (list.size() < word.length()) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (list.get(i) != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private void updateDifficulty() {
        if (wordsCompleted < 2) {
            currentDifficulty = Difficulty.EASY;
        } else if (wordsCompleted < 4) {
            currentDifficulty = Difficulty.MEDIUM;
        } else {
            currentDifficulty = Difficulty.HARD;
        }
    }

    // ── Tile click input (existing) ───────────────────────────────────────────
    /**
     * Move a tile from the available pool into the arrangement. Called when the
     * player clicks an available letter tile.
     *
     * @param availableIndex index in availableLetters list
     * @return true if moved successfully
     */
    public boolean selectLetter(int availableIndex) {
        if (availableIndex < 0 || availableIndex >= availableLetters.size()) {
            return false;
        }
        playerArrangement.add(availableLetters.remove(availableIndex));
        return true;
    }

    /**
     * Move a tile from the arrangement back to the pool. Called when the player
     * clicks an already-placed tile.
     *
     * @param arrangementIndex index in playerArrangement list
     * @return true if moved successfully
     */
    public boolean removeLetter(int arrangementIndex) {
        if (arrangementIndex < 0 || arrangementIndex >= playerArrangement.size()) {
            return false;
        }
        availableLetters.add(playerArrangement.remove(arrangementIndex));
        return true;
    }

    /**
     * Return all placed tiles to the pool. Called by the Clear button.
     */
    public void clearArrangement() {
        availableLetters.addAll(playerArrangement);
        playerArrangement.clear();
        Collections.shuffle(availableLetters);
    }

    /**
     * Swap two tiles within the arrangement. Called when the player drags one
     * placed tile onto another slot.
     *
     * @param i index of first tile
     * @param j index of second tile
     */
    public void swapArrangement(int i, int j) {
        if (i < 0 || j < 0 || i >= playerArrangement.size() || j >= playerArrangement.size()) {
            return;
        }
        char tmp = playerArrangement.get(i);
        playerArrangement.set(i, playerArrangement.get(j));
        playerArrangement.set(j, tmp);
    }

    // ── Keyboard input (new) ──────────────────────────────────────────────────
    /**
     * Handle a keyboard key press.
     *
     * Logic: 1. Ignore anything that is not a letter (digits, punctuation,
     * etc.) 2. If the arrangement is already full → TYPED_FULL 3. Search
     * availableLetters for the FIRST tile matching this letter found → move it
     * into arrangement → TYPED_OK (CatGamePanel plays tile-click sound + same
     * tile effect) not found → TYPED_NOT_AVAILABLE (CatGamePanel shakes input
     * area + shows "not an option")
     *
     * @param key the character the player typed (any case)
     * @return TypeResult enum value
     */
    public TypeResult typeKey(char key) {
        char upper = Character.toUpperCase(key);

        // Ignore non-letter characters silently
        if (!Character.isLetter(upper)) {
            return TypeResult.TYPED_NOT_AVAILABLE;
        }

        // Word is already fully filled — tell player to submit
        if (playerArrangement.size() >= currentWord.length()) {
            return TypeResult.TYPED_FULL;
        }

        // Search for the first matching tile in the available pool
        for (int i = 0; i < availableLetters.size(); i++) {
            if (availableLetters.get(i) == upper) {
                playerArrangement.add(availableLetters.remove(i));
                return TypeResult.TYPED_OK;
            }
        }

        // Letter not available in the pool
        return TypeResult.TYPED_NOT_AVAILABLE;
    }

    /**
     * Handle the Backspace key. Removes the LAST placed tile and returns it to
     * the available pool. Identical in effect to clicking the last tile in the
     * arrangement row.
     *
     * @return true if a tile was removed, false if arrangement was empty
     */
    public boolean backspace() {
        if (playerArrangement.isEmpty()) {
            return false;
        }
        return removeLetter(playerArrangement.size() - 1);
    }

    // ── Answer checking ───────────────────────────────────────────────────────
    /**
     * Compare the current arrangement against the target word.
     *
     * Correct → increments wordsCompleted + score + updates Game singleton
     * Incorrect → increments incorrectAttempts, deducts penalty, records
     * misspelling, returns all tiles to pool
     *
     * @return true if correct
     */
    public boolean submitAnswer() {
        StringBuilder sb = new StringBuilder();
        for (char c : playerArrangement) {
            sb.append(c);
        }
        String answer = sb.toString();

        if (answer.equals(currentWord)) {
            wordsCompleted++;
            totalScore += POINTS_PER_CORRECT;
            Game.getInstance().addScore(POINTS_PER_CORRECT);
            return true;
        } else {
            incorrectAttempts++;

            Game.getInstance().addMisspelledWord(this.currentWord);
            availableLetters.addAll(playerArrangement);
            playerArrangement.clear();
            Collections.shuffle(availableLetters);
            return false;
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getCurrentWord() {
        return currentWord;
    }

    public List<Character> getPlayerArrangement() {
        return Collections.unmodifiableList(playerArrangement);
    }

    public List<Character> getAvailableLetters() {
        return Collections.unmodifiableList(availableLetters);
    }

    public int getWordsCompleted() {
        return wordsCompleted;
    }

    public int getTotalWords() {
        return TOTAL_WORDS;
    }

    public int getLadderRungs() {
        return TOTAL_WORDS;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public double getProgress() {
        return (double) wordsCompleted / TOTAL_WORDS;
    }

    public boolean isComplete() {
        return wordsCompleted >= TOTAL_WORDS;
    }
}
