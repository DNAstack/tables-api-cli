package org.ga4gh.dataset.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    private static File getConfigDir() {
        return new File(System.getProperty("user.home") + "/.config/datasets");
    }

    private static File getConfigFile() {
        return new File(getConfigDir(), "config.json");
    }

    public static void save(Config config) {
        File configDir = getConfigDir();
        if (!configDir.exists()) {
            System.out.println("Creating directory " + configDir.getAbsolutePath());
            configDir.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Config loadConfig(File configFile) {
        try {
            return mapper.readerFor(Config.class).readValue(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Config getUserConfig() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return new Config();
        } else {
            return loadConfig(configFile);
        }
    }

    public static String dump(Config config) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
