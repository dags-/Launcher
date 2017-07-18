package com.skcraft.launcher.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class Closer {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
