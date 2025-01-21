package com.optum.itl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static Properties properties = new Properties();

    static {
        try {
            String configFilePath = System.getProperty("itlconfig.properties");
            properties.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
