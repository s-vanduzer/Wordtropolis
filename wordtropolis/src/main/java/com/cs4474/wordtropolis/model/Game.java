package com.cs4474.wordtropolis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core Game model – singleton holding all shared state across the application.
 * Matches the Game class from the flowchart diagram.
 *
 * Fields: playerName, wordList, avatarPath, misspelledWorldList,
 *         score, currentStage, musicVol, sfxVol
 */
public class Game {

    public enum CurrentStage {
        CAT, BURGULAR, FIRE, BRIDGE, BOSS
    }

    private String playerName;
    private List<String> wordList;
    private String avatarPath;
    private List<String> misspelledWorldList;
    private int score;
    private CurrentStage currentStage;
    private int musicVol;
    private int sfxVol;

    // Activity completion flags
    private boolean catCompleted;
    private boolean burgularCompleted;
    private boolean fireCompleted;
    private boolean bridgeCompleted;

    private static Game instance;

    private Game() {
        wordList             = new ArrayList<>();
        misspelledWorldList  = new ArrayList<>();
        score                = 0;
        musicVol             = 70;
        sfxVol               = 80;
        currentStage         = CurrentStage.CAT;

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
        if (instance == null) instance = new Game();
        return instance;
    }

    /** Full reset – used when the player chooses "Play Again". */
    public static void resetInstance() {
        instance = new Game();
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getPlayerName()                        { return playerName; }
    public void   setPlayerName(String playerName)       { this.playerName = playerName; }

    public List<String> getWordList()                    { return wordList; }
    public void         setWordList(List<String> list)   { this.wordList = list; }

    public String getAvatarPath()                        { return avatarPath; }
    public void   setAvatarPath(String avatarPath)       { this.avatarPath = avatarPath; }

    public List<String> getMisspelledWorldList()         { return misspelledWorldList; }
    public void         addMisspelledWord(String word)   { misspelledWorldList.add(word); }

    public int  getScore()                               { return score; }
    public void addScore(int points)                     { this.score += points; }

    public CurrentStage getCurrentStage()                        { return currentStage; }
    public void         setCurrentStage(CurrentStage s)          { this.currentStage = s; }

    public int  getMusicVol()                            { return musicVol; }
    public void setMusicVol(int v)                       { this.musicVol = v; }

    public int  getSfxVol()                              { return sfxVol; }
    public void setSfxVol(int v)                         { this.sfxVol = v; }

    public boolean isCatCompleted()                      { return catCompleted; }
    public void    setCatCompleted(boolean v)            { this.catCompleted = v; }

    public boolean isBurgularCompleted()                 { return burgularCompleted; }
    public void    setBurgularCompleted(boolean v)       { this.burgularCompleted = v; }

    public boolean isFireCompleted()                     { return fireCompleted; }
    public void    setFireCompleted(boolean v)           { this.fireCompleted = v; }

    public boolean isBridgeCompleted()                   { return bridgeCompleted; }
    public void    setBridgeCompleted(boolean v)         { this.bridgeCompleted = v; }

    public boolean allActivitiesCompleted() {
        return catCompleted && burgularCompleted && fireCompleted && bridgeCompleted;
    }
}
