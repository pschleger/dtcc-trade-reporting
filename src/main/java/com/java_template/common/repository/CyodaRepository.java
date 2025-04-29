package com.java_template.common.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.dto.Meta;
import com.java_template.common.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.java_template.common.config.Config.CYODA_API_URL;

@Component
//@ConditionalOnProperty(name = "repository.type", havingValue = "http")
public class CyodaRepository implements CrudRepository {
    private final Logger logger = LoggerFactory.getLogger(CyodaRepository.class);

    private final Authentication authentication;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper;

    private final String FORMAT = "JSON"; // or "XML"

    public CyodaRepository(Authentication authentication, HttpUtils httpUtils, ObjectMapper objectMapper) {
        this.authentication = authentication;
        this.httpUtils = httpUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<ObjectNode> findById(Meta meta, UUID id) {
        return getById(meta, id);
    }

    @Override
    public CompletableFuture<ArrayNode> findAllByCriteria(Meta meta, Object criteria) {
        return findAllByCondition(meta, criteria);
    }

    @Override
    public CompletableFuture<ArrayNode> save(Meta meta, Object entity) {
        return saveNewEntities(meta, entity);
    }

    @Override
    public CompletableFuture<ArrayNode> saveAll(Meta meta, Object entities) {
        return saveNewEntities(meta, entities);
    }

    @Override
    public CompletableFuture<ObjectNode> update(Meta meta, UUID id, Object entity) {
        return updateEntity(meta, id, entity);
    }

    @Override
    public CompletableFuture<ObjectNode> updateAll(Meta meta, List<Object> entities) {
        return null;
    }

    @Override
    public Meta getMeta(String token, String entityModel, String entityVersion) {
        return new Meta(token, entityModel, entityVersion);
    }

    @Override
    public CompletableFuture<ObjectNode> count(Meta meta) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> deleteById(Meta meta, UUID id) {
        return deleteEntity(meta, id);
    }

    @Override
    public CompletableFuture<ObjectNode> delete(Meta meta, Object entity) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> deleteAll(Meta meta) {
        return deleteAllByModel(meta);
    }

    @Override
    public CompletableFuture<ObjectNode> deleteAllEntities(Meta meta, List<Object> entities) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> deleteAllByKey(Meta meta, List<Object> keys) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> deleteByKey(Meta meta, Object key) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> existsByKey(Meta meta, Object key) {
        return null;
    }

    @Override
    public CompletableFuture<ArrayNode> findAll(Meta meta) {
        return getAllEntities(meta);
    }

    @Override
    public CompletableFuture<ObjectNode> findAllByKey(Meta meta, List<Object> keys) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> findByKey(Meta meta, Object key) {
        return null;
    }

    private CompletableFuture<ArrayNode> getAllEntities(Meta meta) {
        String path = String.format("entity/%s/%s", meta.getEntityModel(), meta.getEntityVersion());
        return httpUtils.sendGetRequest(meta.getToken(), CYODA_API_URL, path)
                .thenApply(response -> {
                    int statusCode = response.get("status").asInt();
                    JsonNode jsonNode = response.get("json");
                    if (jsonNode != null && jsonNode.isArray() && statusCode >= 200 && statusCode < 300) {
                        return (ArrayNode) jsonNode;
                    } else {
                        logger.warn("Expected an ArrayNode under 'json', but got: {}", jsonNode);
                        return JsonNodeFactory.instance.arrayNode();
                    }
                });
    }

    private CompletableFuture<ObjectNode> getById(Meta meta, UUID id) {
        String path = String.format("entity/%s", id);
        return httpUtils.sendGetRequest(meta.getToken(), CYODA_API_URL, path)
                .thenApply(response -> { return (ObjectNode) response.get("json").get("data"); });
    }

    private CompletableFuture<ArrayNode> saveNewEntities(Meta meta, Object data) {
        String path = String.format("entity/%s/%s/%s", FORMAT, meta.getEntityModel(), meta.getEntityVersion());

        return httpUtils.sendPostRequest(meta.getToken(), CYODA_API_URL, path, data)
                .thenApply(response -> {
                    if (response != null) {
                        int statusCode = response.get("status").asInt();
                        JsonNode jsonNode = response.get("json");

                        if (jsonNode != null && jsonNode.isArray() && statusCode >= 200 && statusCode < 300) {
                            logger.info("Successfully saved new entities. Response: {}", response);
                            return (ArrayNode) jsonNode;
                        } else {
                            logger.error("Response does not contain a valid 'json' array. Response: {}", response);
                            throw new RuntimeException("Response does not contain a valid 'json' array");
                        }
                    } else {
                        logger.error("Failed to save new entity. Response is null");
                        throw new RuntimeException("Failed to save new entity: Response is null");
                    }
                })
                .exceptionally(ex -> {
                    logger.error("An error occurred while saving new entities '{}' with version '{}': {}",
                            meta.getEntityModel(), meta.getEntityVersion(), ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    private CompletableFuture<ObjectNode> updateEntity(Meta meta, UUID id, Object entity) {
        String path = String.format("entity/%s/%s/%s", FORMAT, id, meta.getUpdateTransition());

        return httpUtils.sendPutRequest(meta.getToken(), CYODA_API_URL, path, entity)
                .thenApply(response -> {
                    if (response != null) {
                        int statusCode = response.get("status").asInt();
                        if (statusCode >= 200 && statusCode < 300) {
                            logger.info("Successfully updated entity with id '{}'. Status: {}, Response: {}", id, statusCode, response.get("json"));
                        } else {
                            logger.warn("Update request for entity with id '{}' returned non-success status: {}. Response: {}", id, statusCode, response.get("json"));
                        }

                        return (ObjectNode) response.get("json");
                    } else {
                        logger.error("Failed to update the entity with id '{}'. Response is null", id);
                        throw new RuntimeException("Failed to save new entity.");
                    }
                })
                .exceptionally(ex -> {
                    logger.error("An error occurred while updating entity with id '{}': {}", id, ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    private CompletableFuture<ObjectNode> deleteEntity(Meta meta, UUID id) {
        String path = String.format("entity/%s", id);

        return httpUtils.sendDeleteRequest(meta.getToken(), CYODA_API_URL, path);
    }

    private CompletableFuture<ObjectNode> deleteAllByModel(Meta meta) {
        String path = String.format("entity/%s/%s", meta.getEntityModel(), meta.getEntityVersion());

        return httpUtils.sendDeleteRequest(meta.getToken(), CYODA_API_URL, path);
    }

    public CompletableFuture<Boolean> modelExists(String token, String modelName, String entityVersion) {
    String exportModelPath = String.format("model/export/SIMPLE_VIEW/%s/%s", modelName, entityVersion);
    return httpUtils.sendGetRequest(token, CYODA_API_URL, exportModelPath)
            .thenApply(response ->{
                int status = response.get("status").asInt();
                return status == 200;
            });
    }

    private CompletableFuture<ArrayNode> findAllByCondition(Meta meta, Object condition) {
        return searchEntities(meta, condition)
                .thenApply(resp -> {
                    if (resp.get("json").get("page").get("totalElements").asInt() == 0) {
                        return objectMapper.createArrayNode();
                    }
                    return (ArrayNode) resp.get("json").get("_embedded").get("objectNodes");
                })
                .exceptionally(ex -> {
                    logger.error("Error in findAllByCriteria: {}", ex.getMessage(), ex);
                    return objectMapper.createArrayNode();
                });
    }

    private CompletableFuture<ObjectNode> searchEntities(Meta meta, Object condition) {
        return createSnapshotSearch(meta.getToken(), meta.getEntityModel(), meta.getEntityVersion(), condition)
                .thenCompose(snapshotId -> {
                    if (snapshotId == null) {
                        logger.error("Snapshot ID not found in response");
                        return CompletableFuture.completedFuture(objectMapper.createObjectNode());
                    }
                    try {
                        return waitForSearchCompletion(meta.getToken(), snapshotId, 60, 300)
                                .thenCompose(statusResponse -> getSearchResult(meta.getToken(), snapshotId, 100, 1));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private CompletableFuture<String> createSnapshotSearch(String token, String entityModel, String entityVersion, Object condition) {
        String searchPath = String.format("search/snapshot/%s/%s", entityModel, entityVersion);
        return httpUtils.sendPostRequest(token, CYODA_API_URL, searchPath, condition)
                .thenApply(response -> response.get("json").asText());
    }

    private CompletableFuture<ObjectNode> waitForSearchCompletion(String token, String snapshotId, long timeoutMillis, long intervalMillis) throws IOException {
        long startTime = System.currentTimeMillis();
        return pollSnapshotStatus(token, snapshotId, startTime, timeoutMillis, intervalMillis);
    }

    private CompletableFuture<ObjectNode> pollSnapshotStatus(String token, String snapshotId, long startTime, long timeoutMillis, long intervalMillis) throws IOException {
        return getSnapshotStatus(token, snapshotId).thenCompose(statusResponse -> {
            String status = statusResponse.get("json").get("snapshotStatus").asText();

            if ("SUCCESSFUL".equals(status)) {
                return CompletableFuture.completedFuture(statusResponse);
            } else if (!"RUNNING".equals(status)) {
                return CompletableFuture.failedFuture(new RuntimeException("Snapshot search failed: " + statusResponse));
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > timeoutMillis) {
                return CompletableFuture.failedFuture(new TimeoutException("Timeout exceeded after " + timeoutMillis + " ms"));
            }

            return CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(intervalMillis, TimeUnit.MILLISECONDS))
                    .thenCompose(ignored -> {
                        try {
                            return pollSnapshotStatus(token, snapshotId, startTime, timeoutMillis, intervalMillis);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        });
    }

    private CompletableFuture<ObjectNode> getSnapshotStatus(String token, String snapshotId) throws IOException {
        String path = String.format("search/snapshot/%s/status", snapshotId);
        return httpUtils.sendGetRequest(token, CYODA_API_URL, path);
    }

    private CompletableFuture<ObjectNode> getSearchResult(String token, String snapshotId, int pageSize, int pageNumber) {
        String path = String.format("search/snapshot/%s", snapshotId);
//        Map<String, String> params = Map.of("pageSize", String.valueOf(pageSize), "pageNumber", String.valueOf(pageNumber));
        Map<String, String> params = new HashMap<>();

        return httpUtils.sendGetRequest(token, CYODA_API_URL, path, params)
                .thenApply(response -> {
                    if (response != null) {
                        return response;
                    } else {
                        throw new RuntimeException("Get search result failed: response is null");
                    }
                });
    }
}
