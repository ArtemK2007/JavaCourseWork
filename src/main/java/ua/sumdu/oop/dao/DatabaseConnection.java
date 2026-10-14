package ua.sumdu.oop.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {
    private static String url;
    private static String user;
    private static String password;

    private static final Map<String, String> envVars = loadEnv();

    private static Map<String, String> loadEnv() {
        Map<String, String> map = new HashMap<>(System.getenv());
        Path envPath = Path.of(".env");
        if (Files.exists(envPath)) {
            try {
                for (String line : Files.readAllLines(envPath)) {
                    line = line.trim();
                    if (!line.startsWith("#") && line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    public static String getEnv(String key, String defaultValue) {
        return envVars.getOrDefault(key, defaultValue);
    }

    public static void configure(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || user == null || password == null) {
            throw new SQLException("Дані для підключення до БД не налаштовані.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection(String dbUrl, String dbUser, String dbPassword) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            configure(dbUrl, dbUser, dbPassword);
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}