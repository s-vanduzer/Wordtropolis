package com.cs4474.wordtropolis.model;
import java.util.*;

/**
 *  CatGameModel.java - Hero vs Villain Battle Version
 */
public class BossGameModel {

    // ── Difficulty / Phase ────────────────────────────────────────────────────
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum BattlePhase { PHASE_1A, PHASE_1B, PHASE_1C }
    
    public enum TypeResult {
        TYPED_OK,
        TYPED_NOT_AVAILABLE,
        TYPED_FULL
    }

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final int TOTAL_WORDS_PHASE_1A = 8;
    public static final int TOTAL_WORDS_PHASE_1C = 5;
    public static final int MAYOR_MAX_HEALTH = 100;
    public static final int HERO_MAX_HEALTH = 100;
    public static final int SHIELD_MAX_HEALTH = 60;
    public static final int DAMAGE_PER_CORRECT = 15;
    public static final int HERO_DAMAGE_PER_INCORRECT = 10;

    private static final char[] DISTRACTOR_POOL = "XQZVWBJK".toCharArray();

    // ── State ─────────────────────────────────────────────────────────────────
    private List<String>    phase1AWords;       // main word list
    private List<String>    phase1BWords;       // misspelled words list
    private List<String>    phase1CWords;       // harder words list
    private List<String>    currentWordPool;
    private String          currentWord;
    private List<Character> playerArrangement;
    private List<Character> availableLetters;
    private List<String> timeoutWords; 

    // Combat stats
    private int         heroHealth;
    private int         mayorHealth;
    private int         mayorShield;
    private BattlePhase currentPhase;
    private int         wordsCompletedInPhase;
    private int         totalScore;
    private int         incorrectAttempts;
    private Difficulty  currentDifficulty;
    
    private boolean isHintActive = false;
private int hintProgress = 0;
private boolean autoCompleted = false;
    
    // Tracking
    private List<String> misspelledDuringGame;
    private Random random;

    // ── Constructor ───────────────────────────────────────────────────────────
    public BossGameModel(List<String> providedWords, List<String> misspelledWords) {
        random = new Random();
        misspelledDuringGame = new ArrayList<>();
        timeoutWords = new ArrayList<>();
        
        // Initialize word lists
        initializeWordLists(providedWords);
        //STart at phase 1
        startPhase(BattlePhase.PHASE_1A);
        
        // Initialize combat stats
        heroHealth = HERO_MAX_HEALTH;
        mayorHealth = MAYOR_MAX_HEALTH;
        mayorShield = SHIELD_MAX_HEALTH;
        currentPhase = BattlePhase.PHASE_1A;
        wordsCompletedInPhase = 0;
        totalScore = 0;
        incorrectAttempts = 0;
        currentDifficulty = Difficulty.EASY;
        
        // Start the game
        loadNextWord();
    }
    

private void initializeWordLists(List<String> providedWords) {
    // 1. Create a clean list (No spaces, Uppercase, No duplicates)
    List<String> clean = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();
    
    for (String w : providedWords) {
        if (w == null) continue;
        String upper = w.trim().toUpperCase();
        // Regex [A-Z]+ ensures we only take words with letters
        if (!upper.isEmpty() && upper.matches("[A-Z]+") && seen.add(upper)) {
            clean.add(upper);
        }
    }

    // 2. Phase 1A: Use the CLEAN list
    this.phase1AWords = new ArrayList<>(clean);
    // Shuffle first so same-length words appear in a random order
    Collections.shuffle(this.phase1AWords);
    // Then sort so the game starts easy (short words) and gets harder
    this.phase1AWords.sort(Comparator.comparingInt(String::length));

    // 3. Phase 1B: Use clean teacher words as a backup if no mistakes exist yet
    this.phase1BWords = new ArrayList<>(Game.getInstance().getMisspelledWordList());
    if (this.phase1BWords.isEmpty()) {
        this.phase1BWords.addAll(clean);
        Collections.shuffle(this.phase1BWords);
    }

    // 4. Phase 1C: Take the hardest (longest) words from the teacher
    this.phase1CWords = new ArrayList<>(clean);
    this.phase1CWords.sort((a, b) -> Integer.compare(b.length(), a.length()));
}
 


    private void startPhase(BattlePhase phase) {
    this.currentPhase = phase;
    this.wordsCompletedInPhase = 0;

    switch (phase) {
        case PHASE_1A:
            currentWordPool = new ArrayList<>(phase1AWords);
            break;
        case PHASE_1B:
            // Refresh misspelled words one last time in case new ones were added during 1A
            List<String> mistakes = Game.getInstance().getMisspelledWordList();
            currentWordPool = mistakes.isEmpty() ? new ArrayList<>(phase1AWords) : new ArrayList<>(mistakes);
            break;
        case PHASE_1C:
            currentWordPool = new ArrayList<>(phase1CWords);
            break;
    }
    
    if (currentWordPool.isEmpty()) {
        currentWordPool.addAll(phase1AWords); // Ultimate fallback to teacher list
    }
    
    Collections.shuffle(currentWordPool);
    loadNextWord();
}


    
    private List<String> cleanWordList(List<String> words) {
        List<String> clean = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String w : words) {
            String cleaned = cleanWord(w);
            if (cleaned != null && seen.add(cleaned)) {
                clean.add(cleaned);
            }
        }
        return clean;
    }
    
    private String cleanWord(String word) {
        if (word == null) return null;
        String upper = word.trim().toUpperCase();
        if (!upper.isEmpty() && upper.matches("[A-Z]+")) {
            return upper;
        }
        return null;
    }
    
    
    // Add this method to handle timeout words
    public void addTimeoutWord(String word) {
        if (word != null && !word.isEmpty()) {
            String cleaned = cleanWord(word);
            if (cleaned != null && !timeoutWords.contains(cleaned)) {
                timeoutWords.add(cleaned);
                System.out.println("[CatGame] Timeout word added to Phase 1B pool: " + cleaned);

                // Also add to phase1BWords for immediate use
                if (!phase1BWords.contains(cleaned)) {
                    phase1BWords.add(cleaned);
                }
            }
        }
    }

    // ── Word loading ──────────────────────────────────────────────────────────
    public boolean loadNextWord() {
        if (currentWordPool.isEmpty()) {
            return false;
        }
        
        currentWord = currentWordPool.remove(0);
        availableLetters = buildAvailableLetters(currentWord, currentDifficulty);
        playerArrangement = new ArrayList<>();
        updateDifficulty();
        return true;
    }
    
    
    // Update refreshForPhase to include timeout words and proper phase setup
public void refreshForPhase(BattlePhase newPhase) {
    currentPhase = newPhase;
    wordsCompletedInPhase = 0;
    
    System.out.println("[BossGame] === REFRESHING FOR PHASE " + newPhase + " ===");
    
    switch (newPhase) {
        case PHASE_1A:
            currentWordPool = new ArrayList<>(phase1AWords);
            System.out.println("[BossGame] Phase 1A loaded with " + currentWordPool.size() + " words");
            break;
            
        case PHASE_1B:
            // Combine misspelled words AND timeout words
            Set<String> allReviewWords = new LinkedHashSet<>();
            allReviewWords.addAll(phase1BWords);           // Existing misspelled
            allReviewWords.addAll(timeoutWords);           // Words that timed out
            allReviewWords.addAll(misspelledDuringGame);   // Words misspelled during this session
            
            currentWordPool = new ArrayList<>(allReviewWords);
            
            // If still empty, add defaults
            if (currentWordPool.isEmpty()) {
                currentWordPool.addAll(Arrays.asList("BUILD", "LEVEL", "GRADE", "MAYOR", "FIGHT"));
                System.out.println("[BossGame] Phase 1B using default words");
            }
            
            System.out.println("[BossGame] Phase 1B loaded with " + currentWordPool.size() + 
                               " words (misspelled: " + misspelledDuringGame.size() + 
                               ", timeout: " + timeoutWords.size() + ")");
            break;
            
        case PHASE_1C:
            currentWordPool = new ArrayList<>(phase1CWords);
            System.out.println("[BossGame] Phase 1C loaded with " + currentWordPool.size() + " words");
            break;
    }
    
    Collections.shuffle(currentWordPool);
    loadNextWord();
    debugBattleState(); // Print battle state after refresh
}
//    
    private List<Character> buildAvailableLetters(String word, Difficulty diff) {
        List<Character> letters = new ArrayList<>();
        for (char c : word.toCharArray()) letters.add(c);

        int distractors = (diff == Difficulty.MEDIUM) ? 1
                        : (diff == Difficulty.HARD)   ? 2 : 0;
        
        for (int i = 0; i < distractors; i++) {
            letters.add(DISTRACTOR_POOL[random.nextInt(DISTRACTOR_POOL.length)]);
        }

        int tries = 0;
        do { Collections.shuffle(letters); tries++; }
        while (tries < 20 && spellsOriginal(letters, word));

        return letters;
    }

    private boolean spellsOriginal(List<Character> list, String word) {
        if (list.size() < word.length()) return false;
        for (int i = 0; i < word.length(); i++)
            if (list.get(i) != word.charAt(i)) return false;
        return true;
    }

    private void updateDifficulty() {
        if (wordsCompletedInPhase < 2) currentDifficulty = Difficulty.EASY;
        else if (wordsCompletedInPhase < 4) currentDifficulty = Difficulty.MEDIUM;
        else currentDifficulty = Difficulty.HARD;
    }

    // ── Tile management ───────────────────────────────────────────────────────
    public boolean selectLetter(int availableIndex) {
        if (availableIndex < 0 || availableIndex >= availableLetters.size()) return false;
        playerArrangement.add(availableLetters.remove(availableIndex));
        return true;
    }

    public boolean removeLetter(int arrangementIndex) {
        if (arrangementIndex < 0 || arrangementIndex >= playerArrangement.size()) return false;
        availableLetters.add(playerArrangement.remove(arrangementIndex));
        return true;
    }

    public void clearArrangement() {
        availableLetters.addAll(playerArrangement);
        playerArrangement.clear();
        Collections.shuffle(availableLetters);
    }

    public void swapArrangement(int i, int j) {
        if (i < 0 || j < 0 || i >= playerArrangement.size() || j >= playerArrangement.size()) return;
        char tmp = playerArrangement.get(i);
        playerArrangement.set(i, playerArrangement.get(j));
        playerArrangement.set(j, tmp);
    }

    // ── Keyboard input ────────────────────────────────────────────────────────
    public TypeResult typeKey(char key) {
        char upper = Character.toUpperCase(key);
        if (!Character.isLetter(upper)) return TypeResult.TYPED_NOT_AVAILABLE;
        if (playerArrangement.size() >= currentWord.length()) {
            return TypeResult.TYPED_FULL;
        }

        for (int i = 0; i < availableLetters.size(); i++) {
            if (availableLetters.get(i) == upper) {
                playerArrangement.add(availableLetters.remove(i));
                return TypeResult.TYPED_OK;
            }
        }
        return TypeResult.TYPED_NOT_AVAILABLE;
    }

    public boolean backspace() {
        if (playerArrangement.isEmpty()) return false;
        return removeLetter(playerArrangement.size() - 1);
    }

    // ── Combat & Answer checking ──────────────────────────────────────────────
    
// Add these methods to BossGameModel
public void startHintMode() {
    if (!isHintActive && currentWord != null) {
        isHintActive = true;
        hintProgress = 0;
        autoCompleted = false;
        System.out.println("[BossGame] Hint mode activated for word: " + currentWord);
    }
}

public void updateHint() {
    if (!isHintActive || autoCompleted) return;
    
    // Calculate how many letters to fill based on time left
    // As time runs out, more letters get auto-filled
    int totalLetters = currentWord.length();
    int currentFilled = playerArrangement.size();
    
    // Fill one more letter if we're behind schedule
    if (currentFilled < totalLetters && hintProgress < totalLetters) {
        // Get the next letter that should be placed
        char nextLetter = currentWord.charAt(currentFilled);
        
        // Find this letter in available letters
        for (int i = 0; i < availableLetters.size(); i++) {
            if (availableLetters.get(i) == nextLetter) {
                // Auto-place the letter
                playerArrangement.add(availableLetters.remove(i));
                hintProgress++;
                System.out.println("[BossGame] Hint auto-placed letter: " + nextLetter + 
                                   " (" + hintProgress + "/" + totalLetters + ")");
                break;
            }
        }
    }
    
    // Check if word is now fully completed by hints
    if (playerArrangement.size() >= totalLetters && !autoCompleted) {
        autoCompleted = true;
        System.out.println("[BossGame] Word auto-completed by hints! Will count as incorrect.");
    }
}

public boolean isWordAutoCompleted() {
    return autoCompleted;
}

public void resetHintMode() {
    isHintActive = false;
    hintProgress = 0;
    autoCompleted = false;
}

public boolean isHintModeActive() {
    return isHintActive;
}

// Update the submitAnswer method to handle auto-completed words
public AnswerResult submitAnswer() {
    StringBuilder sb = new StringBuilder();
    for (char c : playerArrangement) sb.append(c);
    String answer = sb.toString();
    
    boolean isCorrect = answer.equals(currentWord);
    int damageDealt = 0;
    int damageTaken = 0;
    boolean phaseTransition = false;
    BattlePhase newPhase = currentPhase;
    boolean isVictory = false;
    
    // If word was auto-completed by hints, mark as incorrect
    if (autoCompleted && !isCorrect) {
        isCorrect = false;
        System.out.println("[BossGame] Word was auto-completed by hints - marking as incorrect");
    }
    
    if (isCorrect) {
        // CORRECT ANSWER - Deal damage based on phase
        totalScore += 50;
        Game.getInstance().addScore(50);
        
        switch (currentPhase) {
            case PHASE_1A:
                damageDealt = Math.min(DAMAGE_PER_CORRECT, mayorHealth);
                mayorHealth -= DAMAGE_PER_CORRECT;
                wordsCompletedInPhase++;
                
                if (mayorHealth <= MAYOR_MAX_HEALTH / 2) {
                    phaseTransition = true;
                    newPhase = BattlePhase.PHASE_1B;
                    mayorShield = SHIELD_MAX_HEALTH;
                    System.out.println("[BossGame] Transitioning to PHASE_1B - Shield activated!");
                }
                break;
                
            case PHASE_1B:
                damageDealt = Math.min(DAMAGE_PER_CORRECT, mayorShield);
                mayorShield -= DAMAGE_PER_CORRECT;
                wordsCompletedInPhase++;
                
                System.out.println("[BossGame] Phase 1B - Shield: " + mayorShield + "/" + SHIELD_MAX_HEALTH);
                
                if (mayorShield <= 0) {
                    phaseTransition = true;
                    newPhase = BattlePhase.PHASE_1C;
                    System.out.println("[BossGame] Transitioning to PHASE_1C - Shield broken!");
                }
                break;
                
            case PHASE_1C:
                damageDealt = Math.min(DAMAGE_PER_CORRECT, mayorHealth);
                mayorHealth -= DAMAGE_PER_CORRECT;
                wordsCompletedInPhase++;
                
                System.out.println("[BossGame] Phase 1C - Mayor Health: " + mayorHealth + "/" + MAYOR_MAX_HEALTH);
                
                if (mayorHealth <= 0) {
                    isVictory = true;
                    System.out.println("[BossGame] VICTORY! Mayor defeated!");
                }
                break;
        }
    } else {
        // INCORRECT ANSWER - Hero takes damage
        damageTaken = HERO_DAMAGE_PER_INCORRECT;
        heroHealth = Math.max(0, heroHealth - damageTaken);
        incorrectAttempts++;
        Game.getInstance().addMisspelledWord(answer);
        
        System.out.println("[BossGame] Incorrect! Hero health: " + heroHealth + "/" + HERO_MAX_HEALTH);
        
        // Store misspelled word for Phase 1B
        if (!misspelledDuringGame.contains(currentWord)) {
            misspelledDuringGame.add(currentWord);
            if (!phase1BWords.contains(currentWord)) {
                phase1BWords.add(currentWord);
            }
            System.out.println("[BossGame] Misspelled word added to Phase 1B: " + answer);
        }
        
        // Return letters to pool
        availableLetters.addAll(playerArrangement);
        playerArrangement.clear();
        Collections.shuffle(availableLetters);
        
        if (heroHealth <= 0) {
            return new AnswerResult(false, 0, damageTaken, true, false, currentPhase, "DEFEAT");
        }
    }
    
    // Reset hint mode for next word
    resetHintMode();
    
    // Handle phase transition
    if (phaseTransition) {
        refreshForPhase(newPhase);
        return new AnswerResult(isCorrect, damageDealt, damageTaken, false, false, newPhase, 
            newPhase == BattlePhase.PHASE_1B ? "SHIELD_ACTIVATED" : "SHIELD_BROKEN");
    }
    
    // Check for victory
    if (isVictory) {
        totalScore += 200;
        Game.getInstance().addScore(200);
        return new AnswerResult(isCorrect, damageDealt, damageTaken, false, true, currentPhase, "VICTORY");
    }
    
    return new AnswerResult(isCorrect, damageDealt, damageTaken, false, false, currentPhase, 
        isCorrect ? "HIT" : "MISS");
}

// Update advanceToNextWord to reset hint mode
public void advanceToNextWord() {
    resetHintMode();
    loadNextWord();
}
    
    // Answer result class
    public static class AnswerResult {
        public final boolean correct;
        public final int damageDealt;
        public final int damageTaken;
        public final boolean heroDied;
        public final boolean victory;
        public final BattlePhase newPhase;
        public final String effect;
        
        public AnswerResult(boolean correct, int damageDealt, int damageTaken, 
                           boolean heroDied, boolean victory, BattlePhase newPhase, String effect) {
            this.correct = correct;
            this.damageDealt = damageDealt;
            this.damageTaken = damageTaken;
            this.heroDied = heroDied;
            this.victory = victory;
            this.newPhase = newPhase;
            this.effect = effect;
        }
    }

    
private boolean isRunning = true;

public void stopGame() {
    this.isRunning = false;
    // Clear any temporary word pools to free memory
    if (currentWordPool != null) currentWordPool.clear();
    System.out.println("Boss Game Model stopped.");
}

public boolean isRunning() {
    return isRunning;
}
    
    
    // ── Getters ───────────────────────────────────────────────────────────────
    public String getCurrentWord() { return currentWord; }
    public List<Character> getPlayerArrangement() { return Collections.unmodifiableList(playerArrangement); }
    public List<Character> getAvailableLetters() { return Collections.unmodifiableList(availableLetters); }
    public int getHeroHealth() { return heroHealth; }
    public int getMayorHealth() { return mayorHealth; }
    public int getMayorShield() { return mayorShield; }
    public int getHeroMaxHealth() { return HERO_MAX_HEALTH; }
    public int getMayorMaxHealth() { return MAYOR_MAX_HEALTH; }
    public int getShieldMaxHealth() { return SHIELD_MAX_HEALTH; }
    public BattlePhase getCurrentPhase() { return currentPhase; }
    public int getWordsCompletedInPhase() { return wordsCompletedInPhase; }
    public int getTotalScore() { return totalScore; }
    public int getIncorrectAttempts() { return incorrectAttempts; }
    public Difficulty getCurrentDifficulty() { return currentDifficulty; }
    public boolean isHeroDead() { return heroHealth <= 0; }
    // Replace the isComplete() method with this:
public boolean isComplete() { 
    // Victory only happens in Phase 1C when mayor health reaches 0
    return currentPhase == BattlePhase.PHASE_1C && mayorHealth <= 0; 
}

// Add a method to check if shield is broken
public boolean isShieldBroken() {
    return mayorShield <= 0;
}

// Add method to check if phase transition should happen from 1A to 1B
public boolean shouldTransitionToPhase1B() {
    return currentPhase == BattlePhase.PHASE_1A && mayorHealth <= MAYOR_MAX_HEALTH / 2;
}

// Add method to check if phase transition should happen from 1B to 1C
public boolean shouldTransitionToPhase1C() {
    return currentPhase == BattlePhase.PHASE_1B && mayorShield <= 0;
}


// Add this debug method to see current battle state
public void debugBattleState() {
    System.out.println("\n╔════════════════════════════════════════════════════════════╗");
    System.out.println("║                   BATTLE STATE DEBUG                      ║");
    System.out.println("╠════════════════════════════════════════════════════════════╣");
    System.out.printf("║ Phase: %-46s║\n", currentPhase);
    System.out.printf("║ Hero Health: %d/%-36d║\n", heroHealth, HERO_MAX_HEALTH);
    System.out.printf("║ Mayor Health: %d/%-36d║\n", mayorHealth, MAYOR_MAX_HEALTH);
    System.out.printf("║ Mayor Shield: %d/%-36d║\n", mayorShield, SHIELD_MAX_HEALTH);
    System.out.printf("║ Words in Phase: %d/%-36s║\n", wordsCompletedInPhase, 
        currentPhase == BattlePhase.PHASE_1A ? "8" : (currentPhase == BattlePhase.PHASE_1B ? "?" : "5"));
    System.out.printf("║ Total Score: %-39d║\n", totalScore);
    System.out.println("╚════════════════════════════════════════════════════════════╝\n");
}
}