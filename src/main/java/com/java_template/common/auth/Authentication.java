package com.java_template.common.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.java_template.common.config.Config.*;

@Service
public class Authentication {

    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);

    private final ObjectMapper om;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private String refreshToken;
    private String accessToken;
    private Long accessTokenExpiry; // epoch seconds

    public Authentication(ObjectMapper om) {
        this.om = om;
    }

    public synchronized void invalidateTokens() {
        logger.info("Invalidating cached tokens");
        refreshToken = null;
        accessToken = null;
        accessTokenExpiry = null;
    }

    private synchronized String login() throws Exception {
        String url = String.format("%s/auth/login", CYODA_API_URL);
        logger.info("Authenticating with Cyoda API at {}", url);

        HttpPost httpPost = new HttpPost(url);
        String jsonPayload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", API_KEY, API_SECRET);
        StringEntity entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        logger.info("Authentication response code: {}", statusCode);

        String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        if (statusCode != 200) {
            logger.error("Login failed ({}): {}", statusCode, responseString);
            throw new RuntimeException("Login failed: " + statusCode + " " + responseString);
        }

        JsonNode rootNode = om.readTree(responseString);
        String token = rootNode.has("refreshToken") ? rootNode.get("refreshToken").asText() :
                rootNode.has("refresh_token") ? rootNode.get("refresh_token").asText() : null;

        if (token == null || token.isEmpty()) {
            logger.error("No refresh token found in login response");
            throw new RuntimeException("Refresh token missing in login response");
        }

        logger.info("Successfully obtained refresh token");
        return token;
    }

    private synchronized void refreshAccessToken() throws Exception {
        if (refreshToken == null) {
            refreshToken = login();
        }

        String url = String.format("%s/auth/token", CYODA_API_URL);
        logger.info("Refreshing access token at {}", url);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + refreshToken);

        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if (statusCode == 401 || statusCode == 403) {
            logger.warn("Refresh token invalid ({}); re-authenticating", statusCode);
            invalidateTokens();
            refreshToken = login();
            refreshAccessToken();
            return;
        }

        if (statusCode != 200) {
            logger.error("Token refresh failed ({}): {}", statusCode, responseString);
            throw new RuntimeException("Token refresh failed: " + statusCode + " " + responseString);
        }

        JsonNode rootNode = om.readTree(responseString);
        String token = rootNode.has("token") ? rootNode.get("token").asText() :
                rootNode.has("access_token") ? rootNode.get("access_token").asText() : null;
        long expiry = rootNode.has("expires_in") ? rootNode.get("expires_in").asLong() : 300;

        if (token == null || token.isEmpty()) {
            logger.error("No access token found in token response");
            throw new RuntimeException("Access token missing in token response");
        }

        accessToken = token;
        accessTokenExpiry = Instant.now().getEpochSecond() + expiry;

        logger.info("Successfully obtained access token (expires in {} seconds)", expiry);
    }

    public synchronized String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        try {
            if (accessToken == null || accessTokenExpiry == null || now >= accessTokenExpiry - 60) {
                refreshAccessToken();
            }
        } catch (Exception e) {
            logger.error("Error obtaining access token", e);
            throw new RuntimeException(e);
        }
        return accessToken;
    }

    @PreDestroy
    public void cleanup() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.warn("Error closing HTTP client", e);
        }
    }
}
