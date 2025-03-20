package com.java_template.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Base64;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String CYODA_AI_URL = dotenv.get("CYODA_AI_URL");
    public static final String CYODA_API_URL = dotenv.get("CYODA_API_URL") + "/api";

    public static final String API_KEY = new String(Base64.getDecoder().decode(dotenv.get("CYODA_API_KEY")));
    public static final String API_SECRET = new String(Base64.getDecoder().decode(dotenv.get("CYODA_API_SECRET")));

    public static final String ENTITY_VERSION = dotenv.get("ENTITY_VERSION", "1000");
    public static final String GRPC_ADDRESS = dotenv.get("GRPC_ADDRESS");
    public static final String GRPC_PROCESSOR_TAG = dotenv.get("GRPC_PROCESSOR_TAG", "elt");

    public static final String CYODA_AI_API = "cyoda";
    public static final String WORKFLOW_AI_API = "workflow";

    public static final boolean MOCK_AI = Boolean.parseBoolean(dotenv.get("MOCK_AI", "false"));
    public static final String CONNECTION_AI_API = dotenv.get("CONNECTION_AI_API");
    public static final String RANDOM_AI_API = dotenv.get("RANDOM_AI_API");
    public static final String TRINO_AI_API = dotenv.get("TRINO_AI_API");
    public static final String CHAT_ID = dotenv.get("CHAT_ID");
    public static final String CHAT_REPOSITORY = dotenv.get("CHAT_REPOSITORY", "cyoda");
    public static final String PROJECT_DIR = dotenv.get("PROJECT_DIR", "/tmp");
    public static final String REPOSITORY_URL = dotenv.get("REPOSITORY_URL", "https://github.com/Cyoda-platform/quart-client-template");

    public static final String REPOSITORY_NAME = REPOSITORY_URL.replaceAll(".*/(.*)\\.git", "$1");

    public static final String ENTITY_CLASS_NAME = "com.cyoda.tdb.model.treenode.TreeNodeEntity";
}
