package com.cs4474.wordtropolis.model;

import java.util.*;

/**
 * BurglarGameModel.java
 * Manages word queue, letter pool, and game state for the burglar chase game.
 * The player must fill in missing letters in words to advance the hero.
 */
public class BurglarGameModel {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final int TOTAL_WORDS        = 500;
    public static final int POINTS_PER_CORRECT = 50;
    public static final int PENALTY_INCORRECT  = 10;
    public static final int HERO_START_POS     = 0;      // Hero starts at left (0)
    public static final int ROBBER_START_POS   = 10;     // Robber starts at right (10)
    public static final int WIN_POSITION       = 10;     // Hero wins by reaching right side
    public static final int LOSE_POSITION      = 0;      // Robber wins by reaching left side

    private static final char[] DISTRACTOR_POOL = "XQZVWBJK".toCharArray();

    // ── Word Question Structure ───────────────────────────────────────────────
    public static class WordQuestion {
        public final String fullWord;
        public final String displayWord;    // Word with underscores for missing letters
        public final String missingLetters; // The letters that go in blanks
        public final int[] blankPositions;  // Positions of blanks in displayWord
        
        public WordQuestion(String fullWord, String displayWord, 
                           String missingLetters, int[] blankPositions) {
            this.fullWord = fullWord;
            this.displayWord = displayWord;
            this.missingLetters = missingLetters;
            this.blankPositions = blankPositions;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private List<WordQuestion> wordQueue;
    private WordQuestion currentQuestion;
    private List<Character> playerArrangement;  // Letters placed in blanks (in order)
    private List<Character> availableLetters;   // Letters the player can choose from
    
    private int wordsCompleted;
    private int incorrectAttempts;
    private int totalScore;
    private int heroPosition;
    private int robberPosition;
    private boolean gameActive;
    
    // ── Constructor ───────────────────────────────────────────────────────────
    
    public BurglarGameModel(List<String> providedWords) {
        // 1. Create a copy of the list to avoid modifying the original source
        List<String> shuffledWords = new ArrayList<>(providedWords);

        // 2. Shuffle the copy randomly
        Collections.shuffle(shuffledWords);

        // 3. Build the queue from the now-shuffled list
        wordQueue = buildWordQueue(shuffledWords);
        wordsCompleted = 0;
        incorrectAttempts = 0;
        totalScore = 0;
        heroPosition = HERO_START_POS;
        robberPosition = ROBBER_START_POS;
        gameActive = true;
        loadNextWord();
    }
    
    // ── Word Queue Building ───────────────────────────────────────────────────
    
    /**
     * Build the ordered word queue from the provided list.
     * Converts each word into a WordQuestion with missing letters.
     */
    private List<WordQuestion> buildWordQueue(List<String> provided) {
        // Step 1: Clean, uppercase, deduplicate
        List<String> clean = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String w : provided) {
            if (w == null) continue;
            String upper = w.trim().toUpperCase();
            if (!upper.isEmpty() && upper.matches("[A-Z]+") && seen.add(upper)) {
                clean.add(upper);
            }
        }
        
        // Emergency fallback
        if (clean.isEmpty()) {
            System.out.println("[BurglarGameModel] WARNING: word list empty, using emergency defaults.");
            clean = new ArrayList<>(Arrays.asList(
                    "CAT", "DOG", "SUN", "MOON", "STAR", "FISH", "BIRD", "TREE", "PIZZA", "DARE", "CHAT","APPLE"));
        }
        
        // Step 2: Sort shortest first
        clean.sort(Comparator.comparingInt(String::length));
        
//        // Step 3: Cap at TOTAL_WORDS
//        if (clean.size() > TOTAL_WORDS) {
//            clean = clean.subList(0, TOTAL_WORDS);
//        }
        
        // Step 4: Pad by cycling if fewer than TOTAL_WORDS
        List<String> base = new ArrayList<>(clean);
        int i = 0;
        while (clean.size() < TOTAL_WORDS) {
            clean.add(base.get(i++ % base.size()));
        }
        
        // Step 5: Convert each word to a WordQuestion with missing letters
        List<WordQuestion> questions = new ArrayList<>();
        Random rand = new Random();
        for (String word : clean) {
            questions.add(createWordQuestion(word, rand));
        }
        
        return questions;
    }
    
    /**
     * Create a WordQuestion from a full word.
     * Randomly chooses which letters to hide (2-3 letters for short words, more for longer).
     */
    private WordQuestion createWordQuestion(String word, Random rand) {
        int wordLen = word.length();
        // Determine how many letters to hide
        int hideCount;
        if (wordLen <= 3) hideCount = 1;
        else if (wordLen <= 5) hideCount = 2;
        else if (wordLen <= 7) hideCount = 3;
        else hideCount = 4;
        
        // Choose random positions to hide (avoid first and last sometimes for readability)
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < wordLen; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, rand);
        
        int[] blankPositions = new int[hideCount];
        StringBuilder missingLetters = new StringBuilder();
        StringBuilder displayWord = new StringBuilder();
        
        // Mark positions as hidden
        boolean[] hidden = new boolean[wordLen];
        for (int i = 0; i < hideCount; i++) {
            blankPositions[i] = positions.get(i);
            hidden[blankPositions[i]] = true;
            missingLetters.append(word.charAt(blankPositions[i]));
        }
        
        // Build display word
        for (int i = 0; i < wordLen; i++) {
            if (hidden[i]) {
                displayWord.append('_');
            } else {
                displayWord.append(word.charAt(i));
            }
        }
        
        // Sort missing letters alphabetically for easier comparison
        char[] missingArr = missingLetters.toString().toCharArray();
        Arrays.sort(missingArr);
        
        return new WordQuestion(word, displayWord.toString(), 
                               new String(missingArr), blankPositions);
    }
    
    // ── Word Loading ──────────────────────────────────────────────────────────
    
    /** Advance to the next word. Returns false when all words are done. */
    public boolean loadNextWord() {
        if (wordQueue.isEmpty()) return false;
        currentQuestion = wordQueue.remove(0);
        availableLetters = buildAvailableLetters(currentQuestion);
        playerArrangement = new ArrayList<>();
        return true;
    }
    
    /**
     * Build the available letters pool.
     * Includes the missing letters plus random distractors.
     */
    private List<Character> buildAvailableLetters(WordQuestion question) {
        List<Character> letters = new ArrayList<>();
        
        // Add missing letters
        for (char c : question.missingLetters.toCharArray()) {
            letters.add(c);
        }
        
        // Add random distractors to make 5 total options
        Random rand = new Random();
        int needed = 5 - letters.size();
        for (int i = 0; i < needed; i++) {
            letters.add(DISTRACTOR_POOL[rand.nextInt(DISTRACTOR_POOL.length)]);
        }
        
        // Shuffle
        Collections.shuffle(letters);
        return letters;
    }
    
    // ── Tile Actions ──────────────────────────────────────────────────────────
    
    /**
     * Select a letter from available pool.
     * Called when player clicks a floating letter tile.
     */
    public boolean selectLetter(int availableIndex) {
        if (!gameActive) return false;
        if (playerArrangement.size() >= currentQuestion.missingLetters.length()) {
            return false;
        }
        if (availableIndex < 0 || availableIndex >= availableLetters.size()) {
            return false;
        }
        playerArrangement.add(availableLetters.remove(availableIndex));
        return true;
    }
    
    /**
     * Remove a letter from arrangement back to pool.
     */
    public boolean removeLetter(int arrangementIndex) {
        if (!gameActive) return false;
        if (arrangementIndex < 0 || arrangementIndex >= playerArrangement.size()) {
            return false;
        }
        availableLetters.add(playerArrangement.remove(arrangementIndex));
        return true;
    }
    
    /**
     * Clear all placed letters (back to pool).
     */
    public void clearArrangement() {
        availableLetters.addAll(playerArrangement);
        playerArrangement.clear();
        Collections.shuffle(availableLetters);
    }
    
    /**
     * Swap two letters within arrangement.
     */
    public void swapArrangement(int i, int j) {
        if (!gameActive) return;
        if (i < 0 || j < 0 || i >= playerArrangement.size() || j >= playerArrangement.size()) {
            return;
        }
        char tmp = playerArrangement.get(i);
        playerArrangement.set(i, playerArrangement.get(j));
        playerArrangement.set(j, tmp);
    }
    
    // ── Answer Checking ───────────────────────────────────────────────────────
    
/**
 * Check if the arrangement matches the missing letters in the exact order required.
 */
public boolean checkAnswer() {
    // 1. Convert the player's arrangement (List<Character>) into a String
    StringBuilder userInput = new StringBuilder();
    for (Character c : playerArrangement) {
        userInput.append(c);
    }
    String playerSpelled = userInput.toString();

    // 2. Reconstruct the full word as the player spelled it
    // We take the display word (e.g., "_O_N") and fill the blanks with player letters
    StringBuilder finalWordBuilder = new StringBuilder();
    int playerCharIdx = 0;
    String display = currentQuestion.displayWord;

    for (int i = 0; i < display.length(); i++) {
        if (display.charAt(i) == '_') {
            // Fill the blank with the next letter the player picked
            if (playerCharIdx < playerSpelled.length()) {
                finalWordBuilder.append(playerSpelled.charAt(playerCharIdx));
                playerCharIdx++;
            }
        } else {
            // Keep the existing letter (like 'O' or 'N')
            finalWordBuilder.append(display.charAt(i));
        }
    }

    String fullSpelledWord = finalWordBuilder.toString();

    // 3. Compare the full spelled word against the actual target word
    // (Assuming currentQuestion.originalWord holds the correct spelling like "DOWN")
    boolean correct = fullSpelledWord.equalsIgnoreCase(currentQuestion.fullWord);

    if (correct) {
        heroPosition++; 
        wordsCompleted++;
        totalScore += POINTS_PER_CORRECT;
        Game.getInstance().addScore(POINTS_PER_CORRECT);
    } else {
        robberPosition++; 
        incorrectAttempts++;
        // We log what they actually spelled (e.g., "WODN")
        Game.getInstance().addMisspelledWord(fullSpelledWord);
        totalScore = Math.max(0, totalScore - PENALTY_INCORRECT);
    }
    
    return correct;
}
    
public String getCurrentFullWord() {
    return currentQuestion.fullWord; 
}
    
    
    // ── Getters ───────────────────────────────────────────────────────────────
    
    
    public WordQuestion getCurrentQuestion() { return currentQuestion; }
    public List<Character> getPlayerArrangement() { 
        return Collections.unmodifiableList(playerArrangement); 
    }
    public List<Character> getAvailableLetters() { 
        return Collections.unmodifiableList(availableLetters); 
    }
    public int getWordsCompleted() { return wordsCompleted; }
    public int getTotalWords() { return TOTAL_WORDS; }
    public int getIncorrectAttempts() { return incorrectAttempts; }
    public int getTotalScore() { return totalScore; }
    public int getHeroPosition() { return heroPosition; }
    public int getRobberPosition() { return robberPosition; }
    public boolean isGameActive() { return gameActive; }
    public boolean isComplete() { 
    // Hero wins if they reach or pass the robber's current position
    return heroPosition >= robberPosition; 
}
public boolean isFailed() { 
    // Robber wins if they reach the escape point (LOSE_POSITION, usually 0)
    return robberPosition <= LOSE_POSITION; 
}
    
public double getProgress() { 
    // Calculate total distance between their starting points
    int totalInitialDistance = ROBBER_START_POS - HERO_START_POS; // Usually 10
    
    // Calculate the current gap
    int currentGap = robberPosition - heroPosition;
    
    // If the hero catches or passes the robber, progress is 100% (1.0)
    if (currentGap <= 0) return 1.0;
    
    // Progress is based on how much the starting gap has been closed
    double progress = 1.0 - ((double) currentGap / totalInitialDistance);
    
    return Math.max(0.0, Math.min(1.0, progress)); 
}

public void finishGame() {
    this.gameActive = false;
    // Update the global singleton
    Game.getInstance().setBurgularCompleted(true);
    // The score is already added to Game.getInstance() via your handleCorrectAnswer logic
}
    
    // ── Resets ────────────────────────────────────────────────────────────────
    
    public void resetPositions() {
        heroPosition = HERO_START_POS;
        robberPosition = ROBBER_START_POS;
        gameActive = true;
    }
}