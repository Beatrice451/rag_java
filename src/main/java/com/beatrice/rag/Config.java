package com.beatrice.rag;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    public static final Set<Path> DIRS_TO_IGNORE = Stream.of(
                    ".git",
                    ".svn",
                    ".hg",
                    ".idea",
                    ".vscode",
                    ".vs",
                    "__pycache__",
                    ".pytest_cache",
                    ".mypy_cache",
                    "node_modules",
                    "venv",
                    ".venv",
                    "env",
                    "dist",
                    "build",
                    "target",
                    "out",
                    ".env",
                    "logs",
                    "tmp"
            ).map(Path::of)
            .collect(Collectors.toSet());
    public static final Set<String> FILE_EXTENSIONS_TO_IGNORE = Set.of(
            "exe",
            "dll",
            "so",
            "a",
            // Media files
            "jpg",
            "png",
            "gif",
            "mp3",
            "mp4",
            // Archives
            "zip",
            "tar",
            "gz",
            // Database files
            "db",
            "sqlite",
            "dump",
            // Temporary files
            "tmp",
            "bak",
            "swp"
    );
    private static final Logger logger = Logger.getLogger(Config.class.getName());
    private static final Properties properties = new Properties();


    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new RuntimeException("Config file not found");
            }
            properties.load(input);
            LogManager.getLogManager().readConfiguration(Config.class.getClassLoader().getResourceAsStream("logging.properties"));
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
    public static final String DB_DRIVER = get("db.driver", "postgresql");

    //    GITHUB
    public static final String GITHUB_PAT = getRequired("github.pat");
    public static final String OPENAI_API_KEY = getRequired("openai.api.key");
    public static final String OPENAI_BASE_URL = getRequired("openai.base.url");
    public static final String EMBEDDING_MODEL_NAME = getRequired("embedding.model.name");

}
