package com.java_template.common.ai;

import com.java_template.common.auth.Authentication;
import com.java_template.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static com.java_template.common.config.Config.*;
import static com.java_template.common.util.HttpUtils.sendPostRequest;

public class AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

    private static final String API_V_CONNECTIONS = "api/v1/connections";
    private static final String API_V_CYODA = "api/v1/cyoda";
    private static final String API_V_WORKFLOWS = "api/v1/workflows";
    private static final String API_V_RANDOM = "api/v1/random";
    private static final String API_V_TRINO = "api/v1/trino";

    public CompletableFuture<Map<String, Object>> initChat(String token, String chatId) {
        if ("true".equals(MOCK_AI)) {
            return CompletableFuture.completedFuture(Map.of("success", true));
        }

        // Используем метод mapToJson для преобразования Map в строку JSON
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        String[] endpoints = {API_V_CYODA, API_V_WORKFLOWS, API_V_RANDOM};

        CompletableFuture<Void> allRequests = CompletableFuture.allOf(
                sendPostRequest(token, CYODA_AI_URL, API_V_CYODA + "/initial", data),
                sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/initial", data),
                sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
        );

        return allRequests.thenApply(v -> Map.of("success", true));
    }

    public CompletableFuture<String> initWorkflowChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/initial", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<String> initConnectionsChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_CONNECTIONS + "/initial", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<String> initRandomChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<String> initCyodaChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<Map<String, Object>> initTrinoChat(String token, String chatId, String schemaName) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId, "schema_name", schemaName));
        return sendPostRequest(token, CYODA_AI_URL, API_V_TRINO + "/initial", data)
                .thenApply(response -> response.get("json"));
    }

    public CompletableFuture<Map<String, Object>> aiChat(String token, String chatId, String aiEndpoint, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(Map.of("error", "Answer size exceeds 1MB limit"));
        }
        if ("true".equals(MOCK_AI)) {
            return CompletableFuture.completedFuture(Map.of("entity", "some random text"));
        }

        switch (aiEndpoint) {
            case CYODA_AI_API:
                return chatCyoda(token, chatId, aiQuestion);
            case WORKFLOW_AI_API:
                return chatWorkflow(token, chatId, aiQuestion);
            case CONNECTION_AI_API:
                return chatConnection(token, chatId, aiQuestion);
            case RANDOM_AI_API:
                return chatRandom(token, chatId, aiQuestion);
            case TRINO_AI_API:
                return chatTrino(token, chatId, aiQuestion);
            default:
                return CompletableFuture.completedFuture(Map.of("error", "Unknown AI endpoint"));
        }
    }

    public CompletableFuture<Map<String, Object>> chatCyoda(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(Map.of("error", "Answer size exceeds 1MB limit"));
        }
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId, "question", aiQuestion));
        return sendPostRequest(token, CYODA_AI_URL, API_V_CYODA + "/chat", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<Map<String, Object>> chatWorkflow(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(Map.of("error", "Answer size exceeds 1MB limit"));
        }
        String data = JsonUtils.mapToJson(Map.of(
                "question", aiQuestion,
                "return_object", "workflow",
                "chat_id", chatId,
                "class_name", "com.cyoda.tdb.model.treenode.TreeNodeEntity"
        ));
        return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/chat", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<Map<String, Object>> exportWorkflowToCyodaAi(String token, String chatId, Map<String, Object> data) {
        try {
            String requestData = JsonUtils.mapToJson(Map.of(
                    "name", data.get("name"),
                    "chat_id", chatId,
                    "class_name", data.get("class_name"),
                    "transitions", data.get("transitions")
            ));
            return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/generate-workflow", requestData)
                    .thenApply(response -> response.get("json").get("message").asText());
        } catch (Exception e) {
            logger.error("Failed to export workflow", e);
            return CompletableFuture.completedFuture(Map.of("error", "Failed to export workflow"));
        }
    }

    public CompletableFuture<Map<String, Object>> chatConnection(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(Map.of("error", "Answer size exceeds 1MB limit"));
        }
        String data = JsonUtils.mapToJson(Map.of("question", aiQuestion, "return_object", "import-connections", "chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_CONNECTIONS + "/chat", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<Map<String, Object>> chatRandom(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(Map.of("error", "Answer size exceeds 1MB limit"));
        }
        String data = JsonUtils.mapToJson(Map.of("question", aiQuestion, "return_object", "random", "chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/chat", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }

    public CompletableFuture<Map<String, Object>> chatTrino(String token, String chatId, String aiQuestion) {
        String data = JsonUtils.mapToJson(Map.of("question", aiQuestion, "return_object", "random", "chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_TRINO + "/chat", data)
                .thenApply(response -> response.get("json").get("message").asText());
    }
}

