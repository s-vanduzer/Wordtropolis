/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cs4474.wordtropolis;

/**
 *
 * @author annanguyen
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class burglar_game extends JFrame {
    
    // Game state variables
    private int score = 0;
    private int heroPosition = 0;
    private int robberPosition = 3;
    private int correctNeeded = 8;
    private int currentQuestionIndex = 0;
    private boolean gameActive = true;
    
    // Add these with your other class variables
    private int screenWidth;
    private int screenHeight;
    
    // Scrolling background variables
    private int backgroundOffset = 0;
    private int backgroundWidth; // Will be set from actual image
    private int viewportWidth = 1000; // Width of the visible area
    private BufferedImage backgroundImage;
    private BufferedImage heroImage;
    private BufferedImage robberImage;
    private BufferedImage originalHeroImage;
    private BufferedImage originalRobberImage;
    
    // Word bank
    private List<WordQuestion> questions;
    private WordQuestion currentQuestion;
    
    private char[] selectedLetters; // To store the selected letters in order
    
    // UI Components
    private JLabel titleLabel;
    private JLabel scoreLabel;
    private JLabel wordLabel;
    private JLabel feedbackLabel;
    private JPanel lettersPanel;
    private JPanel gamePanel;
    private JButton[] letterButtons;
    private JButton nextButton;
    private JButton exitButton;
    private JButton[] selectedButtons;
    private JLabel[] blankLabels; // New: labels for each blank space
    
    private int selectedCount;
    
    // Visual representation
    // 1. Add these variables to your class member area
private int currentHeroFrame = 0;
private Timer heroAnimationTimer;
private HeroPanel heroPanel; // Replace JLabel heroLabel with this
    private JLabel robberLabel;
    private BackgroundPanel backgroundPanel;
    
    private JButton checkButton;
    
// Custom JPanel for background drawing
class BackgroundPanel extends JPanel {
@Override

protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (backgroundImage != null) {
        Graphics2D g2d = (Graphics2D) g;

        int imgW = backgroundImage.getWidth();
        int imgH = backgroundImage.getHeight();
        int panelW = getWidth();
        int panelH = getHeight();

        // 1. Calculate the scale factor based on height
        // This ensures the image fills the screen vertically without stretching
        double scale = (double) panelH / imgH;
        int scaledWidth = (int) (imgW * scale);

        // 2. Use the backgroundOffset to scroll through the SCALED width
        // backgroundOffset should ideally range from 0 to scaledWidth
        int xOffset = backgroundOffset % scaledWidth;

        // 3. Draw the first instance of the background
        g2d.drawImage(backgroundImage, -xOffset, 0, scaledWidth, panelH, null);

        // 4. Draw the second instance (for seamless looping)
        if (xOffset > 0) {
            g2d.drawImage(backgroundImage, scaledWidth - xOffset, 0, scaledWidth, panelH, null);
        }
    }
}
}
    public burglar_game() {
        loadImages();
        initQuestions();
        initUI();
        loadNewQuestion();
    }
    
private void loadImages() {
    try {
        backgroundImage = ImageIO.read(
            getClass().getResource("/images/burglar_game/street.png")
        );
        backgroundWidth = backgroundImage.getWidth();

        originalHeroImage = ImageIO.read(
            getClass().getResource("/images/burglar_game/hero1_running.png")
        );

        originalRobberImage = ImageIO.read(
            getClass().getResource("/images/burglar_game/robber_running.gif")
        );

        heroImage = resizeImage(originalHeroImage, 100, 100);
        robberImage = resizeImage(originalRobberImage, 70, 70);

    } catch (Exception e) {
        System.err.println("Error loading images: " + e.getMessage());
        createFallbackImages();
    }
}
    
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null) return null;
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }
    
    private void createFallbackImages() {
        backgroundImage = new BufferedImage(2000, 400, BufferedImage.TYPE_INT_RGB);
        Graphics g = backgroundImage.getGraphics();
        g.setColor(new Color(135, 206, 235)); // Sky blue
        g.fillRect(0, 0, 2000, 800);
        g.setColor(new Color(105, 105, 105));
        
        // Draw some buildings for visual reference
        for (int i = 0; i < 20; i++) {
            int x = i * 100;
            int height = 150 + (i % 5) * 30;
            g.fillRect(x, 400 - height, 80, height);
            g.setColor(Color.DARK_GRAY);
            for (int w = 0; w < 3; w++) {
                g.fillRect(x + 15 + w * 20, 400 - height + 20, 10, 15);
            }
            g.setColor(new Color(105, 105, 105));
        }
        g.dispose();
        backgroundWidth = 2000;
        
        heroImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = heroImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(50, 205, 50));
        g2.fillOval(5, 5, 60, 60);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        g2.drawString("🦸", 20, 52);
        g2.dispose();
        
        robberImage = new BufferedImage(70, 70, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g3 = robberImage.createGraphics();
        g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g3.setColor(new Color(220, 20, 60));
        g3.fillOval(5, 5, 60, 60);
        g3.setColor(Color.BLACK);
        g3.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        g3.drawString("🦹", 20, 52);
        g3.dispose();
        
        originalHeroImage = heroImage;
        originalRobberImage = robberImage;
    }
    
    private void initQuestions() {
        questions = new ArrayList<>();
        
        questions.add(new WordQuestion("UMBRELLA", "UM_R_LLA", "BE", new int[]{2, 4}));
        questions.add(new WordQuestion("BANANA", "B_N_N_", "AA", new int[]{1, 3}));
        questions.add(new WordQuestion("WONDER", "W_ND_R", "OE", new int[]{1, 4}));
        questions.add(new WordQuestion("TIGER", "T_G_R", "IE", new int[]{1, 3}));
        questions.add(new WordQuestion("APPLE", "A_P_E", "PL", new int[]{1, 2}));
        questions.add(new WordQuestion("HAPPY", "H_P_Y", "AP", new int[]{1, 2}));
        questions.add(new WordQuestion("CLOUD", "C_OU_", "LD", new int[]{1, 4}));
        questions.add(new WordQuestion("FLOWER", "F_W_R", "LOE", new int[]{1, 3, 4}));
        questions.add(new WordQuestion("GARDEN", "G_R_EN", "AD", new int[]{1, 3}));
        questions.add(new WordQuestion("KITTEN", "K_T_EN", "IT", new int[]{1, 3}));
        questions.add(new WordQuestion("MONKEY", "M_N_EY", "OK", new int[]{1, 2}));
        questions.add(new WordQuestion("PAPER", "P_P_R", "AE", new int[]{1, 3}));
        questions.add(new WordQuestion("RABBIT", "R_B_T", "AI", new int[]{1, 3}));
        questions.add(new WordQuestion("SUNNY", "S_N_Y", "UN", new int[]{1, 2}));
        questions.add(new WordQuestion("TURTLE", "T_R_LE", "UT", new int[]{1, 3}));
        
        Collections.shuffle(questions);
    }
    
private void initUI() {
    // Set to fullscreen but with window decorations (allows minimize)
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    
    // Get screen dimensions
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    viewportWidth = screenSize.width;
    int viewportHeight = screenSize.height;
    
    // Set window size to fullscreen
    setSize(viewportWidth, viewportHeight);
    setLocation(0, 0);
    
    setLayout(new BorderLayout());
    
    // Create custom background panel with dynamic sizing
    backgroundPanel = new BackgroundPanel();
    backgroundPanel.setLayout(new BorderLayout());
    
    // Create a semi-transparent overlay panel
    JPanel overlayPanel = new JPanel();
    overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
    overlayPanel.setOpaque(false);
    overlayPanel.setBackground(new Color(0, 0, 0, 0));
    
    // Top Panel with Score and Controls
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);
    topPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

    // Score display with styled background
    JPanel scorePanel = new JPanel();
    scorePanel.setOpaque(false);
    scoreLabel = new JLabel("SCORE: " + score + "/" + correctNeeded);
    scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
    scoreLabel.setForeground(Color.WHITE);
    scoreLabel.setBackground(new Color(0, 0, 0, 150));
    scoreLabel.setOpaque(true);
    scoreLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    scorePanel.add(scoreLabel);
    topPanel.add(scorePanel, BorderLayout.WEST);

    // Control buttons panel
    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    controlPanel.setOpaque(false);


    topPanel.add(controlPanel, BorderLayout.EAST);
    
    // Game Title
    titleLabel = new JLabel("CATCH THE BURGLAR!", JLabel.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 42));
    titleLabel.setForeground(new Color(255, 215, 0));
    titleLabel.setOpaque(false);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
    
    // Word Display Panel with improved styling
    JPanel wordPanel = new JPanel();
    wordPanel.setOpaque(false);
    wordPanel.setLayout(new BoxLayout(wordPanel, BoxLayout.Y_AXIS));
    wordPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    
    // "New word" label
    JLabel newWordLabel = new JLabel("★ NEW WORD ★", JLabel.CENTER);
    newWordLabel.setFont(new Font("Arial", Font.BOLD, 20));
    newWordLabel.setForeground(new Color(255, 215, 0));
    newWordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    newWordLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    
    // Word display with blanks - using separate labels for each character
    wordLabel = new JLabel("", JLabel.CENTER);
    wordLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
    wordLabel.setForeground(Color.WHITE);
    wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    wordLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    
    wordPanel.add(wordLabel);
    
    // Letters Panel - nicer looking tiles
    JPanel lettersContainer = new JPanel();
    lettersContainer.setLayout(new BoxLayout(lettersContainer, BoxLayout.Y_AXIS));
    lettersContainer.setOpaque(false);
    lettersContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
    
    lettersPanel = new JPanel();
    lettersPanel.setLayout(new GridLayout(1, 5, 20, 20));
    lettersPanel.setBackground(new Color(0, 0, 0, 0));
    lettersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    lettersPanel.setMaximumSize(new Dimension(600, 100));
    
    int buttonSize = Math.min(viewportHeight / 12, 85);
    letterButtons = new JButton[5];
    for (int i = 0; i < 5; i++) {
        letterButtons[i] = createLetterButton("");
        letterButtons[i].setPreferredSize(new Dimension(buttonSize, buttonSize));
        final int index = i;
        letterButtons[i].addActionListener(e -> onLetterSelected(index));
        lettersPanel.add(letterButtons[i]);
    }
    
    lettersContainer.add(lettersPanel);
    
    // Instruction label
    JLabel instructionLabel = new JLabel("▼ Click the missing letters in order ▼", JLabel.CENTER);
    instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    instructionLabel.setForeground(new Color(200, 200, 200));
    instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    instructionLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
    
    lettersContainer.add(instructionLabel);
    
    // Feedback Panel
    feedbackLabel = new JLabel("", JLabel.CENTER);
    feedbackLabel.setFont(new Font("Arial", Font.BOLD, 16));
    feedbackLabel.setForeground(Color.WHITE);
    feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    feedbackLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    
    // Button Panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.setOpaque(false);
    
    // Check Answer Button
    checkButton = createActionButton("✓ CHECK ANSWER", new Color(70, 130, 200));
    checkButton.setEnabled(false);
    checkButton.addActionListener(e -> evaluateSelection());
    
    // Next Button
    nextButton = createActionButton("NEXT WORD →", new Color(60, 179, 113));
    nextButton.setEnabled(false);
    nextButton.addActionListener(e -> loadNewQuestion());
    
    // Exit Button
    exitButton = createActionButton("EXIT GAME", new Color(220, 60, 50));
    exitButton.addActionListener(e -> System.exit(0));
    
    buttonPanel.add(checkButton);
    buttonPanel.add(nextButton);
    buttonPanel.add(exitButton);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Add spacing and assemble main container
    overlayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    overlayPanel.add(topPanel);
    overlayPanel.add(titleLabel);
    overlayPanel.add(wordPanel);
    overlayPanel.add(lettersContainer);
    overlayPanel.add(feedbackLabel);
    overlayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    overlayPanel.add(buttonPanel);
    overlayPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    
    // Add overlay panel to background panel
    backgroundPanel.add(overlayPanel, BorderLayout.CENTER);
    
    // Characters Panel for hero and robber (overlay at bottom)
    JPanel charactersPanel = new JPanel(null);
    charactersPanel.setOpaque(false);
    charactersPanel.setPreferredSize(new Dimension(viewportWidth, 100));
    
//    // Scale character sizes
//    int characterSize = Math.min(viewportHeight / 12, 80);
//    if (originalHeroImage != null && originalRobberImage != null) {
//        heroImage = resizeImage(originalHeroImage, characterSize, characterSize);
//        robberImage = resizeImage(originalRobberImage, characterSize, characterSize);
//    }
    

// Replace the old heroLabel initialization with this:
    heroPanel = new HeroPanel();
    robberLabel = new JLabel(new ImageIcon(robberImage)); // Robber is already a GIF
    
    charactersPanel.add(heroPanel);
    charactersPanel.add(robberLabel);

    
   
    charactersPanel.add(robberLabel);
    
    backgroundPanel.add(charactersPanel, BorderLayout.SOUTH);
    
    add(backgroundPanel);
        
    selectedButtons = new JButton[0];
    selectedLetters = new char[0];
    selectedCount = 0;
    
    screenWidth = viewportWidth;
    screenHeight = viewportHeight;
    
    // Add resize listener
    addComponentListener(new java.awt.event.ComponentAdapter() {
        public void componentResized(java.awt.event.ComponentEvent evt) {
            screenWidth = getWidth();
            screenHeight = getHeight();
            updateCharacterPositions();
            backgroundPanel.repaint();
        }
    });
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
    updateCharacterPositions();
    SwingUtilities.invokeLater(() -> {
    updateCharacterPositions();
});
}

private JButton createStyledControlButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 20));
    button.setBackground(new Color(100, 100, 100, 200));
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return button;
}

private JButton createLetterButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 32));
    button.setBackground(new Color(255, 215, 0));
    button.setForeground(Color.YELLOW);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return button;
}

private JButton createActionButton(String text, Color bgColor) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 16));
    button.setBackground(bgColor);
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return button;
}

private void updateWordDisplayWithBlanks() {
    if (currentQuestion == null) return;
    
    StringBuilder display = new StringBuilder();
    String fullWord = currentQuestion.fullWord;
    String displayWord = currentQuestion.displayWord;
    
    // Create HTML text with blanks highlighted
    StringBuilder htmlBuilder = new StringBuilder("<html><body style='text-align: center;'>");
    
    for (int i = 0; i < displayWord.length(); i++) {
        char c = displayWord.charAt(i);
        if (c == '_') {
            // This is a blank space
            int blankIndex = getBlankIndex(i, currentQuestion.positions);
            if (blankIndex < selectedCount && selectedLetters[blankIndex] != ' ') {
                // Show the selected letter
                htmlBuilder.append("<span style='background-color: #FFD700; color: #000000; padding: 12px 18px; margin: 0 4px; border-radius: 12px; display: inline-block; min-width: 50px; text-align: center; font-weight: bold; font-size: 32px; box-shadow: 0 2px 5px rgba(0,0,0,0.2);'>")
                          .append(selectedLetters[blankIndex])
                          .append("</span>");
            } else {
                // Show empty blank
                htmlBuilder.append("<span style='background-color: #2c3e50; color: #95a5a6; padding: 12px 18px; margin: 0 4px; border-radius: 12px; display: inline-block; min-width: 50px; text-align: center; font-weight: bold; font-size: 28px; box-shadow: inset 0 2px 5px rgba(0,0,0,0.2);'>?</span>");
            }
        } else {
            // Regular character
            htmlBuilder.append("<span style='background-color: #3498db; color: #FFFFFF; padding: 12px 18px; margin: 0 4px; border-radius: 12px; display: inline-block; min-width: 50px; text-align: center; font-weight: bold; font-size: 32px; box-shadow: 0 2px 5px rgba(0,0,0,0.2);'>")
                      .append(c)
                      .append("</span>");
        }
    }
    
    htmlBuilder.append("</body></html>");
    wordLabel.setText(htmlBuilder.toString());
}

private int getBlankIndex(int position, int[] blankPositions) {
    // Count how many blanks come before this position
    int count = 0;
    for (int i = 0; i < blankPositions.length; i++) {
        if (blankPositions[i] < position) {
            count++;
        }
    }
    return count;
}

private void updateCharacterPositions() {
    if (heroPanel != null && robberLabel != null) {
        // Dynamic margins based on screen size
        int minX = screenWidth / 12;
        int maxX = screenWidth - (screenWidth / 8);
        
        // Map positions 0-10 to screen coordinates
        int heroX = minX + (heroPosition * (maxX - minX) / 10);
        int robberX = minX + (robberPosition * (maxX - minX) / 10);
        
int charWidth = 120;  // Adjust size as needed
        int charHeight = 120;
        
        // Match the ground level of your background
        int yPos = screenHeight - charHeight - 150; 
        
        heroPanel.setBounds(heroX, yPos+ 100, charWidth, charHeight);
        robberLabel.setBounds(robberX, yPos, 80, 80);
        
        updateBackgroundScroll();
    }
}
    
    private void updateBackgroundScroll() {
        // Use current screen width as viewport
        int currentWidth = getWidth();
        if (currentWidth > 0) {
            viewportWidth = currentWidth;
        }
        
        if (backgroundWidth > viewportWidth) {
            int maxScroll = backgroundWidth - viewportWidth;
            
            // Map robber position (0-10) to scroll amount (0 to maxScroll)
            backgroundOffset = (robberPosition * maxScroll) / 10;
            
            // Clamp values
            if (backgroundOffset < 0) backgroundOffset = 0;
            if (backgroundOffset > maxScroll) backgroundOffset = maxScroll;
        } else {
            backgroundOffset = 0;
        }
        
        // Repaint the background to show new portion
        if (backgroundPanel != null) {
            backgroundPanel.repaint();
        }
    }
    
private void loadNewQuestion() {
    if (!gameActive) return;
    
    if (currentQuestionIndex >= questions.size()) {
        Collections.shuffle(questions);
        currentQuestionIndex = 0;
    }
    
    currentQuestion = questions.get(currentQuestionIndex);
    currentQuestionIndex++;
    // Dynamically size arrays based on missing letters
    int len = currentQuestion.missingLetters.length();
    selectedButtons = new JButton[len];
    selectedLetters = new char[len];
    selectedCount = 0;
    
    // Update word display with blanks
    updateWordDisplayWithBlanks();
    
    // Generate random letters
    List<Character> randomLetters = generateRandomLetters(currentQuestion.missingLetters);
    
    // Reset letter buttons with new styling
    for (int i = 0; i < 5; i++) {
        letterButtons[i].setText(String.valueOf(randomLetters.get(i)));
        letterButtons[i].setEnabled(true);
        letterButtons[i].setBackground(new Color(255, 215, 0));
        letterButtons[i].setForeground(Color.CYAN);
        letterButtons[i].setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }
    
    // Reset selection arrays
    selectedCount = 0;
    for (int i = 0; i < selectedButtons.length; i++) {
        selectedButtons[i] = null;
    }
    for (int i = 0; i < selectedLetters.length; i++) {
        selectedLetters[i] = ' ';
    }
    
    nextButton.setEnabled(false);
    checkButton.setEnabled(false);
    feedbackLabel.setText("Select the missing letters in order!");
    feedbackLabel.setForeground(new Color(255, 215, 0));
}

private List<Character> generateRandomLetters(String missingLetters) {
    List<Character> letters = new ArrayList<>();
    Random random = new Random();
    
    // Add the missing letters
    for (char c : missingLetters.toCharArray()) {
        letters.add(c);
    }
    
    // Add random letters until we have 5 total
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    while (letters.size() < 5) {
        char randomLetter = alphabet.charAt(random.nextInt(26));
        if (!letters.contains(randomLetter)) {
            letters.add(randomLetter);
        }
    }
    
    // Shuffle
    Collections.shuffle(letters);
    return letters;
}

private void onLetterSelected(int index) {
    if (!gameActive) return;
    
    // Check if we've already selected all missing letters
    if (selectedCount >= currentQuestion.missingLetters.length()) {
        feedbackLabel.setText("✓ All letters selected! Click CHECK ANSWER.");
        feedbackLabel.setForeground(Color.CYAN);
        return;
    }
    
    // Store the selected letter
    char selectedLetter = letterButtons[index].getText().charAt(0);
    
    // Store the selected button
    selectedButtons[selectedCount] = letterButtons[index];
    
    // Visual feedback - mark as selected
    letterButtons[index].setBackground(new Color(100, 200, 100));
    letterButtons[index].setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    letterButtons[index].setEnabled(false);
    
    // Store the selected letter
    selectedLetters[selectedCount] = selectedLetter;
    selectedCount++;
    
    // Update the display to show the filled blank
    updateWordDisplayWithBlanks();
    
    // Update feedback
    if (selectedCount == currentQuestion.missingLetters.length()) {
        feedbackLabel.setText("All letters selected! Click CHECK ANSWER to verify.");
        feedbackLabel.setForeground(new Color(0, 255, 100));
        checkButton.setEnabled(true);
    } else {
        feedbackLabel.setText("Selected " + selectedCount + " of " + 
                             currentQuestion.missingLetters.length() + 
                             " letters. Keep going!");
        feedbackLabel.setForeground(new Color(255, 215, 0));
    }
}

private void evaluateSelection() {
    String selected = new String(selectedLetters);

    if (selected.equals(currentQuestion.missingLetters)) {
        feedbackLabel.setText("✓ CORRECT! Great job!");
        feedbackLabel.setForeground(new Color(0, 255, 100));
        handleCorrectAnswer();
    } else {
        feedbackLabel.setText("✗ WRONG! The correct answer was: " + currentQuestion.missingLetters);
        feedbackLabel.setForeground(new Color(255, 100, 100));
        // Show the correct letters in the blanks for a moment
        showCorrectAnswer();
        handleWrongAnswer();
    }
}

private void showCorrectAnswer() {
    // Temporarily show the correct letters in the blanks
    char[] correctLetters = currentQuestion.missingLetters.toCharArray();
    for (int i = 0; i < correctLetters.length; i++) {
        selectedLetters[i] = correctLetters[i];
    }
    updateWordDisplayWithBlanks();
    
    // Reset the letters after a delay
    Timer timer = new Timer(1500, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetSelection();
            updateWordDisplayWithBlanks();
        }
    });
    timer.setRepeats(false);
    timer.start();
}



private void animateMovement(boolean heroMoves) {
    int steps = 20;
    int delay = 15;

    Timer moveTimer = new Timer(delay, null);

    moveTimer.addActionListener(new ActionListener() {
        int currentStep = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentStep >= steps) {
                moveTimer.stop();

                if (heroMoves) heroPosition++;
                else robberPosition++;

                updateCharacterPositions();

                // End conditions
                if (heroPosition >= robberPosition) {
                    gameActive = false;
                    showWinScreen();
                } else if (robberPosition >= 10) {
                    gameActive = false;
                    showLoseScreen();
                } else if (heroMoves) {
                    nextButton.setEnabled(true);
                } else {
                    resetSelection();
                }

                return;
            }

            // Smooth interpolation
            double progress = (double) currentStep / steps;

            if (heroMoves) {
                double temp = heroPosition + progress;
                updateCharacterPositionSmooth(temp, robberPosition);
            } else {
                double temp = robberPosition + progress;
                updateCharacterPositionSmooth(heroPosition, temp);
            }

            currentStep++;
        }
    });

    moveTimer.start();
}

private void updateCharacterPositionSmooth(double heroPos, double robberPos) {
    int minX = screenWidth / 10;
    int maxX = screenWidth - (screenWidth / 6);

    int heroX = minX + (int)((heroPos * (maxX - minX)) / 10.0);
    int robberX = minX + (int)((robberPos * (maxX - minX)) / 10.0);

    


    updateBackgroundScrollSmooth((heroPos + robberPos) / 2.0);
}

private void updateBackgroundScrollSmooth(double midPosition) {
    
    int currentWidth = getWidth();
    if (currentWidth > 0) {
        viewportWidth = currentWidth;
    }

    if (backgroundWidth > viewportWidth) {
        int maxScroll = backgroundWidth - viewportWidth;

        backgroundOffset = (int)((midPosition * maxScroll) / 10.0);

        if (backgroundOffset < 0) backgroundOffset = 0;
        if (backgroundOffset > maxScroll) backgroundOffset = maxScroll;
    } else {
        backgroundOffset = 0;
    }

    backgroundPanel.repaint();
}


private void handleCorrectAnswer() {
    score++;
//    heroPosition++;
animateMovement(true);
    scoreLabel.setText("SCORE: " + score + "/" + correctNeeded);
    
    // Add bounds checking
    if (heroPosition > 10) {
        heroPosition = 10;
    }
    
    // Disable all letter buttons and check button
    for (JButton btn : letterButtons) {
        btn.setEnabled(false);
    }
    checkButton.setEnabled(false);
    
//    updateCharacterPositions();
    
    // Check if hero caught the robber
    if (heroPosition >= robberPosition) {
        gameActive = false;
        showWinScreen();
    } else {
        nextButton.setEnabled(true);
        feedbackLabel.setText("✓ Correct! Press NEXT WORD to continue!");
        feedbackLabel.setForeground(new Color(0, 255, 100));
    }
}
    
private void handleWrongAnswer() {
    animateMovement(false);
    
    // Disable all letter buttons
    for (JButton btn : letterButtons) {
        btn.setEnabled(false);
    }
    
    checkButton.setEnabled(false);
    nextButton.setEnabled(true);

    if (robberPosition > 10) {
        robberPosition = 10;
    }

//    updateCharacterPositions();

    if (robberPosition >= 10) {
        gameActive = false;
        showLoseScreen();
        return;
    }
    
    feedbackLabel.setText("✗ Wrong! Press NEXT WORD to try another word.");
    feedbackLabel.setForeground(new Color(255, 100, 100));
}

private void resetSelection() {
    selectedCount = 0;

    for (int i = 0; i < selectedButtons.length; i++) {
        selectedButtons[i] = null;
        selectedLetters[i] = ' ';
    }
}

private void showWinScreen() {
    setVisible(false);
    
    // Get screen dimensions for responsive dialog
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int dialogWidth = Math.min(550, screenSize.width - 100);
    int dialogHeight = Math.min(400, screenSize.height - 100);
    
    JDialog winDialog = new JDialog(this, "VICTORY!", true);
    winDialog.setLayout(new BorderLayout());
    winDialog.setSize(dialogWidth, dialogHeight);
    winDialog.setLocationRelativeTo(this);
    winDialog.setUndecorated(true);
    
    // Create main panel with gradient background
    JPanel mainPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, new Color(0, 100, 0), 0, getHeight(), new Color(0, 50, 0));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
    
    // Trophy emoji
    JLabel trophyLabel = new JLabel("🏆", JLabel.CENTER);
    trophyLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
    trophyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Win message
    JLabel winLabel = new JLabel("CONGRATULATIONS!", JLabel.CENTER);
    winLabel.setFont(new Font("Arial", Font.BOLD, 28));
    winLabel.setForeground(Color.YELLOW);
    winLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    JLabel winSubLabel = new JLabel("YOU CAUGHT THE BURGLAR!", JLabel.CENTER);
    winSubLabel.setFont(new Font("Arial", Font.BOLD, 18));
    winSubLabel.setForeground(Color.WHITE);
    winSubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Score display
    JLabel finalScoreLabel = new JLabel("Final Score: " + score + " / " + correctNeeded, JLabel.CENTER);
    finalScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    finalScoreLabel.setForeground(Color.LIGHT_GRAY);
    finalScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    finalScoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    
    // Buttons
    JButton restartButton = createActionButton("PLAY AGAIN", new Color(50, 150, 50));
    restartButton.addActionListener(e -> {
        winDialog.dispose();
        new burglar_game();
        dispose();
    });
    
    JButton exitWinButton = createActionButton("EXIT", new Color(200, 50, 50));
    exitWinButton.addActionListener(e -> System.exit(0));
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.setOpaque(false);
    buttonPanel.add(restartButton);
    buttonPanel.add(exitWinButton);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    mainPanel.add(trophyLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    mainPanel.add(winLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(winSubLabel);
    mainPanel.add(finalScoreLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    mainPanel.add(buttonPanel);
    
    winDialog.add(mainPanel);
    winDialog.setVisible(true);
    
    dispose();
}
    
private void showLoseScreen() {
    setVisible(false);
    
    // Get screen dimensions for responsive dialog
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int dialogWidth = Math.min(550, screenSize.width - 100);
    int dialogHeight = Math.min(400, screenSize.height - 100);
    
    JDialog loseDialog = new JDialog(this, "GAME OVER", true);
    loseDialog.setLayout(new BorderLayout());
    loseDialog.setSize(dialogWidth, dialogHeight);
    loseDialog.setLocationRelativeTo(this);
    loseDialog.setUndecorated(true);
    
    // Create main panel with gradient background
    JPanel mainPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, new Color(100, 0, 0), 0, getHeight(), new Color(50, 0, 0));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
    
    // Sad face emoji
    JLabel sadLabel = new JLabel("😭", JLabel.CENTER);
    sadLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
    sadLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Lose message
    JLabel loseLabel = new JLabel("GAME OVER!", JLabel.CENTER);
    loseLabel.setFont(new Font("Arial", Font.BOLD, 28));
    loseLabel.setForeground(Color.RED);
    loseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    JLabel loseSubLabel = new JLabel("THE BURGLAR ESCAPED!", JLabel.CENTER);
    loseSubLabel.setFont(new Font("Arial", Font.BOLD, 18));
    loseSubLabel.setForeground(Color.WHITE);
    loseSubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Score display
    JLabel finalScoreLabel = new JLabel("You got " + score + " / " + correctNeeded + " correct", JLabel.CENTER);
    finalScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    finalScoreLabel.setForeground(Color.LIGHT_GRAY);
    finalScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    finalScoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    
    // Buttons
    JButton restartButton = createActionButton("TRY AGAIN", new Color(50, 150, 50));
    restartButton.addActionListener(e -> {
        loseDialog.dispose();
        new burglar_game();
        dispose();
    });
    
    JButton exitLoseButton = createActionButton("EXIT", new Color(200, 50, 50));
    exitLoseButton.addActionListener(e -> System.exit(0));
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.setOpaque(false);
    buttonPanel.add(restartButton);
    buttonPanel.add(exitLoseButton);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    mainPanel.add(sadLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    mainPanel.add(loseLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(loseSubLabel);
    mainPanel.add(finalScoreLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    mainPanel.add(buttonPanel);
    
    loseDialog.add(mainPanel);
    loseDialog.setVisible(true);
    
    dispose();
}
    
    // Inner class to hold word question data
    private class WordQuestion {
        String fullWord;
        String displayWord;
        String missingLetters;
        int[] positions;
        
        WordQuestion(String fullWord, String displayWord, String missingLetters, int[] positions) {
            this.fullWord = fullWord;
            this.displayWord = displayWord;
            this.missingLetters = missingLetters;
            this.positions = positions;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new burglar_game();
        });
    }
    
    
    // 2. Add this inner class at the bottom of your file
class HeroPanel extends JPanel {
    private final int TOTAL_FRAMES = 4;

    public HeroPanel() {
        setOpaque(false);
        // This timer cycles the frame index every 150ms
        heroAnimationTimer = new Timer(150, e -> {
            currentHeroFrame = (currentHeroFrame + 1) % TOTAL_FRAMES;
            repaint();
        });
        heroAnimationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (originalHeroImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Keeps the pixel art from looking blurry
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                               RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int frameWidth = originalHeroImage.getWidth() / TOTAL_FRAMES;
            int frameHeight = originalHeroImage.getHeight();

            // Calculate the "window" on the sprite sheet
            int srcX1 = currentHeroFrame * frameWidth;
            int srcX2 = srcX1 + frameWidth;

            // Draw scaled up to fill this panel's bounds
            g2d.drawImage(originalHeroImage, 
                0, 0, getWidth(), getHeight(), // Destination
                srcX1, 0, srcX2, frameHeight,  // Source
                null);
        }
    }
}
}

