/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cs4474.wordtropolis.view;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Sarah VanDuzer FlameLabel is a JLabel that handles flame animation
 * (ignite/extinguish/reignite). It does NOT add itself to any panel or set
 * coordinates.
 */
public class FlameLabel extends JLabel {

    private ArrayList<ImageIcon> startFrames = new ArrayList<>();
    private ArrayList<ImageIcon> loopFrames = new ArrayList<>();
    private ArrayList<ImageIcon> endFrames = new ArrayList<>();

    private final int startFrameCount = 12;
    private final int endFrameCount = 14;

    private final int frameWidth = 24;
    private final int frameHeight = 32;

    private int frameDelay = 100; // ms per frame
    private Timer animationTimer;
    private int currentFrameIndex = 0;
    private ArrayList<ImageIcon> currentFrames;

    private boolean isActive = false; // Whether the fire is burning
    private boolean isAnimating = false;

    /**
     * Constructor to initialize the FlameLabel with start, loop, and end
     * animations
     *
     * @param startSheet
     * @param loopSheet
     * @param endSheet
     */
    public FlameLabel(BufferedImage startSheet, BufferedImage loopSheet, BufferedImage endSheet) {
        this.startFrames = extractFramesFromSheet(startSheet, frameWidth, frameHeight);
        this.loopFrames = extractFramesFromSheet(loopSheet, frameWidth, frameHeight);
        this.endFrames = extractFramesFromSheet(endSheet, frameWidth, frameHeight);

        if (!this.startFrames.isEmpty()) {
            this.setIcon(this.startFrames.get(0)); // Default to start animation first frame
        }
    }

    /**
     * Extract frames from a horizontal sprite sheet
     */
    private ArrayList<ImageIcon> extractFramesFromSheet(BufferedImage sheet, int frameWidth, int frameHeight) {
        ArrayList<ImageIcon> frames = new ArrayList<>();
        if (sheet == null) {
            return frames;
        }

        int cols = sheet.getWidth() / frameWidth;
        int rows = sheet.getHeight() / frameHeight;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                BufferedImage frame = sheet.getSubimage(x * frameWidth, y * frameHeight, frameWidth, frameHeight);
                frames.add(new ImageIcon(frame));
            }
        }
        return frames;
    }

    /**
     * Ignite the fire, playing the start animation and then switching to the
     * loop animation
     */
    public void ignite() {
        if (isActive || isAnimating) {
            return; // Fire is already active or animation is in progress
        }
        isActive = true;
        isAnimating = true;
        playFrames(startFrames, startFrameCount, true); // Play start animation

        System.out.println("End frames: " + endFrameCount);
        System.out.println("Start frames: " + startFrameCount);
    }

    /**
     * Extinguish the fire, playing the end animation once
     */
    public void extinguish() {
        if (!isActive || isAnimating) {
            return; // Fire is not active or animation is in progress
        }
        isActive = false;
        playFrames(endFrames, endFrameCount, false); // Play end animation once
        isAnimating = false;
    }

    /**
     * Play a sequence of frames (start, loop, or end animation)
     */
    private void playFrames(ArrayList<ImageIcon> frames, int maxFrames, boolean switchToLoop) {
        if (frames.isEmpty()) {
            return;
        }

        currentFrames = frames;
        currentFrameIndex = 0;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop(); // Stop previous animation if running
        }

        animationTimer = new Timer(frameDelay, e -> {
            setIcon(currentFrames.get(currentFrameIndex));
            currentFrameIndex++;

            int totalFrames = (maxFrames > 0 && maxFrames <= currentFrames.size())
                    ? maxFrames
                    : currentFrames.size();

            if (currentFrameIndex >= totalFrames) {
                ((Timer) e.getSource()).stop();

                // After the start animation, switch to the loop animation
                if (switchToLoop && isActive) {
                    playLoop(); // Start the loop animation for the fire
                } else {
                    isAnimating = false;
                    if (!isActive) {
                        setIcon(null); // Stop after end sheet is completed
                    }
                }
            }
        });

        animationTimer.start();
    }

    /**
     * Start the loop animation (repeated indefinitely)
     */
    private void playLoop() {
        if (loopFrames.isEmpty()) {
            return;
        }

        currentFrames = loopFrames;
        currentFrameIndex = 0;

        animationTimer = new Timer(frameDelay, e -> {
            setIcon(currentFrames.get(currentFrameIndex));
            currentFrameIndex = (currentFrameIndex + 1) % currentFrames.size();
        });
        animationTimer.start();
        isAnimating = false;
    }

    public boolean isActive() {
        return isActive; // Check if the fire is active (burning)
    }

    public void setFrameDelay(int delayMs) {
        this.frameDelay = delayMs; // Set custom frame delay
    }
}
