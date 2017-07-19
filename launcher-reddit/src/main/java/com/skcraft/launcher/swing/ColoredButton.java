package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author dags <dags@dags.me>
 */
public class ColoredButton extends JButton implements MouseListener {

    private final JLabel label;
    private final Color pressed;
    private final Color background;

    private boolean mousePressed = false;

    public ColoredButton(String text, Color background, Color pressed) {
        this.label = new JLabel(text);
        this.background = background;
        this.pressed = pressed;
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        graphics.setColor(mousePressed ? pressed : background);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        label.setSize(getWidth(), getHeight());
        label.setForeground(getForeground());
        label.setFont(getFont());
        label.paint(graphics);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
