package com.beatrice.rag.database;

import com.beatrice.rag.Config;

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

    /**
     * Establishes and returns a connection to the configured PostgreSQL database.
     *
     * @return a {@link Connection} object to interact with the database
     * @throws RuntimeException if a {@link SQLException} occurs while attempting to connect
     */
    public static Connection getConnection() {
        // jdbc:driver://host:port/db_name
        String conUrl = "jdbc:%s://%s:%d/%s".formatted(DB_DRIVER, DB_HOST, DB_PORT, DB_NAME);
        logger.info("Trying to connect to " + conUrl);
        try {
            Connection con = DriverManager.getConnection(conUrl, DB_USER, DB_PASSWORD);
            logger.info("Successfully connected to " + conUrl);
            return con;
        } catch (SQLException e) {
            throw new RuntimeException("Can't connect to db: " + e);
        }
    }

}

