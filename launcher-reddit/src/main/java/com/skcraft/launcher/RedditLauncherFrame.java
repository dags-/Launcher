/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.swing.FrostPanel;
import com.skcraft.launcher.swing.RedditBackgroundPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.WebpagePanel;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class RedditLauncherFrame extends LauncherFrame implements WindowFocusListener {

    private JPanel updateControls;

    public RedditLauncherFrame(@NonNull final Launcher launcher) {
        super(launcher);
        setMinimumSize(new Dimension(700, 420));
        addWindowFocusListener(this);
    }

    @Override
    public WebpagePanel createNewsPanel() {
        return WebpagePanel.forHTML("");
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
        RedditBackgroundPanel root = new RedditBackgroundPanel(getSubReddit(), 8000L);

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

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        refreshBackgrounds();
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {

    }

    private String getSubReddit() {
        String news = launcher.prop("newsUrl");
        if (!news.contains("reddit.com")) {
            news = "https://reddit.com/r/wallpapers/new.json";
        }

        if (!news.endsWith(".json")) {
            news = news + ".json";
        }
        return news;
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
