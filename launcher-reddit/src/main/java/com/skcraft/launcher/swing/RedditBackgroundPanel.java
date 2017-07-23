package com.skcraft.launcher.swing;

import com.skcraft.launcher.util.Closer;
import com.skcraft.launcher.util.RedditUtils;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class RedditBackgroundPanel extends JPanel implements Runnable, ActionListener, Paintable {

    private static final ImageFader EMPTY = getEmptyFader();

    private final AtomicReference<ImageFader> reference;
    private final AtomicBoolean showing;
    private final AtomicBoolean repaint;
    private final String address;
    private final Timer timer;
    private final long delay;

    public RedditBackgroundPanel(String address, long interval) {
        this.reference = new AtomicReference<ImageFader>(EMPTY);
        this.showing = new AtomicBoolean(true);
        this.repaint = new AtomicBoolean(true);
        this.timer = new Timer(200, this);
        this.address = address;
        this.delay = interval;
        this.timer.start();

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        ImageFader image = reference.get();
        image.paint(this, graphics, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (reference.get().isFading()) {
            repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isShowing()) {
            if (repaint.get()) {
                repaint.set(false);
                repaint();
            }
        } else {
            timer.stop();
            showing.set(false);
        }
    }

    @Override
    public void run() {
        int index = 0;
        List<String> targets = RedditUtils.getBackgrounds(address);
        Collections.shuffle(targets);

        // async
        while (showing.get() && index < targets.size()) {
            try {
                String next = targets.get(index);
                ImageFader image = getImage(next);

                if (image != null) {
                    reference.set(image);
                    repaint.set(true);
                }

                if (++index >= targets.size()) {
                    index = 0;
                }

                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private static ImageFader getEmptyFader() {
        BufferedImage blank = new BufferedImage(720, 480, BufferedImage.TYPE_INT_RGB);
        return new ImageFader(blank, blank, 1000);
    }
}
