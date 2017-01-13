package com.skcraft.launcher.launch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.launcher.util.SharedLocale;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
public class JavaLocalRuntime {

    private static final String meta_url = "http://launchermeta.mojang.com/mc-staging/launcher.json";
    private static final Logger logger = Logger.getLogger("LocalRuntime");

    private final File localStorage;
    private final File fallbackJre;
    private final Runner runner;

    public JavaLocalRuntime(Runner runner, File localStorage, File fallbackJre) {
        this.localStorage = localStorage;
        this.fallbackJre = fallbackJre;
        this.runner = runner;
    }

    public File getRuntime() {
        try {
            String os = getOS();
            String architecture = getArchitecture();
            log("Detected system: {0} {1}", os, architecture);
            runner.setProgress(0.1, SharedLocale.tr("runner.findingLocalJRE"));

            JsonObject jre = getJRE(os, architecture);
            String url = jre.get("url").getAsString();
            String version = jre.get("version").getAsString();
            log("Latest available JRE: {0}", version);
            runner.setProgress(0.2, SharedLocale.tr("runner.findingLocalJRE"));

            File dir = new File(localStorage, version + "-x" + architecture);
            File bin = new File(dir, "bin");
            if (bin.exists()) {
                runner.setProgress(1.0, SharedLocale.tr("runner.findingLocalJRE"));
                log("Setting local JRE {0} bin: {1}", version, bin);
                return bin;
            }

            File zip = new File(localStorage, version + ".zip");
            File lzma = new File(localStorage, version + ".lzma");

            downloadURL(url, lzma);
            runner.setProgress(0.5, SharedLocale.tr("runner.findingLocalJRE"));

            decompressLZMA(lzma, zip);
            runner.setProgress(0.6, SharedLocale.tr("runner.findingLocalJRE"));

            extractZip(zip, dir);
            runner.setProgress(0.7, SharedLocale.tr("runner.findingLocalJRE"));

            deleteFile(lzma);
            deleteFile(zip);

            runner.setProgress(0.8, SharedLocale.tr("runner.findingLocalJRE"));

            log("Setting local JRE {0} bin: {1}", version, bin);

            return bin;
        } catch (Throwable t) {
            return fallbackJre;
        }
    }

    private static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "windows" : os.contains("mac") ? "osx" : "linux";
    }

    private static String getArchitecture() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";
    }

    private static JsonObject getJRE(String os, String architecture) {
        if (os.equals("linux")) {
            throw new UnsupportedOperationException("Mojang do not support linux JRE :[");
        }

        InputStream inputStream = null;
        JsonObject jre = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(meta_url).openConnection();
            inputStream = connection.getInputStream();
            JsonElement data = new JsonParser().parse(new JsonReader(new InputStreamReader(inputStream)));
            JsonObject meta = data.getAsJsonObject();
            JsonObject javas = meta.getAsJsonObject(os);
            JsonObject java = javas.getAsJsonObject(architecture);
            jre = java.getAsJsonObject("jre");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(inputStream);
        }

        if (jre == null) {
            throw new UnsupportedOperationException("Cannot retreive JRE url for system " + os + ":" + architecture);
        }

        return jre;
    }

    private static void downloadURL(String url, File destination) {
        if (destination.exists()) {
            log("Download destination already exists, skipping downloadURL: {0}", destination);
            return;
        }

        InputStream inputStream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            inputStream = connection.getInputStream();
            copyFile(inputStream, destination);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(inputStream);
        }
    }

    private static void decompressLZMA(File lzma, File zip) {
        if (!lzma.exists()) {
            throw new UnsupportedOperationException("Attempted to extractZip non-existent file: " + lzma);
        }

        if (zip.exists()) {
            log("Zip destination already exists, skipping decompression: {0}", zip);
            return;
        }

        BufferedInputStream inputStream = null;
        LZMACompressorInputStream lzmaInputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(lzma));
            lzmaInputStream = new LZMACompressorInputStream(inputStream);
            copyFile(lzmaInputStream, zip);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(lzmaInputStream);
            close(inputStream);
        }
    }

    private static void extractZip(File zip, File destination) {
        if (!zip.exists()) {
            throw new UnsupportedOperationException("Attempted to extractZip non-existent file: " + zip);
        }

        if (destination.mkdirs()) {
            log("Creating dir: {0}", destination);
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
                    copyFile(inputStream, file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(zipFile);
        }
    }

    private static void copyFile(InputStream source, File destination) {
        if (destination.exists()) {
            return;
        }

        ReadableByteChannel byteChannel = null;
        FileOutputStream outputStream = null;
        try {
            if (destination.getParentFile().mkdirs()) {
                log("Creating dir: {0}", destination.getParentFile());
            }

            if (destination.createNewFile()) {
                log("Creating file: {0}", destination);
            }

            byteChannel = Channels.newChannel(source);
            outputStream = new FileOutputStream(destination);
            outputStream.getChannel().transferFrom(byteChannel, 0L, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(byteChannel);
            close(outputStream);
        }
    }

    private static void deleteFile(File file) {
        if (!file.isDirectory()) {
            if (file.delete()) {
                log("Deleted file: {0}", file);
            }
        }
    }

    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void log(String fmt, Object... args) {
        logger.log(Level.INFO, fmt, args);
    }
}
