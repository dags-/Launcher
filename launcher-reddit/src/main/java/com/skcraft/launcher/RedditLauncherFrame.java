/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.swing.RedditBackgroundPanel;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class RedditLauncherFrame extends LauncherFrame implements WindowFocusListener {

    private final Launcher launcher;
    private JPanel updateControls;

    public RedditLauncherFrame(@NonNull final Launcher launcher) {
        super(launcher);
        this.launcher = launcher;
        setMinimumSize(new Dimension(700, 420));
        addWindowFocusListener(this);
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
    }

    private void redditInit() {
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

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(instancesTable, BorderLayout.CENTER);
        left.add(updateControls, BorderLayout.PAGE_END);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(headerImage, BorderLayout.PAGE_START);
        center.add(launchControls, BorderLayout.PAGE_END);

        JPanel root = new RedditBackgroundPanel("wallpapers/new", 7000L);
        root.setLayout(new BorderLayout());
        root.add(left, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);

        add(root);
    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        refreshBackgrounds();
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {

    }

    private void refreshBackgrounds() {
        Configuration c = launcher.getConfig();
        Color col1 = new Color(c.getBackgroundRed(), c.getBackgroundGreen(), c.getBackgroundBlue(), c.getBackgroundAlpha());
        instancesTable.setBackground(col1);

        int r = Math.min(c.getBackgroundRed() + 20, 255);
        int g = Math.min(c.getBackgroundGreen() + 20, 255);
        int b = Math.min(c.getBackgroundBlue() + 20, 255);
        int a = Math.min(c.getBackgroundAlpha() + 40, 255);

        Color col2 = new Color(r, g, b, a);
        updateControls.setBackground(col2);

        repaint();
    }
}
