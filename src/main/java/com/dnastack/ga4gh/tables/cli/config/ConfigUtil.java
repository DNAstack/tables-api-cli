package com.dnastack.ga4gh.tables.cli.config;

import com.dnastack.ga4gh.tables.cli.util.LoggingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciitable.CWC_LongestWordMax;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.Map;

public class ConfigUtil {

    private static ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static Config config;

    private final static String CONFIG_DIR = System.getProperty("user.home") + "/.config/tables";

    private final static String CONFIG_FILE = "config.json";

    private static File getConfigDir() {
        return new File(CONFIG_DIR);
    }

    private static File getConfigFile() {
        return new File(getConfigDir(), CONFIG_FILE);
    }

    public static void setConfig(Config config) {
        ConfigUtil.config = config;
    }

    public static void save() {
        if (config != null) {
            save(config);
        }
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
        if (config != null) {
            return config;
        }
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            config = new Config();
            return config;
        } else {
            config = loadConfig(configFile);
            return config;
        }
    }

    public static void initializeConfig() {
        if (!getConfigFile().exists()) {
            save(new Config());
        } else {
            System.out.println("Config already exists");
        }
    }

    public static String dump(Config config) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getAccessTokenOrNull() {
        return getUserConfig().getTokenOrNull();
    }

    public static void setConfigValue(String key, String value) {
        try {
            Config config = getUserConfig();
            Field field = getConfig(key);
            field.set(config, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printConfigValue(String key) {
        CWC_LongestWordMax cwc = new CWC_LongestWordMax(80);
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
        String value = getConfigValue(key);

        asciiTable.addRow("Property", "Value").setPaddingLeftRight(3);
        asciiTable.addRule();
        asciiTable.addRow(key, value == null ? "" : value).setPaddingLeftRight(3);
        asciiTable.addRule();
        System.out.println(asciiTable.render());
        System.out.println();
    }

    public static String getConfigValue(String key) {
        try {
            Config config = getUserConfig();
            return (String) getConfig(key).get(config);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getConfig(String key) {
        Field[] fields = Config.class.getDeclaredFields();
        Field target = null;
        for (Field field : fields) {
            ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
            if (configProperty != null && configProperty.key().equals(key)) {
                target = field;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("Config property with name: " + key + " does not exist");
        }
        return target;
    }

    public static void printConfig() {
        try {
            Config config = getUserConfig();
            CWC_LongestWordMax cwc = new CWC_LongestWordMax(80);
            AsciiTable asciiTable = new AsciiTable();
            asciiTable.getContext().setWidth(120);
            asciiTable.getRenderer().setCWC(cwc);
            asciiTable.addRule();
            asciiTable.addRow("Property", "Description", "Value").setPaddingLeftRight(3);
            asciiTable.addRule();

            Field[] fields = Config.class.getDeclaredFields();
            for (Field field : fields) {
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
                if (configProperty != null) {
                    String value =
                        configProperty.obscure() ? obscurePassword((String) field.get(config))
                            : (String) field.get(config);
                    if (value == null) {
                        value = "";
                    }
                    asciiTable.addRow(configProperty.key(), configProperty.description(), value).setPaddingLeftRight(3);
                    asciiTable.addRule();
                }
            }

            System.out.println(asciiTable.render());
            System.out.println();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printTokenList() {
        Config config = getUserConfig();
        CWC_LongestWordMax cwc = new CWC_LongestWordMax(80);
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
        asciiTable.addRow("API", "Token").setPaddingLeftRight(3);

        Map<String, String> tokens = config.getApiTokens();
        if (tokens != null) {
            for (Map.Entry<String, String> entry : tokens.entrySet()) {
                asciiTable.addRule();
                asciiTable.addRow(entry.getKey(), entry.getValue());
            }
        }
        asciiTable.addRule();
        System.out.println(asciiTable.render());
        System.out.println();

    }

    private static String obscurePassword(String password) {
        if (password != null) {
            return "*".repeat(password.length());
        }
        return null;
    }
}
