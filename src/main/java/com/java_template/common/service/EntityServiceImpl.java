package com.java_template.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.CrudRepository;
import com.java_template.common.repository.dto.Meta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        return repository.findById(meta, technicalId).thenApply(resultNode -> {
            ObjectNode dataNode = (ObjectNode) resultNode.path("data").deepCopy();
            JsonNode idNode = resultNode.at("/meta/id");
            if (!idNode.isMissingNode()) {
                dataNode.put("technicalId", idNode.asText());
            }
            return dataNode;
        });
    }

    @Override
    public CompletableFuture<ObjectNode> getItemWithMetaFields(String entityModel, String entityVersion, UUID technicalId) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findById(meta, technicalId);
    }

    @Override
    public CompletableFuture<ArrayNode> getItems(String entityModel, String entityVersion) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.findAll(meta).thenApply(resultArray -> {
            ArrayNode simplifiedArray = JsonNodeFactory.instance.arrayNode();
            for (JsonNode item : resultArray) {
                ObjectNode data = (ObjectNode) item.path("data").deepCopy();
                JsonNode technicalId = item.at("/meta/id");
                data.set("technicalId", technicalId);
                simplifiedArray.add(data);
            }
            return simplifiedArray;
        });
    }

    @Override
    public CompletableFuture<ArrayNode> getItemsWithMetaFields(String entityModel, String entityVersion) {
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
    public CompletableFuture<UUID> addItem(String entityModel, String entityVersion, Object entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.save(meta, entity)
                .thenApply(resultArray ->{
                    JsonNode first = resultArray.get(0);
                    return UUID.fromString(first.get("entityIds").get(0).asText());
                });
    }

    @Override
    public CompletableFuture<ArrayNode> addItemAndReturnTransactionInfo(String entityModel, String entityVersion, Object entity) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.save(meta, entity);
    }

    @Override
    public CompletableFuture<List<UUID>> addItems(String entityModel, String entityVersion, Object entities) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.save(meta, entities)
                .thenApply(resultArray -> {
                    JsonNode first = resultArray.get(0);
                    ArrayNode entityIdsNode = (ArrayNode) first.get("entityIds");
                    List<UUID> ids = new ArrayList<>();
                    if (entityIdsNode != null) {
                        for (JsonNode idNode : entityIdsNode) {
                            ids.add(UUID.fromString(idNode.asText()));
                        }
                    }
                    return ids;
                });
    }

    @Override
    public CompletableFuture<ArrayNode> addItemsAndReturnTransactionInfo(String entityModel, String entityVersion, Object entities) {
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
    public CompletableFuture<ArrayNode> deleteItems(String entityModel, String entityVersion) {
        Meta meta = repository.getMeta(token, entityModel, entityVersion);
        return repository.deleteAll(meta);
    }
}

