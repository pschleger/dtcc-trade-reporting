package com.java_template.common.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtils {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger logger = Logger.getLogger(HttpUtils.class.getName());

    private static String ensureBearerToken(String token) {
        return token.startsWith("Bearer") ? token : "Bearer " + token;
    }

    private static HttpRequest.Builder createRequestBuilder(String url, String token, String method) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", ensureBearerToken(token))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.noBody());
    }

    private static HttpRequest createRequest(String url, String token, String method, Object data) {
        HttpRequest.Builder builder = createRequestBuilder(url, token, method);
        if (data != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(data), StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private static CompletableFuture<Map<String, Object>> sendRequest(String url, String token, String method, Object data) {
        HttpRequest request = createRequest(url, token, method, data);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    logger.info(method + " request to " + url + " completed with status " + statusCode);
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", statusCode);
                    result.put("json", responseBody);
                    return result;
                })
                .exceptionally(ex -> {
                    logger.log(Level.SEVERE, "Error during " + method + " request to " + url, ex);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", 500);
                    errorResult.put("json", "Internal error: " + ex.getMessage());
                    return errorResult;
                });
    }

    public static CompletableFuture<Map<String, Object>> sendGetRequest(String token, String apiUrl, String path) {
        return sendRequest(apiUrl + "/" + path, token, "GET", null);
    }

    public static CompletableFuture<Map<String, Object>> sendPostRequest(String token, String apiUrl, String path, Object data) {
        return sendRequest(apiUrl + "/" + path, token, "POST", data);
    }

    public static CompletableFuture<Map<String, Object>> sendPutRequest(String token, String apiUrl, String path, Object data) {
        return sendRequest(apiUrl + "/" + path, token, "PUT", data);
    }

    public static CompletableFuture<Map<String, Object>> sendDeleteRequest(String token, String apiUrl, String path) {
        return sendRequest(apiUrl + "/" + path, token, "DELETE", null);
    }
}
