package com.java_template.common.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Component
public class WorkflowProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);
    private static final Map<String, Function<ObjectNode, CompletableFuture<ObjectNode>>> processDispatch = new HashMap<>();

    private static final Path entityPath = Paths.get("src/main/java/com/java_template/entity");


    static {
        try {
            findAndImportWorkflows();
        } catch (IOException e) {
            logger.warn("Error initializing workflows: {}", e.getMessage());
        }
    }

    private static void findAndImportWorkflows() throws IOException {
        Files.walk(entityPath, 2) // Depth of 2 assumes structure entity/*/workflow.java
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith("workflow.java"))
                .forEach(WorkflowProcessor::registerWorkflow);
    }

    private static void registerWorkflow(Path workflowPath) {
        String className = getClassNameFromPath(workflowPath);
        try {
            Class<?> clazz = Class.forName(className);
            for (var method : clazz.getDeclaredMethods()) {
                if (method.getReturnType().equals(CompletableFuture.class)) {
                    processDispatch.put(method.getName(), payload -> {
                        try {
                            return (CompletableFuture<ObjectNode>) method.invoke(null, payload);
                        } catch (Exception e) {
                            throw new RuntimeException("Error invoking workflow method: " + method.getName(), e);
                        }
                    });
                }
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Error loading class: {} - {}", className, e.getMessage());
        }
    }

    private static String getClassNameFromPath(Path path) {
        Path basePath = Paths.get("src", "main", "java");
        Path relativePath = basePath.relativize(path);
        String className = relativePath.toString().replace(".java", "");

        return className.replace("/", ".");
    }


    public static CompletableFuture<ObjectNode> processEvent(String processorName, ObjectNode payload) {
        Function<ObjectNode, CompletableFuture<ObjectNode>> processor = processDispatch.get(processorName);
        if (processor == null) {
            logger.warn("No process handler found for processor: {}", processorName);
            payload.put("success", false);
            return CompletableFuture.completedFuture(payload);
        }

        return processor.apply(payload);
    }
}
