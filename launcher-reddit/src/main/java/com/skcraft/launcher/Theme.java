package com.skcraft.launcher;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

/**
 * @author dags <dags@dags.me>
 */
public class Theme {

    public final String subreddit;
    public final Color primary;
    public final Color primaryAlt;
    public final Color primaryText;
    public final Color secondary;
    public final Color secondaryAlt;
    public final Color secondaryText;
    public final Color frost;
    public final int primarySize;
    public final int secondarySize;

    public Theme() {
        String subreddit = "";
        Color primary = Color.white;
        Color primaryAlt = Color.white;
        Color primaryText = Color.white;
        Color secondary = Color.white;
        Color secondaryAlt = Color.white;
        Color secondaryText = Color.white;
        Color frost = Color.white;
        int primarySize = 14;
        int secondarySize = 11;

        Properties p = new Properties();
        try {
            p.load(getClass().getResourceAsStream("/com/skcraft/launcher/theme.properties"));
            subreddit = p.getProperty("subreddit");

            primary = Color.decode(p.getProperty("primary.color"));
            primaryAlt = Color.decode(p.getProperty("primary.color.alt"));
            primaryText = Color.decode(p.getProperty("primary.text.color"));
            primarySize = Integer.parseInt(p.getProperty("primary.text.size"));

            secondary = Color.decode(p.getProperty("secondary.color"));
            secondaryAlt = Color.decode(p.getProperty("secondary.color.alt"));
            secondaryText = Color.decode(p.getProperty("secondary.text.color"));
            secondarySize = Integer.parseInt(p.getProperty("secondary.text.size"));

            Color c = Color.decode(p.getProperty("frost.color"));
            int alpha = Math.round(255 * Float.parseFloat(p.getProperty("frost.alpha")));
            frost = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, Math.max(0, alpha)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.subreddit = subreddit;
        this.primary = primary;
        this.primaryAlt = primaryAlt;
        this.primaryText = primaryText;
        this.primarySize = primarySize;
        this.secondary = secondary;
        this.secondaryAlt = secondaryAlt;
        this.secondaryText = secondaryText;
        this.secondarySize = secondarySize;
        this.frost = frost;
    }
}
