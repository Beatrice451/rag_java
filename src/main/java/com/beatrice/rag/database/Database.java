package com.beatrice.rag.database;

import com.beatrice.rag.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Utility class for establishing connections to a PostgreSQL database.
 * <p>
 * This class uses configuration values from {@link Config} to build a JDBC connection URL
 * and obtain a connection via {@link DriverManager}.
 * </p>
 *
 * <p><b>Note:</b> This implementation assumes that the appropriate JDBC driver is available at runtime.</p>
 */
public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static final String DB_HOST = Config.DB_HOST;
    private static final String DB_NAME = Config.DB_NAME;
    private static final String DB_USER = Config.DB_USER;
    private static final String DB_PASSWORD = Config.DB_PASSWORD;
    private static final String DB_DRIVER = Config.DB_DRIVER;
    private static final int DB_PORT = Config.DB_PORT;
    private static final HikariDataSource dataSource = initDataSource();


    private static HikariDataSource initDataSource() {
        logger.info("Initializing data source");
        HikariConfig config = new HikariConfig();
        String conUrl = "jdbc:%s://%s:%d/%s".formatted(DB_DRIVER, DB_HOST, DB_PORT, DB_NAME);
        config.setJdbcUrl(conUrl);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);

        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setLeakDetectionThreshold(60000);

        logger.info("Data source initialized successfully: " + conUrl);
        return new HikariDataSource(config);

    }

    public static Connection getConnection() throws SQLException {
        logger.info("Connection acquired for HikariCP pool");
        return dataSource.getConnection();
    }

}

