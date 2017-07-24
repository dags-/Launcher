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
import java.util.ArrayList;

@Log
public class RedditLauncherFrame extends LauncherFrame {

    public RedditLauncherFrame(@NonNull final Launcher launcher) {
        super(launcher);
        setIcons();
        setMinimumSize(new Dimension(720, 440));
    }

    @Override
    public WebpagePanel createNewsPanel() {
        return WebpagePanel.forHTML("");
    }

    @Override
    public JButton createPrimaryButton(String name) {
        JButton button = new ColoredButton(name, Theme.primary, Theme.primaryAlt);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, Theme.primarySize));
        button.setForeground(Theme.primaryText);
        button.setPreferredSize(Theme.primaryButtonSize);
        return button;
    }

    @Override
    protected JButton createSecondaryButton(String name) {
        JButton button = new ColoredButton(name, Theme.secondary, Theme.secondaryAlt);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, Theme.secondarySize));
        button.setForeground(Theme.secondaryText);
        button.setPreferredSize(Theme.secondaryButtonSize);
        return button;
    }

    @Override
    protected JCheckBox createCheckBox(String name) {
        JCheckBox box = new JCheckBox(name);
        box.setFont(new Font(box.getFont().getName(), Font.PLAIN, Theme.secondarySize));
        box.setBackground(Theme.secondary);
        box.setForeground(Theme.secondaryText);
        box.setPreferredSize(Theme.secondaryButtonSize);
        box.setHorizontalAlignment(SwingConstants.CENTER);
        return box;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        getContentPane().removeAll();
        instancesTable.setSelectionBackground(Theme.primary);
        instancesTable.setSelectionForeground(Theme.primaryText);
        instancesTable.setForeground(Theme.secondaryText);
        instancesTable.setFont(new Font(instancesTable.getFont().getName(), Font.PLAIN, Theme.secondarySize));
        instancesTable.setOpaque(false);
        redditInit();
    }

    private void redditInit() {
        String address = String.format("https://reddit.com/r/%s.json", Theme.subreddit);
        log.info("Set reddit url " + address);
        RedditBackgroundPanel root = new RedditBackgroundPanel(address, 8000L, Theme.randomise);

        JPanel launchControls = new JPanel();
        launchControls.setOpaque(false);
        launchControls.add(selfUpdateButton);
        launchControls.add(optionsButton);
        launchControls.add(launchButton);

        JPanel updateControls = new JPanel();
        updateControls.add(refreshButton);
        updateControls.add(updateCheck);
        updateControls.setBackground(getAltFrostColor(Theme.frost));

        JPanel left = new FrostPanel(root, Theme.frost);
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

    private void setIcons() {
        Image mainIcon = SwingHelper.createImage(LauncherFrame.class, "/com/skcraft/launcher/icon.png");
        Image titleIcon = SwingHelper.createImage(LauncherFrame.class, "/com/skcraft/launcher/title.png");
        ArrayList<Image> icons = new ArrayList<Image>();
        if (mainIcon != null) {
            icons.add(mainIcon);
        }
        if (titleIcon != null) {
            icons.add(titleIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }
        setIconImages(icons);
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
