package com.skcraft.launcher.swing;

import com.google.common.collect.ImmutableList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class BackgroundPanel extends JPanel implements Runnable {

    private final AtomicReference<ImageFader> reference = new AtomicReference<ImageFader>();
    private final List<String> targets;
    private final long interval;

    public BackgroundPanel(List<String> images, long interval) {
        this.targets = ImmutableList.copyOf(images);
        this.interval = interval;
        if (images.size() > 0) {
            this.reference.set(getImage(images.get(0)));
            new Thread(this).start();
        }
    }

    @Override
    public void paint(Graphics graphics) {
        ImageFader image = reference.get();

        if (image != null) {
            int windowWidth = getWidth();
            int windowHeight = getHeight();

            image.paint(this, graphics, windowWidth, windowHeight);

            if (image.isFading()) {
                repaint();
            }
        }

        super.paint(graphics);
    }

    @Override
    public void run() {
        int index = 1;

        while (true) {
            try {
                Thread.sleep(interval);

                String next = targets.get(index);
                ImageFader image = getImage(next);

                if (image != null) {
                    reference.set(image);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            repaint();
                        }
                    });
                }

                if (++index >= targets.size()) {
                    index = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ImageFader getImage(String address) {
        try {
            ImageFader current = reference.get();
            BufferedImage from = current != null ? current.getTo() : null;

            URL url = new URL(address);
            BufferedImage to = ImageIO.read(url);

            return new ImageFader(from, to, 1000L);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
