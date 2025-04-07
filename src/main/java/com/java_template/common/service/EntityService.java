package com.java_template.common.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EntityService {

    //      Retrieve a single item based on its ID.
    CompletableFuture<ObjectNode> getItem(String entityModel, String entityVersion, UUID id);

    //     Retrieve multiple items based on the entity model and version.
    CompletableFuture<ArrayNode> getItems(String entityModel, String entityVersion);

    //     Retrieve an item based on a condition.
    CompletableFuture<ArrayNode> getItemByCondition(String entityModel, String entityVersion, Object condition);

    //     Retrieve items based on a condition.
    CompletableFuture<ArrayNode> getItemsByCondition(String entityModel, String entityVersion, Object condition);

    //     Add a new item to the repository.
    CompletableFuture<ArrayNode> addItem(String entityModel, String entityVersion, Object entity);

    //     Add a list of items to the repository.
    CompletableFuture<ArrayNode> addItems(String entityModel, String entityVersion, Object entities);

    //     Update an existing item in the repository.
    CompletableFuture<ObjectNode> updateItem(String entityModel, String entityVersion, UUID id, Object entity);

    //     Delete an item by ID.
    CompletableFuture<ObjectNode> deleteItem(String entityModel, String entityVersion, UUID id);

    //     Delete all items by entityModel and entityVersion.
    CompletableFuture<ObjectNode> deleteItems(String entityModel, String entityVersion);
}




