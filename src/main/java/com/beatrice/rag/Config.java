package com.beatrice.rag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new RuntimeException("Config file not found");
            }
            properties.load(input);
            logger.info("Config loaded");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.toUpperCase().replace('.', '_')); // some.parameter -> SOME_PARAMETER
        if (envValue != null) {
            return envValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    private static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }



    // No default value, the parameter is required
    private static String getRequired(String key) {
        String envValue = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String propValue = properties.getProperty(key);
        if (propValue == null || propValue.isBlank()) {
            throw new RuntimeException("Required configuration property '" + key + "' is missing!");
        }


        return propValue;
    }

    //    DATABASE
    public static final String DB_HOST = get("db.host", "localhost");
    public static final String DB_NAME = get("db.name", "postgres");
    public static final int DB_PORT = getInt("db.port", 5432);
    public static final String DB_USER = get("db.user", "postgres");
    public static final String DB_PASSWORD = get("db.password", "root");
    //    GITHUB
    public static final String GITHUB_PAT = getRequired("github.pat");

}
