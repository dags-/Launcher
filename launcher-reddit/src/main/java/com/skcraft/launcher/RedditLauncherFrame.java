/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.swing.*;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;

@Log
public class RedditLauncherFrame extends LauncherFrame {

    private static final Theme theme = new Theme();
    private static final Dimension PRIM = new Dimension(125, 55);
    private static final Dimension SEC = new Dimension(110, 30);

    public RedditLauncherFrame(@NonNull final Launcher launcher) {
        super(launcher);
        setMinimumSize(new Dimension(720, 440));
    }

    @Override
    public WebpagePanel createNewsPanel() {
        return WebpagePanel.forHTML("");
    }

    @Override
    public JButton createPrimaryButton(String name) {
        JButton button = new ColoredButton(name, theme.primary, theme.primaryAlt);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, theme.primarySize));
        button.setForeground(theme.primaryText);
        button.setPreferredSize(PRIM);
        return button;
    }

    @Override
    protected JButton createSecondaryButton(String name) {
        JButton button = new ColoredButton(name, theme.secondary, theme.secondaryAlt);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, theme.secondarySize));
        button.setForeground(theme.secondaryText);
        button.setPreferredSize(SEC);
        return button;
    }

    @Override
    protected JCheckBox createCheckBox(String name) {
        JCheckBox box = new JCheckBox(name);
        box.setFont(new Font(box.getFont().getName(), Font.PLAIN, theme.secondarySize));
        box.setBackground(theme.secondary);
        box.setForeground(theme.secondaryText);
        box.setPreferredSize(SEC);
        box.setHorizontalAlignment(SwingConstants.CENTER);
        return box;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        getContentPane().removeAll();
        instancesTable.setBackground(new Color(0, 0, 0, 0));
        instancesTable.setSelectionBackground(theme.primary);
        instancesTable.setSelectionForeground(theme.primaryText);
        instancesTable.setForeground(theme.secondaryText);
        instancesTable.setFont(new Font(instancesTable.getFont().getName(), Font.PLAIN, theme.secondarySize));
        instancesTable.setOpaque(false);
        redditInit();
    }

    private void redditInit() {
        String address = String.format("https://reddit.com/r/%s.json", theme.subreddit);
        log.info("Set reddit url " + address);
        RedditBackgroundPanel root = new RedditBackgroundPanel(address, 8000L);

        JPanel launchControls = new JPanel();
        launchControls.setOpaque(false);
        launchControls.add(selfUpdateButton);
        launchControls.add(optionsButton);
        launchControls.add(launchButton);

        JPanel updateControls = new JPanel();
        updateControls.add(refreshButton);
        updateControls.add(updateCheck);
        updateControls.setBackground(getAltFrostColor(theme.frost));

        JPanel left = new FrostPanel(root, theme.frost);
        left.setLayout(new BorderLayout());
        left.add(instancesTable, BorderLayout.CENTER);
        left.add(updateControls, BorderLayout.PAGE_END);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(getHeaderImage(), BorderLayout.PAGE_START);
        center.add(launchControls, BorderLayout.PAGE_END);

        root.setLayout(new BorderLayout());
        root.add(left, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);

        add(root);
    }

    private JLabel getHeaderImage() {
        JLabel label = new JLabel();
        Image image = SwingHelper.createImage(RedditLauncher.class, "/com/skcraft/launcher/header.png");
        if (image != null) {
            label = new JLabel(new ImageIcon(image));
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return label;
    }

    private Color getAltFrostColor(Color c) {
        int r = Math.min(c.getRed() + 20, 255);
        int g = Math.min(c.getGreen() + 20, 255);
        int b = Math.min(c.getBlue() + 20, 255);
        int a = Math.min(c.getAlpha() + 40, 255);
        return new Color(r, g, b, a);
    }
}
