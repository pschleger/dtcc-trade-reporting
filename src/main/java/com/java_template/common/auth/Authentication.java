package com.java_template.common.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import static com.java_template.common.config.Config.*;

import javax.annotation.PostConstruct;

@Service
public class Authentication {

    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);

    @Getter
    private String token;

    private final ObjectMapper om;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public Authentication(ObjectMapper om) {
        this.om = om;
    }

    @PostConstruct
    public void authenticate() throws Exception {
        String url = String.format("%s/auth/login", CYODA_API_URL);
        HttpPost httpPost = new HttpPost(url);

        String jsonPayload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", API_KEY, API_SECRET);
        StringEntity entity = new StringEntity(jsonPayload);

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        HttpResponse response = httpClient.execute(httpPost);

        HttpEntity responseEntity = response.getEntity();
        logger.info("Authentication response code: {}", response.getStatusLine().getStatusCode());
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");

        JsonNode rootNode = om.readTree(responseString);
        token = rootNode.path("token").asText();

        httpClient.close();
    }
}
