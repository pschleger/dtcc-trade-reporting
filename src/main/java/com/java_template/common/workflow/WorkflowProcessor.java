package com.java_template.common.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class WorkflowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    private final Map<String, Function<ObjectNode, CompletableFuture<ObjectNode>>> processDispatch = new ConcurrentHashMap<>();

    private final WorkflowRegistrar workflowRegistrar;
    private volatile boolean initialized = false;

    public WorkflowProcessor(@Lazy WorkflowRegistrar workflowRegistrar) {
        this.workflowRegistrar = workflowRegistrar;
    }

    public void register(String methodKey, Function<ObjectNode, CompletableFuture<ObjectNode>> fn) {
        processDispatch.put(methodKey, fn);
        logger.info("Registered workflow method: {}", methodKey);
    }

    public CompletableFuture<ObjectNode> processEvent(String eventName, ObjectNode payload) {
        ensureInitialized();

        Function<ObjectNode, CompletableFuture<ObjectNode>> workflowMethod = processDispatch.get(eventName);
        if (workflowMethod == null) {
            logger.warn("No workflow method found: {}", eventName);
            payload.put("success", false);
            return CompletableFuture.completedFuture(payload);
        }

        return workflowMethod.apply(payload);
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            workflowRegistrar.registerAll();
            initialized = true;
        }
    }
}