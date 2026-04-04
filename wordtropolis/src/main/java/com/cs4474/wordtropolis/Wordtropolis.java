package com.cs4474.wordtropolis;

import com.cs4474.wordtropolis.model.Game;
import com.cs4474.wordtropolis.view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    
    // ── Global volume controls ────────────────────────────────────────────────
    private static JPanel volumePanel;
    private static JLabel volumeIcon;
    private static JSlider volumeSlider;
    private static ImageIcon speakerImg;
    private static ImageIcon muteImg;

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
        
        // Use a JLayeredPane to overlay volume control on top of everything
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(920, 700));
        
        cardPanel.setBounds(0, 0, 920, 700);
        layeredPane.add(cardPanel, JLayeredPane.DEFAULT_LAYER);

        // ── Register every screen ─────────────────────────────────────────────
        register(new StartPagePanel(),    SCREEN_START);
        register(new TeacherPagePanel(),  SCREEN_TEACHER);
        register(new StudentPagePanel(),  SCREEN_STUDENT);
        register(new PickHeroPanel(),     SCREEN_HERO_PICK);
        register(new MapPagePanel(),      SCREEN_MAP);
        register(new CatGamePanel(),      SCREEN_CAT_GAME);
        register(new PlaceholderPanel("Fire Rescue\nComing Soon!", new Color(0xFF6B35)), SCREEN_FIRE_GAME);
        register(new BrokenBridgeGamePanel(), SCREEN_BRIDGE_GAME);
        register(new BurglarGamePanel(),  SCREEN_BURGLAR_GAME);
        register(new PlaceholderPanel("Final Boss Fight\nComing Soon!", new Color(0xC0392B)), SCREEN_BOSS_GAME);
        register(new FinishPanel(),       SCREEN_FINISH);

        // ── (Cat Game) ──────────────────────────────────────────────
        register(new CatGamePanel(), SCREEN_CAT_GAME);

        // ── Teammates' panels  (replace PlaceholderPanel when ready) ─────────
        register(new PlaceholderPanel("Fire Rescue\nComing Soon!",
                new Color(0xFF6B35)), SCREEN_FIRE_GAME);

        register(new BrokenBridgeGamePanel(), SCREEN_BRIDGE_GAME);

        //Added Robber game
        register(new BurglarGamePanel(), SCREEN_BURGLAR_GAME);

        //Added Boss game
        register(new BossGamePanel(), SCREEN_BOSS_GAME);

        register(new FinishPanel(), SCREEN_FINISH);

        // ─────────────────────────────────────────────────────────────────────

        mainFrame.add(cardPanel);
        // ── Global Volume Control (added to layered pane on top) ───────────────
        setupGlobalVolumeControl();
        layeredPane.add(volumePanel, JLayeredPane.PALETTE_LAYER);

        mainFrame.add(layeredPane);
        mainFrame.setVisible(true);
        showScreen(SCREEN_START);
    }
    
    // ── Global Volume Control Setup ───────────────────────────────────────────
    
 private static void setupGlobalVolumeControl() {
    // panel for volume control
    volumePanel = new JPanel(null);
    volumePanel.setOpaque(false);
    volumePanel.setBounds(830, 520, 100, 180);
    
    
    try {
        Image speakerRaw = new ImageIcon(Wordtropolis.class.getResource("/images/general/speaker.png")).getImage();
        Image speakerScaled = speakerRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        speakerImg = new ImageIcon(speakerScaled);

        Image muteRaw = new ImageIcon(Wordtropolis.class.getResource("/images/general/nospeaker.png")).getImage();
        Image muteScaled = muteRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        muteImg = new ImageIcon(muteScaled);
    } catch (Exception e) {
        System.out.println("Could not load speaker images");
        speakerImg = null;
        muteImg = null;
    }

    volumeIcon = new JLabel(speakerImg != null ? speakerImg : new ImageIcon());
    volumeIcon.setBounds(10, 70, 40, 40);
    volumeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    volumePanel.add(volumeIcon);

   
    volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
    volumeSlider.setBounds(48, -10, 25, 120); 
    volumeSlider.setOpaque(false);
    volumeSlider.setForeground(new Color(0xECCB2D));
    volumeSlider.setVisible(false);
    volumePanel.add(volumeSlider);

    volumeIcon.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            volumeSlider.setVisible(!volumeSlider.isVisible());
            volumePanel.repaint();
        }
    });

    volumeSlider.addChangeListener(e -> {
        int value = volumeSlider.getValue();
        if (value == 0 && muteImg != null) {
            volumeIcon.setIcon(muteImg);
        } else if (speakerImg != null) {
            volumeIcon.setIcon(speakerImg);
        }
       
    });
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