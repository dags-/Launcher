/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.util.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.skcraft.launcher.util.SharedLocale.tr;

/**
 * The main launcher frame.
 */
@Log
public class LauncherFrame extends JFrame {

    private final Launcher launcher;

    private final Color transparent = new Color(0, 0, 0, 0);
    @Getter
    private final InstanceTable instancesTable = new InstanceTable();
    private final InstanceTableModel instancesModel;
    @Getter
    private final JScrollPane instanceScroll = new JScrollPane(instancesTable);
    private final JButton launchButton = createButton(SharedLocale.tr("launcher.launch"));
    private final JButton refreshButton = createButton(SharedLocale.tr("launcher.checkForUpdates"));
    private final JButton optionsButton = createButton(SharedLocale.tr("launcher.options"));
    private final JButton selfUpdateButton = createButton(SharedLocale.tr("launcher.updateLauncher"));
    private final JCheckBox updateCheck = new JCheckBox(SharedLocale.tr("launcher.downloadUpdates"));

    /**
     * Create a new frame.
     *
     * @param launcher the launcher
     */
    public LauncherFrame(@NonNull Launcher launcher) {
        super(tr("launcher.title", launcher.getVersion()));

        this.launcher = launcher;
        instancesModel = new InstanceTableModel(launcher.getInstances());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(650, 360));
        initComponents();
        pack();
        setLocationRelativeTo(null);

        SwingHelper.setFrameIcon(this, LauncherFrame.class, "/com/skcraft/launcher/icon.png");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                loadInstances();
            }
        });
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private JPanel loadLogo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBackground(transparent);
        panel.setLayout(new BorderLayout());

        Image image = SwingHelper.readBufferedImage(LauncherFrame.class,  "/com/skcraft/launcher/header.png");
        if (image != null) {
            ImageIcon icon = new ImageIcon(image);
            JLabel label = new JLabel(icon);
            panel.add(label, BorderLayout.LINE_END);
            return panel;
        }

        return panel;
    }

    private void initComponents() {
        JPanel container = createContainerPanel();
        container.setBackground(transparent);
        container.setLayout(new BorderLayout());

        selfUpdateButton.setVisible(launcher.getUpdateManager().getPendingUpdate());

        launcher.getUpdateManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("pendingUpdate")) {
                    selfUpdateButton.setVisible((Boolean) evt.getNewValue());
                }
            }
        });

        updateCheck.setSelected(true);
        instancesTable.setModel(instancesModel);
        instancesTable.setBackground(new Color(255, 255, 255, 96));
        optionsButton.setPreferredSize(new Dimension(125, 40));
        launchButton.setPreferredSize(new Dimension(125, 40));
        launchButton.setFont(launchButton.getFont().deriveFont(Font.BOLD));

        JPanel updateControls = new JPanel();
        updateControls.setMaximumSize(new Dimension(900, 30));
        updateControls.setLayout(new GridLayout());
        updateControls.add(refreshButton);
        updateControls.add(updateCheck);

        JPanel launchControls = new JPanel();
        launchControls.setBackground(transparent);
        launchControls.add(optionsButton);
        launchControls.add(launchButton);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setBackground(transparent);
        left.setLayout(new BorderLayout());
        left.add(instancesTable, BorderLayout.CENTER);
        left.add(updateControls, BorderLayout.PAGE_END);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setBackground(transparent);
        right.setLayout(new BorderLayout());
        right.add(loadLogo(), BorderLayout.PAGE_START);
        right.add(launchControls, BorderLayout.PAGE_END);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(4);
        splitPane.setBackground(transparent);
        container.add(splitPane, BorderLayout.CENTER);
        SwingHelper.flattenJSplitPane(splitPane);

        add(container, BorderLayout.CENTER);

        instancesModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (instancesTable.getRowCount() > 0) {
                    instancesTable.setRowSelectionInterval(0, 0);
                }
            }
        });

        instancesTable.addMouseListener(new DoubleClickToButtonAdapter(launchButton));

        instancesTable.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                repaint();
            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });

        instancesTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances();
                launcher.getUpdateManager().checkForUpdate();
            }
        });

        selfUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launcher.getUpdateManager().performUpdate(LauncherFrame.this);
            }
        });

        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOptions();
            }
        });

        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });

        instancesTable.addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                int index = instancesTable.rowAtPoint(e.getPoint());
                Instance selected = null;
                if (index >= 0) {
                    instancesTable.setRowSelectionInterval(index, index);
                    selected = launcher.getInstances().get(index);
                }
                popupInstanceMenu(e.getComponent(), e.getX(), e.getY(), selected);
            }
        });
    }

    protected JPanel createContainerPanel() {
        return new BackgroundPanel(getBackgroundURLs(), 10000L);
    }

    /**
     * Return the news panel.
     *
     * @return the news panel
     */
    protected WebpagePanel createNewsPanel() {
        return WebpagePanel.forURL(launcher.getNewsURL(), false);
    }

    /**
     * Popup the menu for the instances.
     *
     * @param component the component
     * @param x         mouse X
     * @param y         mouse Y
     * @param selected  the selected instance, possibly null
     */
    private void popupInstanceMenu(Component component, int x, int y, final Instance selected) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null) {
            menuItem = new JMenuItem(!selected.isLocal() ? tr("instance.install") : tr("instance.launch"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    launch();
                }
            });
            popup.add(menuItem);

            if (selected.isLocal()) {
                popup.addSeparator();

                menuItem = new JMenuItem(SharedLocale.tr("instance.openFolder"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, selected.getContentDir(), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openSaves"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "saves"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openResourcePacks"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "resourcepacks"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openScreenshots"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "screenshots"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.copyAsPath"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File dir = selected.getContentDir();
                        dir.mkdirs();
                        SwingHelper.setClipboard(dir.getAbsolutePath());
                    }
                });
                popup.add(menuItem);

                popup.addSeparator();

                if (!selected.isUpdatePending()) {
                    menuItem = new JMenuItem(SharedLocale.tr("instance.forceUpdate"));
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            selected.setUpdatePending(true);
                            launch();
                            instancesModel.update();
                        }
                    });
                    popup.add(menuItem);
                }

                menuItem = new JMenuItem(SharedLocale.tr("instance.hardForceUpdate"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        confirmHardUpdate(selected);
                    }
                });
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.deleteFiles"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        confirmDelete(selected);
                    }
                });
                popup.add(menuItem);
            }

            popup.addSeparator();
        }

        menuItem = new JMenuItem(SharedLocale.tr("launcher.refreshList"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances();
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);

    }

    private void confirmDelete(Instance instance) {
        if (!SwingHelper.confirmDialog(this,
                tr("instance.confirmDelete", instance.getTitle()), SharedLocale.tr("confirmTitle"))) {
            return;
        }

        ObservableFuture<Instance> future = launcher.getInstanceTasks().delete(this, instance);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                loadInstances();
            }
        }, SwingExecutor.INSTANCE);
    }

    private void confirmHardUpdate(Instance instance) {
        if (!SwingHelper.confirmDialog(this, SharedLocale.tr("instance.confirmHardUpdate"), SharedLocale.tr("confirmTitle"))) {
            return;
        }

        ObservableFuture<Instance> future = launcher.getInstanceTasks().hardUpdate(this, instance);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                launch();
                instancesModel.update();
            }
        }, SwingExecutor.INSTANCE);
    }

    private void loadInstances() {
        ObservableFuture<InstanceList> future = launcher.getInstanceTasks().reloadInstances(this);

        future.addListener(new Runnable() {
            @Override
            public void run() {
                instancesModel.update();
                if (instancesTable.getRowCount() > 0) {
                    instancesTable.setRowSelectionInterval(0, 0);
                }
                requestFocus();
                LauncherFrame.this.repaint();
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("launcher.checkingTitle"), SharedLocale.tr("launcher.checkingStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void showOptions() {
        ConfigurationDialog configDialog = new ConfigurationDialog(this, launcher);
        configDialog.setVisible(true);
    }

    private void launch() {
        ConsoleFrame.initMessages();

        boolean permitUpdate = updateCheck.isSelected();
        Instance instance = launcher.getInstances().get(instancesTable.getSelectedRow());

        LaunchOptions options = new LaunchOptions.Builder()
                .setInstance(instance)
                .setListener(new LaunchListenerImpl(this))
                .setUpdatePolicy(permitUpdate ? UpdatePolicy.UPDATE_IF_SESSION_ONLINE : UpdatePolicy.NO_UPDATE)
                .setWindow(this)
                .build();
        launcher.getLaunchSupervisor().launch(options);
    }

    private List<String> getBackgroundURLs() {
        String address = launcher.getProperties().getProperty("backgroundsUrl");
        BackgroundProvider provider = getProvider(address);
        return provider.getBackgrounds(address);
    }

    private BackgroundProvider getProvider(String url) {
        if (url.toLowerCase().contains("reddit.com/r/")) {
            return new RedditImageScraper();
        }
        return new JsonImageScraper();
    }

    private static class LaunchListenerImpl implements LaunchListener {

        private final WeakReference<LauncherFrame> frameRef;
        private final Launcher launcher;

        private LaunchListenerImpl(LauncherFrame frame) {
            this.frameRef = new WeakReference<LauncherFrame>(frame);
            this.launcher = frame.launcher;
        }

        @Override
        public void instancesUpdated() {
            LauncherFrame frame = frameRef.get();
            if (frame != null) {
                frame.instancesModel.update();
            }
        }

        @Override
        public void gameStarted() {
            LauncherFrame frame = frameRef.get();
            if (frame != null) {
                frame.dispose();
            }
        }

        @Override
        public void gameClosed() {
            launcher.showLauncherWindow();
        }
    }

}
