package com.java_template.common.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.dto.Meta;
import com.java_template.common.util.HttpUtils;
import com.java_template.common.util.SearchConditionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.java_template.common.config.Config.CYODA_API_URL;

@Component
@ConditionalOnProperty(name = "repository.type", havingValue = "http")
public class CyodaRepository implements CrudRepository {
    private final Logger logger = LoggerFactory.getLogger(CyodaRepository.class);

    private final Authentication authentication;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper;

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
    public CompletableFuture<ObjectNode> findAllByCriteria(Meta meta, Object criteria) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> save(Meta meta, Object entity) {
        return saveNewEntity(meta, entity);
    }

    @Override
    public CompletableFuture<ObjectNode> saveAll(Meta meta, List<Object> entities) {
        return null;
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
    public CompletableFuture<Void> deleteById(Meta meta, UUID id) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> delete(Meta meta, Object entity) {
        return null;
    }

    @Override
    public CompletableFuture<ObjectNode> deleteAll(Meta meta) {
        return null;
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
    public CompletableFuture<ObjectNode> findAll(Meta meta) {
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

    private CompletableFuture<ObjectNode> getAllEntities(Meta meta) {
        String path = String.format("entity/%s/%s", meta.getEntityModel(), meta.getEntityVersion());
        return HttpUtils.sendGetRequest(meta.getToken(), CYODA_API_URL, path);
    }

    private CompletableFuture<ObjectNode> getById(Meta meta, UUID id) {
        String path = String.format("entity/%s", id);
        return HttpUtils.sendGetRequest(meta.getToken(), CYODA_API_URL, path)
                .thenApply(response -> { return (ObjectNode) response.get("json").get("tree"); });
    }

    private CompletableFuture<ObjectNode> saveNewEntity(Meta meta, Object data) {
        String path = String.format("entity/JSON/%s/%s", meta.getEntityModel(), meta.getEntityVersion());
        logger.info("Saving new entity to path: {}", path);

        return HttpUtils.sendPostRequest(meta.getToken(), CYODA_API_URL, path, data)
                .thenApply(response -> {
                    if (response != null) {
                        logger.info("Successfully saved new entity. Response: {}", response);
                        return response;
                    } else {
                        logger.error("Failed to save new entity. Response is null");
                        throw new RuntimeException("Failed to save new entity.");
                    }
                })
                .exceptionally(ex -> {
                    logger.error("An error occurred while saving new entity '{}' with version '{}': {}", meta.getEntityModel(), meta.getEntityVersion(), ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    private CompletableFuture<ObjectNode> updateEntity(Meta meta, UUID id, Object entity) {
        String path = "entity/JSON";

        return HttpUtils.sendPutRequest(meta.getToken(), CYODA_API_URL, path, entity)
                .thenApply(response -> {
                    if (response != null) {
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

    public CompletableFuture<Boolean> modelExists(String token, String modelName, String entityVersion) {
    String exportModelPath = String.format("model/export/SIMPLE_VIEW/%s/%s", modelName, entityVersion);
    return HttpUtils.sendGetRequest(token, CYODA_API_URL, exportModelPath)
            .thenApply(response ->{
                int status = response.get("status").asInt();
                return status == 200;
            });
    }

    private CompletableFuture<ObjectNode> findAllByCriteria(Meta meta, SearchConditionRequest criteria) {
        return searchEntities(meta, criteria)
                .thenApply(resp -> {
                    if (resp.get("json").get("page").get("totalElements").asInt() == 0) {
                        return objectMapper.createObjectNode();
                    }
                    return (ObjectNode) resp.get("json");
                })
                .exceptionally(ex -> {
                    logger.error("Error in findAllByCriteria: {}", ex.getMessage(), ex);
                    return objectMapper.createObjectNode();
                });
    }

    private CompletableFuture<ObjectNode> searchEntities(Meta meta, SearchConditionRequest condition) {
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

    private CompletableFuture<String> createSnapshotSearch(String token, String entityModel, String entityVersion, SearchConditionRequest condition) {
        String searchPath = String.format("search/snapshot/%s/%s/", entityModel, entityVersion);
        return HttpUtils.sendPostRequest(token, CYODA_API_URL, searchPath, condition)
                .thenApply(response -> response.get("json").asText());
    }

    private CompletableFuture<ObjectNode> waitForSearchCompletion(String token, String snapshotId, long timeoutMillis, long intervalMillis) throws IOException {
        long startTime = System.currentTimeMillis();
        return pollSnapshotStatus(token, snapshotId, startTime, timeoutMillis, intervalMillis);
    }

    private CompletableFuture<ObjectNode> pollSnapshotStatus(String token, String snapshotId, long startTime, long timeoutMillis, long intervalMillis) throws IOException {
        return getSnapshotStatus(token, snapshotId).thenCompose(statusResponse -> {
            String status = statusResponse.get("snapshotStatus").asText();

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
        return HttpUtils.sendGetRequest(token, CYODA_API_URL, path);
    }

    private CompletableFuture<ObjectNode> getSearchResult(String token, String snapshotId, int pageSize, int pageNumber) {
        String path = String.format("search/snapshot/%s", snapshotId);
        Map<String, String> params = Map.of("pageSize", String.valueOf(pageSize), "pageNumber", String.valueOf(pageNumber));

        return HttpUtils.sendGetRequest(token, CYODA_API_URL, path, params)
                .thenApply(response -> {
                    if (response != null) {
                        return response;
                    } else {
                        throw new RuntimeException("Get search result failed: response is null");
                    }
                });
    }
}
