package com.java_template.common.repository;

//import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.repository.dto.Meta;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CrudRepository {

    Meta getMeta(String token, String entityModel, String entityVersion);

    CompletableFuture<ObjectNode> count(Meta meta);

    CompletableFuture<Void> deleteById(Meta meta, UUID id);

    CompletableFuture<ObjectNode> delete(Meta meta, Object entity);

    CompletableFuture<ObjectNode> deleteAll(Meta meta);

    CompletableFuture<ObjectNode> deleteAllEntities(Meta meta, List<Object> entities);

    CompletableFuture<ObjectNode> deleteAllByKey(Meta meta, List<Object> keys);

    CompletableFuture<ObjectNode> deleteByKey(Meta meta, Object key);

    CompletableFuture<ObjectNode> existsByKey(Meta meta, Object key);

    CompletableFuture<ObjectNode> findAll(Meta meta);

    CompletableFuture<ObjectNode> findAllByKey(Meta meta, List<Object> keys);

    CompletableFuture<ObjectNode> findByKey(Meta meta, Object key);

    CompletableFuture<ObjectNode> findById(Meta meta, UUID id);

    CompletableFuture<ObjectNode> findAllByCriteria(Meta meta, Object criteria);

    CompletableFuture<ObjectNode> save(Meta meta, Object entity);

    CompletableFuture<ObjectNode> saveAll(Meta meta, List<Object> entities);

    CompletableFuture<ObjectNode> update(Meta meta, UUID id, Object entity);

    CompletableFuture<ObjectNode> updateAll(Meta meta, List<Object> entities);
}

