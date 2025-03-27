package com.java_template.common.service;

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
    private final Authentication authentication;
    private final String token;

    @Autowired
    public EntityServiceImpl(CrudRepository repository, Authentication authentication) {
        this.repository = repository;
        this.authentication = authentication;
        this.token = authentication.getToken();
    }

    @Override
    public CompletableFuture<ObjectNode> getItem(String entityModel, String entityVersion, UUID technicalId) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findById(meta, technicalId);
    }

    @Override
    public CompletableFuture<ObjectNode> getItems(String entityModel, String entityVersion) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAll(meta);
    }

    @Override
    public CompletableFuture<ObjectNode> getItemByCondition(String entityModel, String entityVersion, ObjectNode condition) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAllByCriteria(meta, condition);
    }

    @Override
    public CompletableFuture<ObjectNode> getItemsByCondition(String entityModel, String entityVersion, ObjectNode condition) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAllByCriteria(meta, condition);
    }

    @Override
    public CompletableFuture<ObjectNode> addItem(String entityModel, String entityVersion, ObjectNode entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.save(meta, entity);
    }

    @Override
    public CompletableFuture<ObjectNode> updateItem(String entityModel, String entityVersion, UUID technicalId, Object entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.update(meta, technicalId, entity);
    }

    @Override
    public CompletableFuture<Void> deleteItem(String entityModel, String entityVersion, UUID technicalId) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.deleteById(meta, technicalId)
                .thenApply(ignored -> null);
    }
}

