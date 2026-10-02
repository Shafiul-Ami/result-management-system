package com.resultmanage.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Settings are read from:
 *   1. Environment variables  → used on the online server
 *   2. app.properties         → used on your own PC (NOT uploaded to GitHub)
 */
public final class AppConfig {

    // Must be declared BEFORE the constants below — static fields are created top to bottom
    private static final Properties FILE = loadFile();

    public static final String DB_URL = dbUrl();
    public static final String DB_USER = get("DB_USER", "postgres");
    public static final String DB_PASSWORD = get("DB_PASSWORD", "");
    public static final String TEACHER_ACCESS_CODE = get("TEACHER_ACCESS_CODE", "TEACHER2026");

    private AppConfig() {
    }

    /** Reads app.properties from WEB-INF/classes (if it exists). */
    private static Properties loadFile() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read app.properties", e);
        }
        return props;
    }

    /**
     * DB_URL if given; otherwise built from DB_HOST / DB_PORT / DB_NAME
     * (Render's render.yaml hands the database details over as separate values).
     */
    private static String dbUrl() {
        String host = get("DB_HOST", "");
        if (get("DB_URL", "").isBlank() && !host.isBlank()) {
            return "jdbc:postgresql://" + host + ":" + get("DB_PORT", "5432") + "/" + get("DB_NAME", "result_management");
        }
        return get("DB_URL", "jdbc:postgresql://localhost:5432/result_management");
    }

    /** Environment variable first, then app.properties, then the default. */
    private static String get(String key, String defaultValue) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return FILE.getProperty(key, defaultValue);
    }
}