package com.desafio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigReader instance;
    private final Properties properties;

    private ConfigReader() {
        properties = new Properties();
        loadProperties();
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
            }
            properties.load(inputStream);
            log.info("Configuration loaded from {}", CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + CONFIG_FILE, e);
        }
    }

    public String getProperty(String key) {
        String value = System.getProperty(key, properties.getProperty(key));
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue)).trim();
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key, "false"));
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
    }

    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public boolean isHeadless() {
        return getBooleanProperty("headless");
    }

    public String getBaseUrl() {
        return getProperty("base.url");
    }

    public String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public int getImplicitWait() {
        return getIntProperty("implicit.wait", 10);
    }

    public int getExplicitWait() {
        return getIntProperty("explicit.wait", 15);
    }

    public int getPageLoadTimeout() {
        return getIntProperty("page.load.timeout", 30);
    }
}
