package com.java_template.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Base64;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String CYODA_HOST = getEnv("CYODA_HOST");
    public static final String CYODA_API_URL = getEnv("CYODA_API_URL", "https://" + CYODA_HOST + "/api");
    public static final String GRPC_ADDRESS = getEnv("GRPC_ADDRESS", "grpc-" + CYODA_HOST);
    public static final String GRPC_PROCESSOR_TAG = getEnv("GRPC_PROCESSOR_TAG", "cloud_manager_app");

    public static final String API_KEY = new String(Base64.getDecoder().decode(getEnv("CYODA_API_KEY")));
    public static final String API_SECRET = new String(Base64.getDecoder().decode(getEnv("CYODA_API_SECRET")));

    public static final String ENTITY_VERSION = getEnv("ENTITY_VERSION", "1000");
    public static final String CHAT_ID = dotenv.get("CHAT_ID");

    private static String getEnv(String key, String defaultValue) {
        String value = dotenv.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        return value != null ? value : defaultValue;
    }

    private static String getEnv(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            throw new RuntimeException("Missing required environment variable: " + key);
        }
        return value;
    }

}
