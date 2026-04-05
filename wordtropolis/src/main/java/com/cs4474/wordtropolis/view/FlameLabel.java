/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cs4474.wordtropolis.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Sarah VanDuzer FlameLabel is a JLabel that handles flame animation
 * (ignite/extinguish/reignite). It does NOT add itself to any panel or set
 * coordinates.
 */
public class FlameLabel extends JLabel {

    private ArrayList<ImageIcon> startFrames;
    private ArrayList<ImageIcon> loopFrames;
    private ArrayList<ImageIcon> endFrames;

    private final int frameWidth = 24;
    private final int frameHeight = 32;

    private int frameDelay = 100;
    private Timer animationTimer;
    private int currentFrameIndex = 0;
    private ArrayList<ImageIcon> currentFrames = null;

    private boolean isActive = false;
    private boolean isAnimating = false;

    private final JPanel observer;

    public FlameLabel(BufferedImage startSheet, BufferedImage loopSheet, BufferedImage endSheet, JPanel observer) {
        // Convert sheets to compatible format first
        BufferedImage compStart = toCompatibleImage(startSheet);
        BufferedImage compLoop = toCompatibleImage(loopSheet);
        BufferedImage compEnd = toCompatibleImage(endSheet);

        // Then extract frames from the optimized sheets
        this.startFrames = extractFramesFromSheet(compStart, frameWidth, frameHeight);
        this.loopFrames = extractFramesFromSheet(compLoop, frameWidth, frameHeight);
        this.endFrames = extractFramesFromSheet(compEnd, frameWidth, frameHeight);

        // Ensure the label is transparent so only the flame shows
        setOpaque(false);

        this.observer = observer;
    }

    private ArrayList<ImageIcon> extractFramesFromSheet(BufferedImage sheet, int w, int h) {
        ArrayList<ImageIcon> frames = new ArrayList<>();
        if (sheet == null) {
            return frames;
        }

        int cols = sheet.getWidth() / w;
        int rows = sheet.getHeight() / h;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                frames.add(new ImageIcon(sheet.getSubimage(x * w, y * h, w, h)));
            }
        }
        return frames;
    }

    public void ignite() {
        if (isActive || isAnimating) {
            return;
        }

        isActive = true;
        isAnimating = true;
        playFrames(startFrames, true);
    }

    public void extinguish() {
        // Allow extinguishing even if "igniting" is still playing
        if (!isActive) {
            return;
        }

        isActive = false;
        isAnimating = true; // Mark as animating the exit
        playFrames(endFrames, false);
    }

    private void playFrames(ArrayList<ImageIcon> frames, boolean switchToLoop) {
        if (frames == null || frames.isEmpty()) {
            isAnimating = false;
            return;
        }

        currentFrames = frames;
        currentFrameIndex = 0;

        if (animationTimer != null) {
            animationTimer.stop();
        }

        animationTimer = new Timer(frameDelay, e -> {
            if (currentFrameIndex < currentFrames.size() - 1) {
                currentFrameIndex++;
                triggerRepaint(); // Tell the panel a new frame is ready
            } else {
                ((Timer) e.getSource()).stop();
                if (switchToLoop && isActive) {
                    playLoop();
                } else {
                    isAnimating = false;
                    currentFrames = null;
                    triggerRepaint();
                }
            }
        });

        triggerRepaint(); // Show frame 0 immediately
        animationTimer.start();
    }

    private void playLoop() {
        if (loopFrames.isEmpty()) {
            return;
        }

        currentFrames = loopFrames;
        currentFrameIndex = 0;
        isAnimating = false; // We are now in "idle" state

        if (animationTimer != null) {
            animationTimer.stop();
        }

        animationTimer = new Timer(frameDelay, e -> {
            currentFrameIndex = (currentFrameIndex + 1) % currentFrames.size();
            triggerRepaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentFrames != null && currentFrameIndex < currentFrames.size()) {
            Image frameImage = currentFrames.get(currentFrameIndex).getImage();
            // Using RenderingHints for smoother scaling if label size != frame size
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(frameImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Helper
    private BufferedImage toCompatibleImage(BufferedImage image) {
        GraphicsConfiguration gf = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        /*
         * Create a new image that is compatible with the screen.
         * Transparency.TRANSLUCENT ensures the alpha channel (transparency) is preserved,
         * which is vital for flame effects.
         */
        BufferedImage compatibleImage = gf.createCompatibleImage(
                image.getWidth(),
                image.getHeight(),
                Transparency.TRANSLUCENT);

        // Draw the original image onto the new compatible image
        Graphics2D g2d = compatibleImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return compatibleImage;

    }

    private void triggerRepaint() {
        if (observer != null) {
            observer.repaint();
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
