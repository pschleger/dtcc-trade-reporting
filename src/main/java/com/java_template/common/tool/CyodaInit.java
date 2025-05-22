package com.java_template.common.tool;

import com.java_template.common.auth.Authentication;
import com.java_template.common.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.java_template.common.config.Config.*;
import static com.java_template.common.tool.WorkflowConverter.parseAiWorkflowToDtoJson;
import static com.java_template.common.tool.WorkflowEnricher.enrichWorkflow;

public class CyodaInit {
    private static final Logger logger = LoggerFactory.getLogger(CyodaInit.class);
    private static final Path ENTITY_DIR = Paths.get(System.getProperty("user.dir")).resolve("src/main/java/com/java_template/entity");

    private final Authentication authentication;

    public CyodaInit(Authentication authentication) {
        this.authentication = authentication;
    }

    public CompletableFuture<Void> initCyoda() {
        logger.info("🔄 Starting workflow import into Cyoda...");
        String token = authentication.getToken();
        return initEntitiesSchema(ENTITY_DIR, token)
                .thenRun(() -> logger.info("✅ All workflows imported into Cyoda successfully."))
                .exceptionally(ex -> {
                    logger.error("❌ Cyoda workflow import failed: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    public CompletableFuture<Void> initEntitiesSchema(Path entityDir, String token) {
        try (Stream<Path> jsonFiles = Files.walk(entityDir)) {
            List<CompletableFuture<Void>> futures = jsonFiles
                    .filter(path -> path.toString().toLowerCase().endsWith("workflow.json"))
                    .filter(path -> path.getParent() != null && path.getParent().getParent() != null)
                    .filter(path -> path.getParent().getParent().getFileName().toString().equals("entity"))
                    .map(jsonFile -> {
                        String entityName = jsonFile.getParent().getFileName().toString();
                        return processWorkflowFile(jsonFile, token, entityName);
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
            String enrichedWorkflow = enrichWorkflow(Files.readString(file));

            String workflowContents = enrichedWorkflow
                    .replace("ENTITY_VERSION_VAR", ENTITY_VERSION)
                    .replace("ENTITY_MODEL_VAR", entityName)
                    .replace("CHAT_ID_VAR", CHAT_ID);

            String dto = parseAiWorkflowToDtoJson(workflowContents);
            return HttpUtils.sendPostRequest(token, CYODA_API_URL, "platform-api/statemachine/import?needRewrite=true", dto)
                    .thenApply(response -> {
                        int statusCode = response.get("status").asInt();
                        if (statusCode >= 200 && statusCode < 300) {
                            logger.info("Successfully imported workflow for entity: {}", entityName);
                            return null;
                        } else {
                            String body = response.path("json").toString();
                            throw new RuntimeException("Failed to import workflow for entity " + entityName +
                                    ". Status code: " + statusCode +
                                    ", body: " + body);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error reading file {}: {}", file, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
