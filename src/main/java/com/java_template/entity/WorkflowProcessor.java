package com.java_template.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.util.JsonUtils;
import org.cyoda.cloud.api.event.BaseEvent;
import org.cyoda.cloud.api.event.DataPayload;
import org.cyoda.cloud.api.event.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.EntityProcessorCalculationResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Component
public class WorkflowProcessor {
    private static final Map<String, Function<ObjectNode, CompletableFuture<ObjectNode>>> processDispatch = new HashMap<>();

    private static final Path entityPath = Paths.get("src/main/java/com/java_template/entity");


    static {
        try {
            findAndImportWorkflows();
        } catch (IOException e) {
            System.err.println("Error initializing workflows: " + e.getMessage());
        }
    }

    private static void findAndImportWorkflows() throws IOException {
        Files.walk(entityPath, 2) // Depth of 2 assumes structure entity/*/workflow.java
                .filter(path -> path.getFileName().toString().equals("workflow.java"))
                .forEach(WorkflowProcessor::registerWorkflow);
    }

    private static void registerWorkflow(Path workflowPath) {
        String className = getClassNameFromPath(workflowPath);
        try {
            Class<?> clazz = Class.forName(className);
            for (var method : clazz.getDeclaredMethods()) {
                if (!method.getName().startsWith("_") && method.getReturnType().equals(CompletableFuture.class)) {
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
            System.err.println("Error loading class: " + className + " - " + e.getMessage());
        }
    }

    private static String getClassNameFromPath(Path path) {
        String relativePath = "entity." + entityPath.relativize(path).toString().replace("/", ".");
        return relativePath.replace(".java", "");
    }

    public static CompletableFuture<ObjectNode> processEvent(String processorName, ObjectNode payload) {
        Function<ObjectNode, CompletableFuture<ObjectNode>> processor = processDispatch.get(processorName);
        if (processor == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown processor: " + processorName));
        }

        return processor.apply(payload);
    }
}
