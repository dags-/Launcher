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
    private BufferedImage cached = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public FrostPanel(Paintable under) {
        this.under = under;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (getWidth() != cached.getWidth() || getHeight() != cached.getHeight()) {
            cached = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics g = cached.createGraphics();
        under.paintComponent(g);
        g.dispose();

        BufferedImage blurred = filter.filter(cached, null);
        graphics.drawImage(blurred, 0, 0, getWidth(), getHeight(), Color.black, this);
    }
}
