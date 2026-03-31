/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
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

public class StartScreen extends JPanel {
    private int baseTitleY = 120;   // y axis position 
    private double time = 0;        // to make it smooth
    private final JLabel titleLabel;
    private int floatDirection = 1; // can move it up or down 

    public StartScreen() {

        setLayout(null);
        setPreferredSize(new Dimension(920, 700));

        // bg image (og)
Image bg = new ImageIcon(
    getClass().getResource("/assets/images/BG.png")
).getImage();


Image scaledBg = bg.getScaledInstance(920, 700, Image.SCALE_SMOOTH);

// scaled image 
JLabel background = new JLabel(new ImageIcon(scaledBg));
background.setBounds(0, 0, 920, 700);
background.setLayout(null);
add(background);

// cloud image 
Image cloudRaw = new ImageIcon(getClass().getResource("/assets/images/cloud.png")).getImage();
Image cloudScaled = cloudRaw.getScaledInstance(360, 190, Image.SCALE_SMOOTH); // bigger clouds
ImageIcon cloudImg = new ImageIcon(cloudScaled);

// cloud A behind title 
JLabel cloudA = new JLabel(cloudImg);
cloudA.setBounds(-360, 10, 360, 190);   // top left cloud
background.add(cloudA);   


        // title image
Image title = new ImageIcon(
    getClass().getResource("/assets/images/wordtropiatitle2.png")
).getImage();

// title cale 
Image scaledTitle = title.getScaledInstance(700, -1, Image.SCALE_SMOOTH);

// the title but  window scale 
titleLabel = new JLabel(new ImageIcon(scaledTitle));
        int windowWidth = 920; // window width 

int titleWidth = scaledTitle.getWidth(null);
int titleHeight = scaledTitle.getHeight(null);

int titleX = (windowWidth - titleWidth) / 2;
int titleY = 120; 

titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
background.add(titleLabel);

        // title animation 
        Timer floatTimer = new Timer(60, e -> floatTitle());
        floatTimer.start();

// cloud b in front of title 
        JLabel cloudB = new JLabel(cloudImg);
cloudB.setBounds(920, 140, 360, 190);   // right cloud
background.add(cloudB);   // in front of title 

// cloud animation 

Timer cloudTimer = new Timer(30, e -> {

    // cloud a ( behinf title)
    int ax = cloudA.getX() + 2;   // fast or not fast
    if (ax > 920) ax = -360;      // looping 
    cloudA.setLocation(ax, cloudA.getY());

    // cloud b 
    int bx = cloudB.getX() - 2;   // fast or not fast
    if (bx < -360) bx = 920;      //looping 
    cloudB.setLocation(bx, cloudB.getY());
});

cloudTimer.start();

        // the button 
        JPanel buttonPanel = new JPanel(null);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(210, 350, 500, 150);
        background.add(buttonPanel);

        // Buttons
        JButton teacherBtn = new JButton("I am a Teacher");
        JButton studentBtn = new JButton("I am a Student");

        teacherBtn.setBounds(50, -20, 400, 70);
        studentBtn.setBounds(50, 60, 400, 70);

        teacherBtn.setFont(loadGameFont(24));
        studentBtn.setFont(loadGameFont(24));
        
        teacherBtn.setContentAreaFilled(false);
        teacherBtn.setBorderPainted(false);
        teacherBtn.setFocusPainted(false);
        teacherBtn.setOpaque(false);

        studentBtn.setContentAreaFilled(false);
        studentBtn.setBorderPainted(false);
        studentBtn.setFocusPainted(false);
        studentBtn.setOpaque(false);
        
        teacherBtn.setForeground(new Color(0xEC, 0xCB, 0x2D));
        studentBtn.setForeground(new Color(0xEC, 0xCB, 0x2D));

        buttonPanel.add(teacherBtn);
        buttonPanel.add(studentBtn);

        // the triangle for the menu 
        JLabel selector = new JLabel("▶");
        selector.setFont(loadGameFont(24));
        selector.setForeground(new Color(0xEC, 0xCB, 0x2D));
        selector.setBounds(10, 0, 40, 50);
        selector.setVisible(false);
        buttonPanel.add(selector);

         // highlight bar for the menu 
        JPanel highlight = new JPanel();
        highlight.setBackground(new Color(255, 255, 255, 40)); // translucent white
        highlight.setBounds(0, -40, 1500, 70);
        highlight.setVisible(false);
        buttonPanel.add(highlight);

        buttonPanel.setComponentZOrder(highlight, buttonPanel.getComponentCount() - 1);

        // hovering buttons 
        teacherBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                highlight.setBounds(0, -20, 1500, 70);
                selector.setLocation(10, 0);
                highlight.setVisible(true);
                selector.setVisible(true);
            }
        });

        studentBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                highlight.setBounds(0, 60, 500, 50);
                selector.setLocation(10, 60);
                highlight.setVisible(true);
                selector.setVisible(true);
            }
        });

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                highlight.setVisible(false);
                selector.setVisible(false);
            }
        });

        // volume icon and the slider 
// the speaker icons and it mutes when at 0
        Image speakerRaw = new ImageIcon(getClass().getResource("/assets/images/speaker.png")).getImage();
Image speakerScaled = speakerRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
ImageIcon speakerImg = new ImageIcon(speakerScaled);

Image muteRaw = new ImageIcon(getClass().getResource("/assets/images/nospeaker.png")).getImage();
Image muteScaled = muteRaw.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
ImageIcon muteImg = new ImageIcon(muteScaled);

// icon label 
JLabel volumeIcon = new JLabel(speakerImg);
volumeIcon.setBounds(
    920 - 40 - 30 - 8,   // left
    700 - 40 - 20 - 30,  // up
    40,
    40
);
background.add(volumeIcon);


// vertical slider 
JSlider volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
volumeSlider.setBounds(
    920 - 20 - 20,       // x position
    700 - 120 - 40,      // y up position
    20,
    120
);
volumeSlider.setOpaque(false);
volumeSlider.setForeground(new Color(0xEC, 0xCB, 0x2D));
volumeSlider.setFont(loadGameFont(12));
background.add(volumeSlider);

// start hide 
volumeSlider.setVisible(false);

//toggle when clicked on speaker icon
volumeIcon.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        volumeSlider.setVisible(!volumeSlider.isVisible());
    }
});

// this changes the icon image depending on the slider value 
volumeSlider.addChangeListener(e -> {
    int value = volumeSlider.getValue();

    if (value == 0) {
        volumeIcon.setIcon(muteImg);      // displays mute icon
    } else {
        volumeIcon.setIcon(speakerImg);   // displays normal icon
    }

    System.out.println("Volume: " + value);
});
    }

      // floating title animation


private void floatTitle() {
    int bounceRange = 10;     // how far it moves up/down
    double speed = 0.15;      // increase speed for smoother, faster motion

    time += speed;

    int newY = baseTitleY + (int)(Math.sin(time) * bounceRange);

    titleLabel.setLocation(titleLabel.getX(), newY);
}

    // ============================
    // LOAD CUSTOM FONT
    // ============================
    private Font loadGameFont(int size) {
        try {
            Font font = Font.createFont(
                Font.TRUETYPE_FONT,
                getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf")
            );
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            System.out.println("Font failed to load, using default.");
            return new Font("Arial", Font.PLAIN, size);
        }
    }

    // main method that runs the screen 
    public static void main(String[] args) {
    JFrame frame = new JFrame("Wordtopia");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    StartScreen screen = new StartScreen();
    frame.setContentPane(screen);

    frame.setSize(920, 700);
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}

}