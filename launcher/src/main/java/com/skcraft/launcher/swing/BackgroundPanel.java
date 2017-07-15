package com.skcraft.launcher.swing;

import com.google.common.collect.ImmutableList;
import com.skcraft.launcher.util.Closer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class BackgroundPanel extends JPanel implements ActionListener {

    private final AtomicReference<ImageFader> reference = new AtomicReference<ImageFader>();
    private final AtomicBoolean showing = new AtomicBoolean(true);
    private final List<String> targets;
    private final Timer timer;
    private final long delay;

    public BackgroundPanel(List<String> images, long interval) {
        this.targets = ImmutableList.copyOf(images);
        this.delay = interval;
        this.timer = new Timer(200, this);
        timer.start();

        if (images.size() > 0) {
            this.reference.set(getImage(images.get(0)));
            new Thread(asyncTask()).start();
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
    public void actionPerformed(ActionEvent e) {
        if (isShowing()) {
            repaint();
        } else {
            showing.set(false);
            timer.stop();
        }
    }

    private Runnable asyncTask() {
        return new Runnable() {
            @Override
            public void run() {
                int index = 1;
                do {
                    try {
                        Thread.sleep(delay);

                        String next = targets.get(index);
                        ImageFader image = getImage(next);

                        if (image != null) {
                            reference.set(image);
                        }

                        if (++index >= targets.size()) {
                            index = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (showing.get());
            }
        };
    }

    private ImageFader getImage(String address) {
        InputStream inputStream = null;
        try {
            ImageFader current = reference.get();
            BufferedImage from = current != null ? current.getTo() : null;
            URL url = new URL(address);
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            BufferedImage to = ImageIO.read(inputStream);
            return new ImageFader(from, to, 1000L);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(inputStream);
        }
        return null;
    }
}
