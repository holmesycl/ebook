package com.holmesycl.ebook.util;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(Config.get("baseDir"));
    }

}
