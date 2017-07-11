package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author dags <dags@dags.me>
 */
public class BackgroundPanel extends JPanel {

    private final BufferedImage background;

    public BackgroundPanel(BufferedImage image) {
        this.background = image;
    }

    @Override
    public void paint(Graphics graphics) {
        // todo: scale not stretch
        graphics.drawImage(background, 0, 0, getWidth(), getHeight(), Color.black, this);
        super.paint(graphics);
    }
}
