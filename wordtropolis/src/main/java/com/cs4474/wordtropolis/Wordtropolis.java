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

    // ── Screen name constants ───────────────────────────────────────────────────
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
        SwingUtilities.invokeLater(Wordtropolis::initAndShow);
    }

    // ── Initialise ────────────────────────────────────────────────────────────

    private static void initAndShow() {
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        
        Game game = Game.getInstance();
        game.setDifficulty(Game.Difficulty.HARD);
        game.loadWordsForDifficulty();

        mainFrame = new JFrame("Wordtropolis – Save the City!");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(920, 720);
        mainFrame.setMinimumSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(null); // Use null layout for absolute positioning

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.BLACK);
        cardPanel.setBounds(0, 0, 920, 700);
        mainFrame.add(cardPanel);

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
        register(new BossGamePanel(),     SCREEN_BOSS_GAME);
        register(new FinishPanel(),       SCREEN_FINISH);

        // ── Global Volume Control (added ON TOP of everything using JLayeredPane) ──
        setupGlobalVolumeControl();
        
        // Use JLayeredPane to ensure volume control is on top
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 920, 700);
        
        // Remove cardPanel from mainFrame and add to layeredPane
        mainFrame.remove(cardPanel);
        cardPanel.setBounds(0, 0, 920, 700);
        layeredPane.add(cardPanel, JLayeredPane.DEFAULT_LAYER);
        
        // Add volume panel to the top layer
        layeredPane.add(volumePanel, JLayeredPane.PALETTE_LAYER);
        
        mainFrame.add(layeredPane);
        mainFrame.setVisible(true);
        showScreen(SCREEN_START);
    }
    
    // ── Global Volume Control Setup ───────────────────────────────────────────
    
    private static void setupGlobalVolumeControl() {
    // Panel for volume control - moved down 5 pixels (y from 520 to 525)
    volumePanel = new JPanel(null);
    volumePanel.setOpaque(false);
    volumePanel.setBounds(830, 525, 100, 180);  // Changed y from 520 to 525
    
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

    public static void showScreen(String screenName) {
        SoundManager.stopMusic();
        cardLayout.show(cardPanel, screenName);
//        // Tell SoundManager which room we are in so it knows which sounds are allowed
// Update the SoundManager state
    if (screenName.equals(SCREEN_MAP)) {
        SoundManager.setActivity(SoundManager.GameActivity.SCREEN_MAP);
        SoundManager.stopMusic(); // Stop cat music when returning to map
    } else if (screenName.equals(SCREEN_CAT_GAME)) {
        SoundManager.setActivity(SoundManager.GameActivity.CAT_GAME);
    }
//        
//        // Update the SoundManager activity based on the screen being shown
//    switch (screenName) {
//        case SCREEN_MAP:
//            SoundManager.setActivity(SoundManager.GameActivity.SCREEN_MAP);
//            break;
//        case SCREEN_CAT_GAME:
//            SoundManager.setActivity(SoundManager.GameActivity.CAT_GAME);
//            break;
//        case SCREEN_BURGLAR_GAME:
//            SoundManager.setActivity(SoundManager.GameActivity.BURGLAR_GAME);
//            break;
//        case SCREEN_BOSS_GAME:
//            SoundManager.setActivity(SoundManager.GameActivity.BOSS_GAME);
//            break;
//        default:
//            // For start screens or transitions, you can default to MAP or a neutral state
//            SoundManager.setActivity(SoundManager.GameActivity.SCREEN_MAP);
//            break;
//            
//    }
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof Refreshable) {
                ((Refreshable) comp).refresh();
            }
        }
        
    }

    public static void replaceScreen(String screenName, JPanel newPanel) {
        for (Component comp : cardPanel.getComponents()) {
            if (screenName.equals(comp.getName())) {
                cardPanel.remove(comp);
                break;
            }
        }
        register(newPanel, screenName);
// ADD THIS LINE HERE:
    updateSoundActivity(screenName); 
    
    cardLayout.show(cardPanel, screenName);
    cardPanel.revalidate();
    cardPanel.repaint();
}

// Create this helper so you don't have to write the switch twice
private static void updateSoundActivity(String screenName) {
    switch (screenName) {
        case SCREEN_CAT_GAME:     SoundManager.setActivity(SoundManager.GameActivity.CAT_GAME); break;
        case SCREEN_BURGLAR_GAME: SoundManager.setActivity(SoundManager.GameActivity.BURGLAR_GAME); break;
        case SCREEN_BOSS_GAME:    SoundManager.setActivity(SoundManager.GameActivity.BOSS_GAME); break;
        case SCREEN_MAP:          SoundManager.setActivity(SoundManager.GameActivity.SCREEN_MAP); break;
    }
}

    private static void register(JPanel panel, String key) {
        panel.setName(key);
        cardPanel.add(panel, key);
    }

    public static JFrame getMainFrame() { return mainFrame; }
}