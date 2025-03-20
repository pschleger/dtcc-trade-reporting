package com.java_template.common.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.common.ai.AIAssistantService;
import com.java_template.common.auth.Authentication;
import com.java_template.common.util.HttpUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.java_template.common.config.Config.*;

@Component
public class CyodaInit {
    private static final Logger logger = Logger.getLogger(CyodaInit.class.getName());
    private static final Path ENTITY_DIR = Paths.get(System.getProperty("user.dir")).resolve("entity");
    private static final String API_V_WORKFLOWS = "/api/v1/workflows";

    private final Authentication authentication;
    private final CyodaHttpRepository cyodaHttpRepository;
    private final AIAssistantService aiAssistantService;

    public CyodaInit(Authentication authentication, CyodaHttpRepository cyodaHttpRepository, AIAssistantService aiAssistantService) {
        this.authentication = authentication;
        this.cyodaHttpRepository = cyodaHttpRepository;
        this.aiAssistantService = aiAssistantService;
    }

    //todo token
    @PostConstruct
    public void initCyoda() {
        logger.info("🚀 CyodaInit: initializing Cyoda...");
        String token = authentication.getToken();
        initEntitiesSchema(ENTITY_DIR, token)
                .thenRun(() -> logger.info("✅ CyodaInit: Entities initialized successfully!"));
    }

    public CompletableFuture<Void> initEntitiesSchema(Path entityDir, String token) {
        try (Stream<Path> jsonFiles = Files.walk(entityDir)) {
            List<CompletableFuture<Void>> futures = jsonFiles
                    .filter(path -> path.toString().endsWith("workflow.json"))
                    .filter(path -> path.getParent() != null && path.getParent().getParent() != null)
                    .filter(path -> path.getParent().getParent().getFileName().toString().equals("entity"))
                    .map(jsonFile -> {
                        String entityName = jsonFile.getParent().getFileName().toString();
                        return cyodaHttpRepository.modelExists(token, entityName, ENTITY_VERSION)
                                .thenCompose(modelExists -> modelExists ? CompletableFuture.completedFuture(null)
                                        : initWorkflow(jsonFile.getParent(), token, entityName));
                    })
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        } catch (IOException e) {
            logger.severe("Error reading files: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> initWorkflow(Path entityDir, String token, String entityName) {
        try {
            List<CompletableFuture<Void>> futures = Files.walk(entityDir)
                    .filter(file -> file.getFileName().toString().equals("workflow.json"))
                    .map(file -> processWorkflowFile(file, token, entityName))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        } catch (IOException e) {
            logger.severe("Error processing workflow.json: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> processWorkflowFile(Path file, String token, String entityName) {
        try {
            String workflowContents = Files.readString(file)
                    .replace("ENTITY_VERSION_VAR", ENTITY_VERSION)
                    .replace("ENTITY_MODEL_VAR", entityName)
                    .replace("CHAT_ID_VAR", CHAT_ID);

            Map<String, String> data = Map.of(
                    "workflow_json", workflowContents,
                    "class_name", ENTITY_CLASS_NAME
            );

            return HttpUtils.sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/initial", data)
                    .thenAccept(response -> logger.info("AI workflow init status: " + response.get("status")))
                    .thenCompose(ignored -> HttpUtils.sendPostRequest(token, CYODA_AI_URL, API_V_WORKFLOWS + "/return-dto", data))
                    .thenCompose(response -> HttpUtils.sendPostRequest(token, CYODA_API_URL, "platform-api/statemachine/import?needRewrite=true", response.get("json")))
                    .thenApply(response -> null);
        } catch (IOException e) {
            logger.severe("Ошибка чтения файла: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }
}

//    private static void initTrino(String entityName, String token) {
//        Map<String, String> trinoModelsConfig = new HashMap<>();
//
//        sendGetRequest(token, CYODA_API_URL, "treeNode/model/")
//                .thenApply(resp -> {
//                    List<Map<String, Object>> trinoModels = (List<Map<String, Object>>) resp.get("json");
//                    return trinoModels.stream()
//                            .filter(model -> entityName.equals(model.get("modelName")) && ENTITY_VERSION.equals(model.get("modelVersion").toString()))
//                            .findFirst();
//                })
//                .thenAccept(modelOpt -> {
//                    modelOpt.ifPresent(model -> {
//                        String modelId = model.get("id").toString();
//                        sendGetRequest(token, CYODA_API_URL, "sql/schema/genTables/" + modelId)
//                                .thenApply(modelDetails -> {
//                                    Map<String, Object> data = Map.of(
//                                            "id", null,
//                                            "schemaName", entityName,
//                                            "tables", modelDetails.get("json")
//                                    );
//                                    return sendPostRequest(token, CYODA_API_URL, "sql/schema/", data);
//                                })
//                                .thenAccept(resp -> {
//                                    String chatId = resp.get("json").toString();
//                                    aiService.initTrinoChat(token, chatId, entityName);
//                                    trinoModelsConfig.put(entityName, chatId);
//                                    saveConfig(trinoModelsConfig);
//                                });
//                    });
//                });
//    }

//    private static void saveConfig(Map<String, String> config) {
//        Path configFilePath = ENTITY_DIR.resolve("config.json");
//        try {
//            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFilePath.toFile(), config);
//        } catch (IOException e) {
//            logger.severe("Error saving config: " + e.getMessage());
//        }
//    }
//}


//class AIService {
//    public void initTrinoChat(String token, String chatId, String schemaName) {
//        logger.info("Initializing Trino chat for " + schemaName);
//    }
//}
