/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.swing.*;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class RedditLauncherFrame extends LauncherFrame {

    private JPanel updateControls;
    private Properties theme;

    public RedditLauncherFrame(@NonNull final Launcher launcher) {
        super(launcher);
        setMinimumSize(new Dimension(700, 420));
    }

    @Override
    public WebpagePanel createNewsPanel() {
        return WebpagePanel.forHTML("");
    }

    @Override
    public JButton createPrimaryButton(String name) {
        Color unpressed = Color.decode(getTheme().getProperty("button.primary.color"));
        Color pressed = Color.decode(getTheme().getProperty("button.primary.pressed.color"));
        Color text = Color.decode(getTheme().getProperty("button.primary.text.color"));
        int size = Integer.parseInt(getTheme().getProperty("button.primary.text.size"));
        JButton button = new ColoredButton(name, unpressed, pressed);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, size));
        button.setForeground(text);
        return button;
    }

    @Override
    protected JButton createSecondaryButton(String name) {
        Color unpressed = Color.decode(getTheme().getProperty("button.secondary.color"));
        Color pressed = Color.decode(getTheme().getProperty("button.secondary.pressed.color"));
        Color text = Color.decode(getTheme().getProperty("button.secondary.text.color"));
        int size = Integer.parseInt(getTheme().getProperty("button.secondary.text.size"));
        JButton button = new ColoredButton(name, unpressed, pressed);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, size));
        button.setForeground(text);
        return button;
    }

    @Override
    protected JCheckBox createCheckBox(String name) {
        Color unpressed = Color.decode(getTheme().getProperty("button.secondary.color"));
        Color text = Color.decode(getTheme().getProperty("button.secondary.text.color"));
        int size = Integer.parseInt(getTheme().getProperty("button.secondary.text.size"));
        JCheckBox box = new JCheckBox(name);
        box.setFont(new Font(box.getFont().getName(), Font.PLAIN, size));
        box.setBackground(unpressed);
        box.setForeground(text);
        return box;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        getContentPane().removeAll();

        refreshButton.setPreferredSize(new Dimension(100, 30));
        updateCheck.setPreferredSize(new Dimension(100, 30));
        updateCheck.setHorizontalAlignment(SwingConstants.CENTER);

        optionsButton.setPreferredSize(new Dimension(120, 40));
        launchButton.setPreferredSize(new Dimension(120, 40));

        redditInit();
        refreshBackgrounds();
    }

    private void redditInit() {
        String address = String.format("https://reddit.com/r/%s.json", getTheme().getProperty("subbreddit"));
        RedditBackgroundPanel root = new RedditBackgroundPanel(address, 8000L);

        JPanel launchControls = new JPanel();
        launchControls.setOpaque(false);
        launchControls.add(selfUpdateButton);
        launchControls.add(optionsButton);
        launchControls.add(launchButton);

        updateControls = new JPanel();
        updateControls.add(refreshButton);
        updateControls.add(updateCheck);

        JLabel headerImage = new JLabel();
        Image image = SwingHelper.createImage(RedditLauncher.class, "/com/skcraft/launcher/header.png");
        if (image != null) {
            headerImage = new JLabel(new ImageIcon(image));
            headerImage.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        JPanel left = new FrostPanel(root);
        left.setLayout(new BorderLayout());
        left.setOpaque(false);
        left.add(instancesTable, BorderLayout.CENTER);
        left.add(updateControls, BorderLayout.PAGE_END);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(headerImage, BorderLayout.PAGE_START);
        center.add(launchControls, BorderLayout.PAGE_END);

        root.setLayout(new BorderLayout());
        root.add(left, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);

        add(root);
    }

    private Properties getTheme() {
        if (theme == null) {
            Properties theme = new Properties();
            try {
                theme.load(RedditLauncherFrame.class.getResourceAsStream("/com/skcraft/launcher/theme.properties"));
                this.theme = theme;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return theme;
    }

    private void refreshBackgrounds() {
        Color background = Color.decode(getTheme().getProperty("list.background.color"));
        int alpha = Math.round(256 * Float.parseFloat(getTheme().getProperty("list.background.alpha")));
        alpha = Math.min(alpha, 255);

        Color col1 = new Color(background.getRed(), background.getGreen(), background.getBlue(), alpha);
        instancesTable.setBackground(col1);

        int r = Math.min(background.getRed() + 20, 255);
        int g = Math.min(background.getGreen() + 20, 255);
        int b = Math.min(background.getBlue() + 20, 255);
        int a = Math.min(alpha + 40, 255);

        Color col2 = new Color(r, g, b, a);
        updateControls.setBackground(col2);

        repaint();
    }
}
