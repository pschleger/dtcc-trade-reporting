package com.java_template.common.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Component
public class WorkflowRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRegistrar.class);

    private final ApplicationContext applicationContext;
    private final WorkflowProcessor workflowProcessor;

    public WorkflowRegistrar(ApplicationContext applicationContext, WorkflowProcessor workflowProcessor) {
        this.applicationContext = applicationContext;
        this.workflowProcessor = workflowProcessor;
    }

    public void registerAll() {
        Map<String, Object> workflowBeans = applicationContext.getBeansWithAnnotation(Component.class);

        for (Object bean : workflowBeans.values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.getSimpleName().toLowerCase().endsWith("workflow")) {
                for (var method : clazz.getDeclaredMethods()) {
                    if (CompletableFuture.class.isAssignableFrom(method.getReturnType())
                            && method.getParameterCount() == 1
                            && method.getParameterTypes()[0].equals(ObjectNode.class)) {

                        method.setAccessible(true);
                        String methodKey = method.getName();

                        Function<ObjectNode, CompletableFuture<ObjectNode>> fn = payload -> {
                            try {
                                return (CompletableFuture<ObjectNode>) method.invoke(bean, payload);
                            } catch (Exception e) {
                                logger.error("Error invoking workflow method '{}': {}", methodKey, e.getMessage());
                                throw new RuntimeException(e);
                            }
                        };

                        workflowProcessor.register(methodKey, fn);
                    }
                }
            }
        }
    }
}
