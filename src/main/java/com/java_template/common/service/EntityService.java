package com.java_template.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EntityService {

//      Retrieve a single item based on its ID.
    CompletableFuture<ObjectNode> getItem(String entityModel, String entityVersion, UUID id);

//     Retrieve multiple items based on the entity model and version.
    CompletableFuture<ObjectNode> getItems(String entityModel, String entityVersion);

//     Retrieve an item based on a condition.
    CompletableFuture<ObjectNode> getItemByCondition(String entityModel, String entityVersion, ObjectNode condition);

//     Retrieve items based on a condition.
    CompletableFuture<ObjectNode> getItemsByCondition(String entityModel, String entityVersion, ObjectNode condition);

//     Add a new item to the repository.
    CompletableFuture<ObjectNode> addItem(String entityModel, String entityVersion, ObjectNode entity);

//     Update an existing item in the repository.
    CompletableFuture<ObjectNode> updateItem(String entityModel, String entityVersion, UUID id, Object entity);

//     Update an existing item in the repository.
    CompletableFuture<Void> deleteItem(String entityModel, String entityVersion, UUID id);
}




