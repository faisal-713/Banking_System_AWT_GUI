package com.banking.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add mysql-connector-j.jar to the classpath.");
        }
    }

    private DBConnection() {
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            // Fallback to default local values if db.properties is not found
            System.err.println("db.properties not found, using default values.");
        }
        URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC");
        USERNAME = props.getProperty("db.username", "root");
        PASSWORD = props.getProperty("db.password", "");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
