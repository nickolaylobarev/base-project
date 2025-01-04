package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for loading and retrieving application properties.
 * Loads properties from environment-specific files (e.g., "dev.properties", "prod.properties"),
 * and provides methods to access both public and private properties, with fallback options for missing values
 */

@Slf4j
public class PropertiesManagerUtil {
    private static final String ENV_SYSTEM_PROPERTY = "env";
    private static final String ENV_ENV_VARIABLE = "TEST_ENV";
    private static final String DEFAULT_ENV = "dev";

    private static final Properties PROPERTIES = loadProperties();
    private static final Properties PRIVATE_PROPERTIES = loadPrivateProperties();

    public static String getProperty(String propertyName) {
        String value = PROPERTIES.getProperty(propertyName);
        if (value == null) {
            throw new NullPointerException("Property " + propertyName + " is not found");
        }
        return value;
    }

    public static String getProperty(String propertyName, String defaultValue) {
        return PROPERTIES.getProperty(propertyName, defaultValue);
    }

    public static String getPrivatePropertyOrNull(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = PRIVATE_PROPERTIES.getProperty(propertyName);
        }
        return value;
    }

    public static String getPrivateProperty(String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = PRIVATE_PROPERTIES.getProperty(propertyName, defaultValue);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        String environment = getEnvironment();

        log.info("TEST ENVIRONMENT: {}", environment.toUpperCase(Locale.getDefault()));

        String configurationFilePath = "/properties/" + environment + ".properties";
        try (InputStream inputStream = PropertiesManagerUtil.class.getResourceAsStream(configurationFilePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + configurationFilePath, e);
        }
        return properties;
    }

    private static Properties loadPrivateProperties() {
        Properties properties = new Properties();
        String environment = getEnvironment();
        String configurationFilePath = "/properties/" + environment + "-private.properties";

        try (InputStream inputStream = PropertiesManagerUtil.class.getResourceAsStream(configurationFilePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load private properties from " + configurationFilePath, e);
        }
        return properties;
    }

    private static String getEnvironment() {
        String envFromOsVar = System.getenv(ENV_ENV_VARIABLE);
        if (envFromOsVar == null || envFromOsVar.isBlank()) {
            envFromOsVar = DEFAULT_ENV;
        }

        String envFromSysProp = System.getProperty(ENV_SYSTEM_PROPERTY);
        if (envFromSysProp != null && !envFromSysProp.isBlank()) {
            return envFromSysProp.toLowerCase(Locale.getDefault());
        }

        return envFromOsVar.toLowerCase(Locale.getDefault());
    }
}
