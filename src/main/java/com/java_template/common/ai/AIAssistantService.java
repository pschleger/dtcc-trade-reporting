package com.java_template.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static com.java_template.common.config.Config.*;
import static com.java_template.common.util.HttpUtils.sendPostRequest;

@Component
public class AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);
    private static final ObjectMapper om = new ObjectMapper();

    private static final String API_V_CONNECTIONS = "api/v1/connections";
    private static final String API_V_CYODA = "api/v1/cyoda";
    private static final String API_V_WORKFLOWS = "api/v1/workflows";
    private static final String API_V_RANDOM = "api/v1/random";
    private static final String API_V_TRINO = "api/v1/trino";

    public CompletableFuture<ObjectNode> initChat(String token, String chatId) {
        ObjectNode result = om.createObjectNode();
        if (MOCK_AI) {
            return CompletableFuture.completedFuture(result.put("success", true));
        }

        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));

        CompletableFuture<Void> allRequests = CompletableFuture.allOf(
                sendPostRequest(token, CYODA_AI_URL, API_V_CYODA + "/initial", data),
                sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/initial", data),
                sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
        );

        return allRequests.thenApply(v -> result.put("success", true));
    }

    public CompletableFuture<ObjectNode> initWorkflowChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/initial", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> initConnectionsChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_CONNECTIONS + "/initial", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> initRandomChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> initCyodaChat(String token, String chatId) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/initial", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> initTrinoChat(String token, String chatId, String schemaName) {
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId, "schema_name", schemaName));
        return sendPostRequest(token, CYODA_AI_URL, API_V_TRINO + "/initial", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> aiChat(String token, String chatId, String aiEndpoint, String aiQuestion) {
        ObjectNode resultNode = om.createObjectNode();
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            resultNode.put("error", "Answer size exceeds 1MB limit");
            return CompletableFuture.completedFuture(resultNode);
        }
        if (MOCK_AI) {
            resultNode.put("entity", "some random text");
            return CompletableFuture.completedFuture(resultNode);
        }

        if (aiEndpoint.equals(CONNECTION_AI_API)) {
            return chatConnection(token, chatId, aiQuestion);
        } else if (aiEndpoint.equals(WORKFLOW_AI_API)) {
            return chatWorkflow(token, chatId, aiQuestion);
        } else if (aiEndpoint.equals(RANDOM_AI_API)) {
            return chatRandom(token, chatId, aiQuestion);
        } else if (aiEndpoint.equals(TRINO_AI_API)) {
            return chatTrino(token, chatId, aiQuestion);
        } else if (aiEndpoint.equals(CYODA_AI_API)) {
            return chatCyoda(token, chatId, aiQuestion);
        } else {
            return CompletableFuture.completedFuture(resultNode.put("error", "Unknown AI endpoint"));
        }
    }

    public CompletableFuture<ObjectNode> chatCyoda(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            ObjectNode errorNode = om.createObjectNode();
            errorNode.put("error", "Answer size exceeds 1MB limit");
            return CompletableFuture.completedFuture(errorNode);
        }
        String data = JsonUtils.mapToJson(Map.of("chat_id", chatId, "question", aiQuestion));
        return sendPostRequest(token, CYODA_AI_URL, API_V_CYODA + "/chat", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> chatWorkflow(String token, String chatId, String aiQuestion) {
        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            ObjectNode errorNode = om.createObjectNode();
            errorNode.put("error", "Answer size exceeds 1MB limit");
            return CompletableFuture.completedFuture(errorNode);
        }
        String data = JsonUtils.mapToJson(Map.of(
                "question", aiQuestion,
                "return_object", "workflow",
                "chat_id", chatId,
                "class_name", "com.cyoda.tdb.model.treenode.TreeNodeEntity"
        ));
        return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/chat", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> exportWorkflowToCyodaAi(String token, String chatId, Map<String, Object> data) {
        try {
            String requestData = JsonUtils.mapToJson(Map.of(
                    "name", data.get("name"),
                    "chat_id", chatId,
                    "class_name", data.get("class_name"),
                    "transitions", data.get("transitions")
            ));
            return sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/generate-workflow", requestData)
                    .thenApply(response -> (ObjectNode) response.get("json"));
        } catch (Exception e) {
            logger.error("Failed to export workflow", e);
            ObjectNode errorNode = om.createObjectNode();
            errorNode.put("error", "Failed to export workflow");
            errorNode.put("exception", e.getMessage());
            return CompletableFuture.completedFuture(errorNode);
        }
    }

    public CompletableFuture<ObjectNode> chatConnection(String token, String chatId, String aiQuestion) {
        ObjectNode result = om.createObjectNode();

        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(result.put("error", "Answer size exceeds 1MB limit"));
        }

        ObjectNode data = om.createObjectNode();
        data.put("question", aiQuestion);
        data.put("return_object", "import-connections");
        data.put("chat_id", chatId);

        return sendPostRequest(token, CYODA_AI_URL, API_V_CONNECTIONS + "/chat", data)
                .thenApply(response -> {
                    ObjectNode responseJson = (ObjectNode) response.get("json");
                    return result.put("message", responseJson.get("message").asText());
                });
    }

    public CompletableFuture<ObjectNode> chatRandom(String token, String chatId, String aiQuestion) {
        ObjectNode result = om.createObjectNode();

        if (aiQuestion != null && aiQuestion.length() > 1 * 1024 * 1024) {
            return CompletableFuture.completedFuture(result.put("error", "Answer size exceeds 1MB limit"));
        }
        String data = JsonUtils.mapToJson(Map.of("question", aiQuestion, "return_object", "random", "chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_RANDOM + "/chat", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }

    public CompletableFuture<ObjectNode> chatTrino(String token, String chatId, String aiQuestion) {
        String data = JsonUtils.mapToJson(Map.of("question", aiQuestion, "return_object", "random", "chat_id", chatId));
        return sendPostRequest(token, CYODA_AI_URL, API_V_TRINO + "/chat", data)
                .thenApply(response -> (ObjectNode) response.get("json"));
    }
}

