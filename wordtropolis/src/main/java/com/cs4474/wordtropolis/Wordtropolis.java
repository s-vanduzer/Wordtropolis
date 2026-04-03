package com.cs4474.wordtropolis;

import com.cs4474.wordtropolis.model.Game;
import com.cs4474.wordtropolis.view.*;

import javax.swing.*;
import java.awt.*;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  Wordtropolis  –  Save the City!
 *  CS 4474 Group Project
 *
 *  Entry point and screen router.
 *  Uses a single JFrame with CardLayout to swap between panels.
 *
 *  Run this class in NetBeans (F6 / right-click → Run File).
 * ╚══════════════════════════════════════════════════════════╝
 */
public class Wordtropolis {

    // ── Screen name constants (used everywhere to navigate) ───────────────────
    public static final String SCREEN_START        = "START";
    public static final String SCREEN_TEACHER      = "TEACHER";
    public static final String SCREEN_STUDENT      = "STUDENT";
    public static final String SCREEN_HERO_PICK    = "HERO_PICK";
    public static final String SCREEN_MAP          = "MAP";
    public static final String SCREEN_CAT_GAME     = "CAT_GAME";
    public static final String SCREEN_FIRE_GAME    = "FIRE_GAME";
    public static final String SCREEN_BRIDGE_GAME  = "BRIDGE_GAME";
    public static final String SCREEN_BURGLAR_GAME = "BURGLAR_GAME";
    public static final String SCREEN_BOSS_GAME    = "BOSS_GAME";
    public static final String SCREEN_FINISH       = "FINISH";

    // ── Swing state ───────────────────────────────────────────────────────────
    private static JFrame     mainFrame;
    private static CardLayout cardLayout;
    private static JPanel     cardPanel;

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Always create/modify Swing components on the Event Dispatch Thread
        SwingUtilities.invokeLater(Wordtropolis::initAndShow);
    }

    // ── Initialise ────────────────────────────────────────────────────────────

    private static void initAndShow() {
        // Enable anti-aliased text system-wide
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        
        Game game = Game.getInstance();
        game.setDifficulty(Game.Difficulty.HARD);   // change to MEDIUM / HARD
        game.loadWordsForDifficulty();

        mainFrame = new JFrame("Wordtropolis \u2013 Save the City!");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(920, 700);
        mainFrame.setMinimumSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(Color.BLACK);
        

        // ── Register every screen ─────────────────────────────────────────────
        // NOTE TO TEAM:
        //   Replace any PlaceholderPanel below with your real activity panel.
        //   Make sure your panel is in the com.cs4474.wordtropolis.view package.
        // ─────────────────────────────────────────────────────────────────────

        register(new StartPagePanel(),    SCREEN_START);
        register(new TeacherPagePanel(),  SCREEN_TEACHER);
        register(new StudentPagePanel(),  SCREEN_STUDENT);
        register(new PickHeroPanel(),     SCREEN_HERO_PICK);
        register(new MapPagePanel(),      SCREEN_MAP);

        // ── (Cat Game) ──────────────────────────────────────────────
        register(new CatGamePanel(), SCREEN_CAT_GAME);

        // ── Teammates' panels  (replace PlaceholderPanel when ready) ─────────
        register(new PlaceholderPanel("Fire Rescue\nComing Soon!",
                new Color(0xFF6B35)), SCREEN_FIRE_GAME);

        register(new PlaceholderPanel("Fix the Bridge\nComing Soon!",
                new Color(0x118AB2)), SCREEN_BRIDGE_GAME);

        //Added Robber game
        register(new BurglarGamePanel(), SCREEN_BURGLAR_GAME);

        //Added Boss game
        register(new BossGamePanel(), SCREEN_BOSS_GAME);

        register(new FinishPanel(), SCREEN_FINISH);

        // ─────────────────────────────────────────────────────────────────────

        mainFrame.add(cardPanel);
        mainFrame.setVisible(true);
        showScreen(SCREEN_START);
    }

    // ── Navigation API ────────────────────────────────────────────────────────

    /**
     * Navigate to the named screen.
     * If the panel implements Refreshable its refresh() method is called automatically.
     */
    public static void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof Refreshable) {
                ((Refreshable) comp).refresh();
            }
        }
    }

    
    
    /**
     * Replace a panel entirely and navigate to it.
     * Used by MapPagePanel to rebuild CatGamePanel with a fresh model on each play.
     *
     * Example:
     *   Wordtropolis.replaceScreen(SCREEN_CAT_GAME, new CatGamePanel());
     */
    public static void replaceScreen(String screenName, JPanel newPanel) {
        // Remove the old panel that has this name
        for (Component comp : cardPanel.getComponents()) {
            if (screenName.equals(comp.getName())) {
                cardPanel.remove(comp);
                break;
            }
        }
        register(newPanel, screenName);
        cardLayout.show(cardPanel, screenName);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /** Add a panel to the card deck and tag it with its key as the component name. */
    private static void register(JPanel panel, String key) {
        panel.setName(key);
        cardPanel.add(panel, key);
    }

    public static JFrame getMainFrame() { return mainFrame; }
}