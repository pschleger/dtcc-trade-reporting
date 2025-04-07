package com.java_template.common.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.CrudRepository;
import com.java_template.common.repository.dto.Meta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EntityServiceImpl implements EntityService {

    private final CrudRepository repository;
    private final String token;

    @Autowired
    public EntityServiceImpl(CrudRepository repository, Authentication authentication) {
        this.repository = repository;
        this.token = authentication.getToken();
    }

    @Override
    public CompletableFuture<ObjectNode> getItem(String entityModel, String entityVersion, UUID technicalId) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findById(meta, technicalId);
    }

    @Override
    public CompletableFuture<ArrayNode> getItems(String entityModel, String entityVersion) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAll(meta);
    }

    @Override
    public CompletableFuture<ArrayNode> getItemByCondition(String entityModel, String entityVersion, Object condition) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAllByCriteria(meta, condition);
    }

    @Override
    public CompletableFuture<ArrayNode> getItemsByCondition(String entityModel, String entityVersion, Object condition) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAllByCriteria(meta, condition);
    }

    @Override
    public CompletableFuture<ArrayNode> addItem(String entityModel, String entityVersion, Object entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.save(meta, entity);
    }

    @Override
    public CompletableFuture<ArrayNode> addItems(String entityModel, String entityVersion, Object entities) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.saveAll(meta, entities);
    }

    @Override
    public CompletableFuture<ObjectNode> updateItem(String entityModel, String entityVersion, UUID technicalId, Object entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.update(meta, technicalId, entity);
    }

    @Override
    public CompletableFuture<ObjectNode> deleteItem(String entityModel, String entityVersion, UUID technicalId) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.deleteById(meta, technicalId);
    }

    @Override
    public CompletableFuture<ObjectNode> deleteItems(String entityModel, String entityVersion) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.deleteAll(meta)
                .thenApply(ignored -> null);
    }
}

