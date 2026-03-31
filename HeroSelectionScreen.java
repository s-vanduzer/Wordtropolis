/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wordtropolis;

/**
 *
 * @author Sarah
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.Timer;

public class HeroSelectionScreen extends JPanel {

    private JLabel selectedBorder = new JLabel();
    private JTextField nameField;

    private JLabel selectedHero = null; // stores selected hero

    private int floatY = 0;
    private int floatDir = 1;

    public HeroSelectionScreen() {

        setLayout(null);
        setPreferredSize(new Dimension(920, 700));

        // the background image 
        Image bg = new ImageIcon(getClass().getResource("/assets/images/BG.png")).getImage();
        Image scaledBg = bg.getScaledInstance(920, 700, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(scaledBg));
        background.setBounds(0, 0, 920, 700);
        background.setLayout(null);
        add(background);

       // wordtropolis title image 
        Image titleImg = new ImageIcon(getClass().getResource("/assets/images/wordtropiatitle2.png")).getImage();
        Image scaledTitle = titleImg.getScaledInstance(600, -1, Image.SCALE_SMOOTH);

        JLabel gameTitle = new JLabel(new ImageIcon(scaledTitle));
        gameTitle.setBounds((920 - 600) / 2, 10, 600, 200);
        background.add(gameTitle);

        // choose your hero title 
        JLabel title = new JLabel("Select Your Hero!");
        title.setFont(loadGameFont(28));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 185, 920, 50);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        background.add(title);

        // the super hero images 
        JLabel girlHero = createHero("/assets/images/girlhero.png", 120, 270);
        JLabel boyHero  = createHero("/assets/images/boyhero.png", 360, 270);
        JLabel alienHero = createHero("/assets/images/alien.png", 600, 270);

        background.add(girlHero);
        background.add(boyHero);
        background.add(alienHero);

        //floating animation for characters 
        Timer floatTimer = new Timer(60, e -> {
            floatY += floatDir;
            if (floatY > 6 || floatY < -6) floatDir *= -1;

            girlHero.setLocation(120, 270 + floatY);
            boyHero.setLocation(360, 270 + floatY);
            alienHero.setLocation(600, 270 + floatY);
        });
        floatTimer.start();

        // yellow border for hero selection
        selectedBorder.setOpaque(false);
        selectedBorder.setVisible(false);
        background.add(selectedBorder);

        addHeroHoverAndSelect(girlHero);
        addHeroHoverAndSelect(boyHero);
        addHeroHoverAndSelect(alienHero);

        // Enter name ( for student)
        nameField = new JTextField("Enter Name Here...") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xEC, 0xCB, 0x2D));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };

        nameField.setBounds(260, 540, 400, 50);
        nameField.setFont(loadGameFont(20));
        nameField.setForeground(Color.WHITE);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        nameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (nameField.getText().equals("Enter Name Here...")) {
                    nameField.setText("");
                }
            }
        });

        background.add(nameField);

        // the button images 
        Image btnImg = new ImageIcon(getClass().getResource("/assets/images/button.png")).getImage();
        Image scaledBtn = btnImg.getScaledInstance(170, 55, Image.SCALE_SMOOTH);
        ImageIcon btnIcon = new ImageIcon(scaledBtn);

      // back button 
        JButton backBtn = new JButton("Back", btnIcon);
        backBtn.setHorizontalTextPosition(JButton.CENTER);
        backBtn.setFont(loadGameFont(16));
        backBtn.setForeground(Color.WHITE);

        backBtn.setBounds(60, 540, 170, 55);

        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        background.add(backBtn);

        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { backBtn.setSize(175, 60); }
            public void mouseExited(MouseEvent e) { backBtn.setSize(170, 55); }
        });

        backBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(new StartScreen());
            frame.revalidate();
        });

        // continue button
        JButton contBtn = new JButton("Continue", btnIcon);
        contBtn.setHorizontalTextPosition(JButton.CENTER);
        contBtn.setFont(loadGameFont(16));
        contBtn.setForeground(Color.WHITE);

        contBtn.setBounds(680, 540, 170, 55);

        contBtn.setBorderPainted(false);
        contBtn.setContentAreaFilled(false);
        contBtn.setFocusPainted(false);
        background.add(contBtn);

        contBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { contBtn.setSize(175, 60); }
            public void mouseExited(MouseEvent e) { contBtn.setSize(170, 55); }
        });

        contBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(new MapScreen());
            frame.revalidate();
        });

       // volume and icon button
        Image speakerRaw = new ImageIcon(getClass().getResource("/assets/images/speaker.png")).getImage();
        Image speakerScaled = speakerRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon speakerImg = new ImageIcon(speakerScaled);

        Image muteRaw = new ImageIcon(getClass().getResource("/assets/images/nospeaker.png")).getImage();
        Image muteScaled = muteRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon muteImg = new ImageIcon(muteScaled);

        JLabel volumeIcon = new JLabel(speakerImg);
        volumeIcon.setBounds(920 - 40 - 30 - 8, 700 - 40 - 20 - 30, 40, 40);
        background.add(volumeIcon);

        JSlider volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
        volumeSlider.setBounds(920 - 20 - 20, 700 - 120 - 40, 20, 120);
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(new Color(0xEC, 0xCB, 0x2D));
        volumeSlider.setFont(loadGameFont(12));
        volumeSlider.setVisible(false);
        background.add(volumeSlider);

        volumeIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                volumeSlider.setVisible(!volumeSlider.isVisible());
            }
        });

        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            volumeIcon.setIcon(value == 0 ? muteImg : speakerImg);
        });
    }

    // // hero creation 
    private JLabel createHero(String path, int x, int y) {
        Image img = new ImageIcon(getClass().getResource(path)).getImage();
        Image scaled = img.getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        JLabel hero = new JLabel(new ImageIcon(scaled));
        hero.setBounds(x, y, 180, 180);
        return hero;
    }

    // // hover and clicking hero selection
    private void addHeroHoverAndSelect(JLabel hero) {

        hero.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hero.setSize(190, 190);
                hero.setLocation(hero.getX() - 5, hero.getY() - 5);

                if (hero != selectedHero) {
                    selectedBorder.setBounds(hero.getX() - 10, hero.getY() - 10, 210, 210);
                    selectedBorder.setBorder(BorderFactory.createLineBorder(new Color(0xEC, 0xCB, 0x2D), 4, true));
                    selectedBorder.setVisible(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hero.setSize(180, 180);
                hero.setLocation(hero.getX() + 5, hero.getY() + 5);

                if (hero != selectedHero) {
                    selectedBorder.setVisible(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectedHero = hero;

                selectedBorder.setBounds(hero.getX() - 10, hero.getY() - 10, 210, 210);
                selectedBorder.setBorder(BorderFactory.createLineBorder(new Color(0xEC, 0xCB, 0x2D), 4, true));
                selectedBorder.setVisible(true);
            }
        });
    }

            // the font 
   
    private Font loadGameFont(int size) {
        try {
            Font font = Font.createFont(
                Font.TRUETYPE_FONT,
                getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf")
            );
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            return new Font("Arial", Font.PLAIN, size);
        }
    }
}