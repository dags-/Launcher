package com.skcraft.launcher;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

/**
 * @author dags <dags@dags.me>
 */
public class Theme {

    public static final String subreddit;
    public static final boolean randomise;
    public static final Color primary;
    public static final Color primaryAlt;
    public static final Color primaryText;
    public static final Color secondary;
    public static final Color secondaryAlt;
    public static final Color secondaryText;
    public static final Color frost;
    public static final int primarySize;
    public static final int secondarySize;
    public static final Dimension primaryButtonSize;
    public static final Dimension secondaryButtonSize;

    static  {
        String _subreddit = "";
        boolean _randomise = false;
        Color _primary = Color.white;
        Color _primaryAlt = Color.white;
        Color _primaryText = Color.white;
        Color _secondary = Color.white;
        Color _secondaryAlt = Color.white;
        Color _secondaryText = Color.white;
        Color _frost = Color.white;
        int _primarySize = 14;
        int _secondarySize = 11;
        int primButW = 125;
        int primButH = 55;
        int secButW = 110;
        int secButH = 30;

        Properties p = new Properties();
        try {
            p.load(Theme.class.getResourceAsStream("/com/skcraft/launcher/theme.properties"));
            _subreddit = p.getProperty("subreddit");
            _randomise = Boolean.parseBoolean(p.getProperty("randomise"));

            _primary = Color.decode(p.getProperty("primary.color"));
            _primaryAlt = Color.decode(p.getProperty("primary.color.alt"));
            _primaryText = Color.decode(p.getProperty("primary.text.color"));
            _primarySize = Integer.parseInt(p.getProperty("primary.text.size"));

            _secondary = Color.decode(p.getProperty("secondary.color"));
            _secondaryAlt = Color.decode(p.getProperty("secondary.color.alt"));
            _secondaryText = Color.decode(p.getProperty("secondary.text.color"));
            _secondarySize = Integer.parseInt(p.getProperty("secondary.text.size"));

            Color c = Color.decode(p.getProperty("frost.color"));
            int alpha = Math.round(255 * Float.parseFloat(p.getProperty("frost.opacity")));
            _frost = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, Math.max(0, alpha)));

            primButW = Integer.parseInt(p.getProperty("primary.button.width"));
            primButH = Integer.parseInt(p.getProperty("primary.button.height"));
            secButW = Integer.parseInt(p.getProperty("secondary.button.width"));
            secButH = Integer.parseInt(p.getProperty("secondary.button.height"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        subreddit = _subreddit;
        randomise = _randomise;
        primary = _primary;
        primaryAlt = _primaryAlt;
        primaryText = _primaryText;
        primarySize = _primarySize;
        secondary = _secondary;
        secondaryAlt = _secondaryAlt;
        secondaryText = _secondaryText;
        secondarySize = _secondarySize;
        frost = _frost;
        primaryButtonSize = new Dimension(primButW, primButH);
        secondaryButtonSize = new Dimension(secButW, secButH);
    }
}
