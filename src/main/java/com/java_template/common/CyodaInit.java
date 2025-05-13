package com.java_template.common;

import com.java_template.common.ai.AIAssistantService;
import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.CyodaRepository;
import com.java_template.common.util.HttpUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.java_template.common.config.Config.*;

@Component
public class CyodaInit {
    private static final Logger logger = LoggerFactory.getLogger(CyodaInit.class);
    private static final Path ENTITY_DIR = Paths.get(System.getProperty("user.dir")).resolve("src/main/java/com/java_template/entity");
    private static final String API_V_WORKFLOWS = "/api/v1/workflows";

    private final Authentication authentication;
    private final CyodaRepository cyodaRepository;
    private final AIAssistantService aiAssistantService;

    public CyodaInit(Authentication authentication, CyodaRepository cyodaRepository, AIAssistantService aiAssistantService) {
        this.authentication = authentication;
        this.cyodaRepository = cyodaRepository;
        this.aiAssistantService = aiAssistantService;
    }

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
                        return cyodaRepository.modelExists(token, entityName, ENTITY_VERSION)
                                .thenCompose(modelExists -> modelExists ? CompletableFuture.completedFuture(null)
                                        : processWorkflowFile(jsonFile, token, entityName));
                    })
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        } catch (IOException e) {
            logger.error("Error reading files at {}: {}", entityDir, e.getMessage(), e);
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
            logger.error("Error reading file: {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }
}
