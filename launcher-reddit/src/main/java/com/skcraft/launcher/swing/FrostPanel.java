package com.skcraft.launcher.swing;

import com.skcraft.launcher.util.BlurFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author dags <dags@dags.me>
 */
public class FrostPanel extends JPanel {

    private final BlurFilter filter = new BlurFilter(10F);
    private final Paintable under;
    private final Color color;

    public FrostPanel(Paintable under, Color frostColor) {
        this.under = under;
        this.color = frostColor;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (color.getAlpha() < 250) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics g = image.createGraphics();
            under.paintComponent(g);
            g.dispose();

            BufferedImage blurred = filter.filter(image, null);
            graphics.drawImage(blurred, 0, 0, getWidth(), getHeight(), null);
        }

        graphics.setColor(color);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }
}
