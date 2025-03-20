package com.java_template.common.repository;

import com.java_template.common.auth.Authentication;
import com.java_template.common.repository.dto.SaveEntityRespDTO;
import com.java_template.common.util.JsonToEntityListParser;
import com.java_template.common.util.JsonToEntityParser;
import com.java_template.common.util.SearchConditionRequest;
import com.java_template.common.entity.BaseEntity;
import com.java_template.common.entity.EntityRegistry;
import com.java_template.common.util.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static com.java_template.common.config.Config.*;

@Component
public class CyodaHttpRepository {
    private static final Logger logger = LoggerFactory.getLogger(CyodaHttpRepository.class);

    private String token;

    private final String FORMAT = "JSON"; // or "XML"
    private final String CONVERTER = "SAMPLE_DATA"; // or "JSON_SCHEMA", "SIMPLE_VIEW"

    private final ObjectMapper mapper;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final JsonToEntityListParser jsonToEntityListParser;
    private final JsonToEntityParser jsonToEntityParser;

    private final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build();

    public CyodaHttpRepository(ObjectMapper mapper, JsonToEntityListParser jsonToEntityListParser, JsonToEntityParser jsonToEntityParser, Authentication authentication) {
        this.mapper = mapper;
        this.jsonToEntityListParser = jsonToEntityListParser;
        this.jsonToEntityParser = jsonToEntityParser;
        this.token = authentication.getToken();

        if (this.token == null) {
            throw new IllegalStateException("Token is not initialized");
        }
    }

    public <T extends BaseEntity> HttpResponse saveEntityModel(List<T> entities) throws IOException {
        String model = EntityRegistry.getModelByClass(entities.get(0).getClass());

        String url = CYODA_API_URL + String.format("/api/treeNode/model/import/%s/%s/%s/%s", FORMAT, CONVERTER, model, ENTITY_VERSION);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        return executeHttpRequest(httpPost);
    }

    public <T extends BaseEntity> HttpResponse getEntityModel(List<T> entities) throws IOException {
        String model = EntityRegistry.getModelByClass(entities.get(0).getClass());

        String url = CYODA_API_URL + String.format("/api/treeNode/model/export/%s/%s/%s", model, CONVERTER, ENTITY_VERSION);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpGet);
    }

    public HttpResponse getAllEntityModels() throws IOException {
        String url = CYODA_API_URL + "/api/treeNode/model";
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpGet);
    }

    public <T extends BaseEntity> HttpResponse lockEntityModel(List<T> entities) throws IOException {
        String model = EntityRegistry.getModelByClass(entities.get(0).getClass());

        String url = CYODA_API_URL + String.format("/api/treeNode/model/%s/%s/lock", model, ENTITY_VERSION);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpPut);
    }

    public <T extends BaseEntity> HttpResponse unlockEntityModel(List<T> entities) throws IOException {
        String model = EntityRegistry.getModelByClass(entities.get(0).getClass());

        String url = CYODA_API_URL + String.format("/api/treeNode/model/%s/%s/unlock", model, ENTITY_VERSION);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpPut);
    }

    public HttpResponse deleteEntityModel(String modelName, String modelVersion) throws IOException {
        String url = String.format(CYODA_API_URL + "/api/treeNode/model/%s/%s", modelName, modelVersion);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpDelete);
    }

    public HttpResponse deleteEntityModel(String modelName) throws IOException {
        return deleteEntityModel(modelName, ENTITY_VERSION);
    }


    public <T extends BaseEntity> HttpResponse saveEntities(List<T> entities) throws IOException {
        String model = EntityRegistry.getModelByClass(entities.get(0).getClass());

        String url = CYODA_API_URL + String.format("/api/entity/%s/%s/%s", FORMAT, model, ENTITY_VERSION);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        var json = convertListToJson(entities);
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);

        return executeHttpRequest(httpPost);
    }

    public <T extends BaseEntity> SaveEntityRespDTO saveSingleEntity(T entity) {
        String model = EntityRegistry.getModelByClass(entity.getClass());

        String url = CYODA_API_URL + String.format("/api/entity/%s/%s/%s", FORMAT, model, ENTITY_VERSION);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        String json = null;
        try {
            json = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String respJson = EntityUtils.toString(response.getEntity());
                List<SaveEntityRespDTO> entities = mapper.readValue(respJson, new TypeReference<>() {});
                return entities.get(0);
            } else {
                logger.error("Request failed! Status code: " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage(), e);
        }
        return null;
    }

    public HttpResponse executeHttpRequest(HttpRequestBase request) throws IOException {
        logger.info(mapper.writeValueAsString(request.toString()));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("OK");
            } else {
                logger.error("Error: Status = {}, Response = {}",
                        response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity()));
            }
            return response;
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage(), e);
        }
        return null;
    }

    public HttpResponse deleteEntityById(String id) throws IOException {
        String url = CYODA_API_URL + String.format("/api/entity/TREE/%s", id);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestConfig);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpDelete);
    }

    public HttpResponse deleteAllEntitiesByModel(String modelName, String modelVersion) throws IOException {
        String url = CYODA_API_URL + String.format("/api/entity/%s/%s", modelName, modelVersion);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpDelete);
    }

    public <T extends BaseEntity> HttpResponse deleteAllEntitiesByModel(List<T> entities) throws IOException {
        String modelName = EntityRegistry.getModelByClass(entities.get(0).getClass());
        return deleteAllEntitiesByModel(modelName);
    }

    public HttpResponse deleteAllEntitiesByModel(String modelName) throws IOException {
        return deleteAllEntitiesByModel(modelName, ENTITY_VERSION);
    }

    public <T> String convertListToJson(List<T> entities) throws JsonProcessingException {
        return mapper.writeValueAsString(entities);
    }

    public String runSearchAndGetSnapshotId(String model, String version, SearchConditionRequest searchConditionRequest) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s/%s", CYODA_API_URL, model, version);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        String json = mapper.writeValueAsString(searchConditionRequest);
        httpPost.setEntity(new StringEntity(json));

        logger.info(mapper.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity).trim();
            JsonNode jsonNode = mapper.readTree(responseBody);

            return jsonNode.asText();
        }
    }

    public Map<String, Object> getSnapshotStatus(String snapshotId) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s/status", CYODA_API_URL, snapshotId);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(mapper.writeValueAsString(httpGet.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            Map<String, Object> responseMap = mapper.readValue(responseString, new TypeReference<>() {
            });
            return responseMap;
        }
    }

//    public <T extends BaseEntity> List<T> getSearchResultAsEntities(String model, String version, SearchConditionRequest conditionRequest) throws IOException, InterruptedException {
//        var clazz = EntityRegistry.getClassByModel(model);
//        var snapshotId = runSearchAndGetSnapshotId(model, version, conditionRequest);
//
//        if (isSearchSuccessful(snapshotId)) {
//            var response = getSearchResultAsJson(snapshotId);
//            return (List<T>) jsonToEntityListParser.parseResponse(response, clazz);
//        }
//        return List.of();
//    }

    public boolean isSearchSuccessful(String snapshotId, int waitTimeInMillis) throws IOException, InterruptedException {
        int waitIntervalInMillis = 400;
        int waitedTime = 0;
        while ((!getSnapshotStatus(snapshotId).get("snapshotStatus").equals("SUCCESSFUL")) && waitedTime < waitTimeInMillis) {
            TimeUnit.MILLISECONDS.sleep(waitIntervalInMillis);
            waitedTime += waitIntervalInMillis;
        }
        return getSnapshotStatus(snapshotId).get("snapshotStatus").equals("SUCCESSFUL");
    }

    public boolean isSearchSuccessful(String snapshotId) throws IOException, InterruptedException {
        return isSearchSuccessful(snapshotId, 10000);
    }


    public String getSearchResultAsJson(String snapshotId) throws IOException {
        return getSearchResultAsJson(snapshotId, 100, 0);
    }

    public String getSearchResultAsJson(String snapshotId, int pageSize, int pageNumber) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s?pageSize=%s&pageNumber=%s", CYODA_API_URL, snapshotId, pageSize, pageNumber);
        return getRequest(url);
    }

    public String getAllEntitiesAsJson(String model, String version) throws IOException {
        return getAllEntitiesAsJson(model, version, 100, 1);
    }

    public String getAllEntitiesAsJson(String model, String version, int pageSize) throws IOException {
        return getAllEntitiesAsJson(model, version, pageSize, 1);
    }

    public String getAllEntitiesAsJson(String model, String version, int pageSize, int pageNumber) throws IOException {
        String url = String.format("%s/api/entity/TREE/%s/%s?pageSize=%s&pageNumber=%s", CYODA_API_URL, model, version, pageSize, pageNumber);
        return getRequest(url);
    }

    public <T extends BaseEntity> List<T> getAllEntitiesAsObjects(String model, String version) throws IOException {
        String json = getAllEntitiesAsJson(model, version);

        Class clazz = EntityRegistry.getClassByModel(model);

        return jsonToEntityListParser.parseResponse(json, clazz);
    }

    public <T extends BaseEntity> List<T> getAllEntitiesAsObjects(String model, String version, int pageSize) throws IOException {
        String json = getAllEntitiesAsJson(model, version, pageSize);

        Class clazz = EntityRegistry.getClassByModel(model);

        return jsonToEntityListParser.parseResponse(json, clazz);
    }

    public <T extends BaseEntity> List<T> getAllEntitiesAsObjects(String model, String version, int pageSize, int pageNumber) throws IOException {
        String json = getAllEntitiesAsJson(model, version, pageSize, pageNumber);

        Class clazz = EntityRegistry.getClassByModel(model);

        return jsonToEntityListParser.parseResponse(json, clazz);
    }

    public String getByIdAsJson(UUID id) throws IOException {
        String url = CYODA_API_URL + String.format("/api/entity/TREE/%s", id);
        return getRequest(url);
    }

    public <T extends BaseEntity> T getByIdAsObject(UUID id) {
        String json = null;
        try {
            json = getByIdAsJson(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return jsonToEntityParser.parseResponse(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(mapper.writeValueAsString(httpGet.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
    }

    public HttpResponse launchTransition(UUID id, String transition) throws IOException {

        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity/transition?entityId=%s&entityClass=%s&transitionName=%s", CYODA_API_URL, entityId, ENTITY_CLASS_NAME, transition);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Authorization", "Bearer " + token);

        return executeHttpRequest(httpPut);
    }

    public List<String> getListTransitions(UUID id) throws IOException {
        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity/fetch/transitions?entityId=%s&entityClass=%s", CYODA_API_URL, entityId, ENTITY_CLASS_NAME);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(mapper.writeValueAsString(httpGet.toString()));

        List<String> stringList = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JsonNode jsonNode = mapper.readTree(responseBody);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    stringList.add(node.asText());
                }
            }
        }

        return stringList;
    }

    public String getCurrentState(UUID id) throws IOException {
        String entityId = id.toString();
        String url = CYODA_API_URL + String.format("/api/platform-api/entity-info/fetch/lazy?entityClass=%s&entityId=%s&columnPath=state", ENTITY_CLASS_NAME, entityId);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(mapper.writeValueAsString(httpGet.toString()));

        JsonNode jsonNode;

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            jsonNode = mapper.readTree(responseBody);
        }

        return jsonNode.get(0).get("value").asText();
    }

    public <T extends BaseEntity> boolean updateEntity(T entity, String transitionName) {
        String model = EntityRegistry.getModelByClass(entity.getClass());
        String entityId = entity.getId().toString();

        String url = CYODA_API_URL + String.format("/api/entity/%s/%s/%s", FORMAT, entityId, transitionName);
        HttpPut httpPut = new HttpPut(url);

        httpPut.setHeader("Content-Type", "application/json");
        httpPut.setHeader("Authorization", "Bearer " + token);

        String json = null;
        try {
            json = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPut.setEntity(requestEntity);

        try {
            HttpResponse resp = executeHttpRequest(httpPut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public CompletableFuture<Boolean> modelExists(String token, String modelName, String entityVersion) {
        String exportModelPath = String.format("model/export/SIMPLE_VIEW/%s/%s", modelName, entityVersion);
        return HttpUtils.sendGetRequest(token, CYODA_API_URL, exportModelPath)
                .thenApply(response ->{
                    int status = (int) response.get("status");
                    return status == 200;
                });
    }
}

