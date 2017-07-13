package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * @author dags <dags@dags.me>
 */
public class ImageFader implements ActionListener {

    private final Timer timer = new Timer(1, this);
    private final BufferedImage from;
    private final BufferedImage to;
    private final long duration;
    private final long start;

    private float alpha = 0F;

    public ImageFader(BufferedImage from, BufferedImage to, long duration) {
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.start = System.currentTimeMillis();
        if (from != null && to != null) {
            timer.start();
        }
    }

    public BufferedImage getFrom() {
        return from;
    }

    public BufferedImage getTo() {
        return to;
    }

    public boolean isFading() {
        return timer.isRunning();
    }

    public void paint(Component parent, Graphics graphics, int windowWidth, int windowHeight) {
        Graphics2D g = (Graphics2D) graphics;
        Composite c = g.getComposite();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (isFading() && from != null) {
            g.setComposite(AlphaComposite.SrcOver.derive(1F - alpha));
            drawScaled(g, parent, from, windowWidth, windowHeight);
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        }

        drawScaled(g, parent, to, windowWidth, windowHeight);
        g.setComposite(c);
    }

    private void drawScaled(Graphics graphics, Component parent, BufferedImage image, int windowWidth, int windowHeight) {
        float scale = getScale(windowWidth, windowHeight, image.getWidth(), image.getHeight());
        int width = scale(image.getWidth(), windowWidth, scale);
        int height = scale(image.getHeight(), windowHeight, scale);
        int xOff = (width - windowWidth) / 2;
        int yOff = (height - windowHeight) / 2;
        graphics.drawImage(image, -xOff, -yOff, width, height, parent);
    }

    private float getScale(int windowW, int windowH, int imageW, int imageH) {
        float fw = windowW / (float) imageW;
        float fh = windowH / (float) imageH;
        return Math.max(fw, fh);
    }

    private int scale(int imageDim, int windowDim, float scale) {
        int scaled = Math.round(imageDim * scale);
        return Math.max(scaled, windowDim);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        float time = System.currentTimeMillis() - start;

        alpha = time / duration;

        if (alpha >= 1F) {
            timer.stop();
        }
    }
}
