package com.skcraft.launcher.launch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.concurrency.SettableProgress;
import com.skcraft.launcher.util.Closer;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
@Log
public class JavaRuntimeFetcher {

    private static final String LAUNCHER_DATA = "http://launchermeta.mojang.com/mc-staging/launcher.json";

    @Getter
    private final SettableProgress progress;

    public JavaRuntimeFetcher(SettableProgress progress) {
        this.progress = progress;
    }

    public File findJRE() {
        File runtime = getRuntimeDir();
        if (runtime == null) {
            log.log(Level.INFO, "Could not detect runtime dir on platform {0}", Environment.getInstance().getPlatform());
            return null;
        }

        log.log(Level.INFO, "Detected runtime dir: {0}, exists: {1}", new Object[]{runtime, runtime.exists()});

        File x64 = resolve(runtime, "jre-x64");
        File jre = getLatestJRE(x64);
        if (jre == null) {
            installJRE();
        }

        jre = getLatestJRE(x64);
        if (jre == null) {
            log.log(Level.WARNING, "Could not locate JRE in: {0}, exists: {1}", new Object[]{x64, x64.exists()});
            return null;
        }

        boolean root = containsAll(jre, "license", "welcome");
        boolean bin = containsAll(new File(jre, "bin"), "java");
        boolean lib = containsAll(new File(jre, "lib"), "rt", "resources", "charsets");
        if (!root || !bin || !lib) {
            log.log(Level.WARNING, "Incomplete JRE installation detected in: {0}", new Object[]{jre});
            installJRE();
            jre = getLatestJRE(x64);
            if (jre == null) {
                log.log(Level.WARNING, "Could not locate JRE in: {0}, exists: {1}", new Object[]{x64, x64.exists()});
                return null;
            }
        }

        log.log(Level.INFO, "Detected JRE: {0}", jre);

        if (!resolve(jre, "bin", "java").setExecutable(true)) {
            log.log(Level.WARNING, "Unable to make 'java' executable!");
        }

        if (!resolve(jre, "bin", "javaw").setExecutable(true)) {
            log.log(Level.WARNING, "Unable to make 'javaw' executable!");
        }

        return resolve(jre, "bin");
    }

    private File getRuntimeDir() {
        return resolve(new File("").getAbsoluteFile(), "runtime");
    }

    private void installJRE() {
        try {
            File x64 = resolve(getRuntimeDir(), "jre-x64");

            JsonNode jreMeta = getJRE();
            if (jreMeta == null) {
                return;
            }

            // download the jre to the version dir
            // decompress from lzma -> zip
            // extract zip to version dir

            String jreURL = jreMeta.get("url").asText();
            String jreVersion = jreMeta.get("version").asText();

            File jreDir = resolve(x64, jreVersion);
            File zip = new File(jreDir, jreVersion + ".zip");
            File lzma = new File(jreDir, jreVersion + ".lzma");

            download(jreURL, lzma);
            decompress(lzma, zip);
            extract(zip, zip.getParentFile());

            if (lzma.delete()) {
                log.log(Level.INFO, "Removing installation file: {0}", lzma);
            }

            if (zip.delete()) {
                log.log(Level.INFO, "Removing installation file: {0}", zip);
            }
        } catch (Throwable ignore) {
            log.log(Level.WARNING, "Something went wrong whilst try to install the jre locally");
        }
    }

    private File getLatestJRE(File dir) {
        File[] jres = dir.listFiles();
        if (jres == null || jres.length == 0) {
            return null;
        }

        File latest = null;
        for (File jre : jres) {
            if (latest == null || jre.lastModified() > latest.lastModified()) {
                latest = jre;
            }
        }

        return latest;
    }

    private File resolve(File parent, String... path) {
        File file = parent;
        for (String s : path) {
            file = new File(file, s);
        }
        return file;
    }

    private JsonNode getJRE() {
        Platform system = Environment.getInstance().getPlatform();
        String os = system == Platform.WINDOWS ? "windows" : system == Platform.MAC_OS_X ? "osx" : "linux";

        if (os.equals("linux")) {
            throw new UnsupportedOperationException("Mojang do not support a linux JRE :[");
        }

        InputStream inputStream = null;
        JsonNode jre = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(LAUNCHER_DATA).openConnection();
            inputStream = connection.getInputStream();
            JsonNode root = new ObjectMapper().readTree(inputStream);
            JsonNode platform = root.get(os);
            JsonNode x64 = platform.get("64");
            jre = x64.get("jre");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(inputStream);
        }

        if (jre == null) {
            throw new UnsupportedOperationException("Cannot retrieve JRE url for system " + os + ":x64");
        }

        return jre;
    }

    private void download(String url, File destination) {
        if (destination.exists() && destination.delete()) {
            log.log(Level.INFO, "Download destination already exists, deleting: {0}", destination);
        }

        int attempts = 0;
        while (attempts++ < 5) {
            InputStream inputStream = null;

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                String length = connection.getHeaderField("Content-Length");
                if (length == null) {
                    log.log(Level.INFO, "Download attempt {0} failed", attempts);
                    Thread.sleep(500);
                    continue;
                }

                long len = Long.parseLong(length);
                if (len <= 0) {
                    log.log(Level.INFO, "Download attempt {0} failed", attempts);
                    Thread.sleep(500);
                    continue;
                }

                inputStream = connection.getInputStream();
                copyFile(inputStream, destination, SharedLocale.tr("runtimeFetcher.download"), len);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Closer.close(inputStream);
            }
        }
    }

    private void decompress(File lzma, File zip) {
        if (!lzma.exists()) {
            throw new UnsupportedOperationException("Attempted to decompress non-existent file: " + lzma);
        }

        if (zip.exists()) {
            log.log(Level.INFO, "Zip destination already exists, skipping decompression: {0}", zip);
            return;
        }

        BufferedInputStream inputStream = null;
        LZMACompressorInputStream lzmaInputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(lzma));
            lzmaInputStream = new LZMACompressorInputStream(inputStream);
            copyFile(lzmaInputStream, zip, SharedLocale.tr("runtimeFetcher.decompress"), -1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(lzmaInputStream);
            Closer.close(inputStream);
        }
    }

    private void extract(File zip, File destination) {
        if (!zip.exists()) {
            throw new UnsupportedOperationException("Attempted to extract non-existent file: " + zip);
        }

        if (destination.mkdirs()) {
            log.log(Level.INFO, "Creating dir: {0}", destination);
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    File file = new File(destination, entry.getName());
                    copyFile(inputStream, file, SharedLocale.tr("runtimeFetcher.extract", file.getName()), -1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(zipFile);
        }
    }

    private void copyFile(InputStream source, File destination, String status, long length) {
        ReadableByteChannel byteChannel = null;
        FileOutputStream outputStream = null;
        try {
            if (destination.getParentFile().mkdirs()) {
                log.log(Level.INFO, "Creating dir: {0}", destination.getParentFile());
            }

            if (destination.createNewFile()) {
                log.log(Level.INFO, "Creating file: {0}", destination);
            }

            byteChannel = Channels.newChannel(source);
            outputStream = new FileOutputStream(destination);

            if (length < 0) {
                getProgress().set(status, -1);
                outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
            } else {
                long position = 0;
                long increment = length / 100L;
                while (position < length) {
                    outputStream.getChannel().transferFrom(byteChannel, position, increment);
                    position += increment;

                    double progress = (double) position / (double) length;
                    getProgress().set(status, progress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(byteChannel);
            Closer.close(outputStream);
        }
    }

    private boolean containsAll(File dir, String... lookups) {
        String[] files = dir.list();
        if (files != null) {
            outer:
            for (String lookup : lookups) {
                for (String file : files) {
                    if (file.toLowerCase().contains(lookup.toLowerCase())) {
                        continue outer;
                    }
                }
                return false;
            }
        }
        return true;
    }
}