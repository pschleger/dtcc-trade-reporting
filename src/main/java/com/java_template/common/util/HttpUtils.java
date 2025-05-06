package com.java_template.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class HttpUtils {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final ObjectMapper om = new ObjectMapper();

    private static String ensureBearerToken(String token) {
        return token.startsWith("Bearer") ? token : "Bearer " + token;
    }

    private static HttpRequest.Builder createRequestBuilder(String url, String token, String method) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", ensureBearerToken(token));
        }
        return builder.method(method, HttpRequest.BodyPublishers.noBody());
    }


    private static HttpRequest createRequest(String url, String token, String method, Object data) {
        HttpRequest.Builder builder = createRequestBuilder(url, token, method);
        if (data != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(data), StandardCharsets.UTF_8));
        }
        return builder.build();
    }

    private static CompletableFuture<ObjectNode> sendRequest(String url, String token, String method, Object data) {
        HttpRequest request = createRequest(url, token, method, data);
        ObjectNode result = om.createObjectNode();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    logger.info("{} request to {} completed with status {}", method, url, statusCode);
                    try {
                        JsonNode responseJson = om.readTree(responseBody);
                        if (responseJson.isObject()) {
                            result.set("json", (ObjectNode) responseJson);
                        } else if (responseJson.isTextual()) {
                            try {
                                JsonNode parsedJson = om.readTree(responseJson.asText());
                                if (parsedJson.isObject()) {
                                    result.set("json", (ObjectNode) parsedJson);
                                } else {
                                    result.set("json", parsedJson);
                                }
                            } catch (Exception e) {
                                result.put("json", responseJson.asText());
                            }
                        } else {
                            result.put("json", responseJson);
                        }
                        result.put("status", statusCode);
                        return result;
                    } catch (Exception e) {
                        logger.warn("Failed to parse response JSON: {}", e.getMessage());
                        result.put("status", statusCode);
                        result.put("json", responseBody);
                        return result;
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error during {} request to {}", method, url, ex);
                    result.put("status", 500);
                    result.put("json", "Internal error: " + ex.getMessage());
                    return result;
                });
    }

    public static CompletableFuture<ObjectNode> sendGetRequest(String token, String apiUrl, String path, Map<String, String> params) {
        String fullUrl = buildUrlWithParams(apiUrl, path, params);
        return sendRequest(fullUrl, token, "GET", null);
    }

    public static CompletableFuture<ObjectNode> sendGetRequest(String token, String apiUrl, String path) {
        String fullUrl = buildUrlWithParams(apiUrl, path, null);
        return sendRequest(fullUrl, token, "GET", null);
    }

    public static CompletableFuture<ObjectNode> sendPostRequest(String token, String apiUrl, String path, Object data) {
        String fullUrl = buildUrlWithParams(apiUrl, path, null);
        return sendRequest(fullUrl, token, "POST", data);
    }

    public static CompletableFuture<ObjectNode> sendPutRequest(String token, String apiUrl, String path, Object data) {
        String fullUrl = buildUrlWithParams(apiUrl, path, null);
        return sendRequest(fullUrl, token, "PUT", data);
    }

    public static CompletableFuture<ObjectNode> sendDeleteRequest(String token, String apiUrl, String path) {
        String fullUrl = buildUrlWithParams(apiUrl, path, null);
        return sendRequest(fullUrl, token, "DELETE", null);
    }

    private static String buildUrlWithParams(String apiUrl, String path, Map<String, String> params) {
        String baseUrl = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        String fullUrl = (path == null || path.isBlank()) ? baseUrl : baseUrl + "/" + path;

        if (params == null || params.isEmpty()) {
            return fullUrl;
        }
        String queryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return fullUrl + "?" + queryString;
    }
}
