package com.java_template.common.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.java_template.common.config.Config.*;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class Authentication {

    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private OAuth2AuthorizedClient cachedClient;
    private final ReentrantLock lock = new ReentrantLock(); // 🔐 fine-grained lock

    public Authentication() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("cyoda")
                .tokenUri(CYODA_API_URL + "/oauth/token")
                .clientId(CYODA_CLIENT_ID)
                .clientSecret(CYODA_CLIENT_SECRET)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("your-scope")
                .build();

        var registrationRepo = new InMemoryClientRegistrationRepository(registration);
        var clientService = new InMemoryOAuth2AuthorizedClientService(registrationRepo);
        this.authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                registrationRepo, clientService
        );
    }

    public String getAccessToken() {
        Instant now = Instant.now();

        // 🔍 Fast path: no lock needed if cached token is valid
        if (isTokenValid(now)) {
            return cachedClient.getAccessToken().getTokenValue();
        }

        // 🔐 Only block on token refresh
        lock.lock();
        try {
            // Re-check inside lock (double-checked locking)
            if (isTokenValid(Instant.now())) {
                return cachedClient.getAccessToken().getTokenValue();
            }

            logger.info("Fetching new OAuth2 access token");
            OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("cyoda")
                    .principal("cyoda-client")
                    .build();

            cachedClient = authorizedClientManager.authorize(request);

            if (cachedClient == null || cachedClient.getAccessToken() == null) {
                throw new IllegalStateException("Failed to obtain access token");
            }

            OAuth2AccessToken token = cachedClient.getAccessToken();
            logger.info("Obtained new access token, expires at: {}", token.getExpiresAt());
            return token.getTokenValue();
        } finally {
            lock.unlock();
        }
    }

    public void invalidateTokens() {
        lock.lock();
        try {
            logger.info("Invalidating cached OAuth2 access token");
            cachedClient = null;
        } finally {
            lock.unlock();
        }
    }

    private boolean isTokenValid(Instant now) {
        if (cachedClient == null) return false;
        OAuth2AccessToken token = cachedClient.getAccessToken();
        return token != null && token.getExpiresAt() != null &&
                now.isBefore(token.getExpiresAt().minusSeconds(60));
    }
}