package com.dotnt.booking_travel_backend.booking.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OpenSkyService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    private final String clientId;
    private final String clientSecret;

    private final Cache<String, String> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(50, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    private final Cache<String, JsonNode> statesCache = Caffeine.newBuilder()
            .expireAfterWrite(45, TimeUnit.SECONDS)
            .maximumSize(1)
            .build();

    public OpenSkyService(
            ObjectMapper objectMapper,
            @Value("${app.opensky.client-id:}") String clientId,
            @Value("${app.opensky.client-secret:}") String clientSecret
    ) {
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public record OpenSkyState(
            String icao24,
            String callsign,
            String originCountry,
            Long timePosition,
            Long lastContact,
            Double longitude,
            Double latitude,
            Double barometricAltitude,
            Boolean onGround,
            Double velocity,
            Double trueTrack,
            Double verticalRate
    ) {}

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public List<OpenSkyState> findByCallsign(String callsign) {
        String cleaned = callsign.trim().toUpperCase();
        JsonNode root = getStates();
        JsonNode states = root.path("states");
        if (states.isMissingNode() || !states.isArray()) {
            return List.of();
        }

        List<OpenSkyState> result = new ArrayList<>();
        for (JsonNode raw : states) {
            if (!raw.isArray()) continue;
            JsonNode csNode = raw.get(1);
            if (csNode == null || csNode.isNull()) continue;
            String cs = csNode.asText("").trim().toUpperCase();
            if (cs.isEmpty() || !cs.startsWith(cleaned)) continue;

            result.add(new OpenSkyState(
                    getText(raw, 0),
                    cs,
                    getText(raw, 2),
                    getLong(raw, 3),
                    getLong(raw, 4),
                    getDouble(raw, 5),
                    getDouble(raw, 6),
                    getDouble(raw, 7),
                    getBoolean(raw, 8),
                    getDouble(raw, 9),
                    getDouble(raw, 10),
                    getDouble(raw, 11)
            ));
        }
        return result;
    }

    private JsonNode getStates() {
        String cacheKey = "states";
        JsonNode cached = statesCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        String token = getToken();
        String json = restClient.get()
                .uri("https://opensky-network.org/api/states/all")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(json);
            statesCache.put(cacheKey, root);
            return root;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenSky states response: " + e.getMessage());
        }
    }

    private String getToken() {
        String cached = tokenCache.getIfPresent("token");
        if (cached != null) return cached;

        if (!isConfigured()) {
            throw new RuntimeException("OpenSky client credentials chưa được cấu hình");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        String json = restClient.post()
                .uri("https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(json);
            String token = root.path("access_token").asText();
            if (token.isEmpty()) {
                throw new RuntimeException("Empty access token from OpenSky auth");
            }
            tokenCache.put("token", token);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get OpenSky access token: " + e.getMessage());
        }
    }

    private String getText(JsonNode arr, int index) {
        JsonNode n = arr.get(index);
        return n == null || n.isNull() ? null : n.asText(null);
    }

    private Long getLong(JsonNode arr, int index) {
        JsonNode n = arr.get(index);
        return n == null || n.isNull() ? null : n.asLong();
    }

    private Double getDouble(JsonNode arr, int index) {
        JsonNode n = arr.get(index);
        return n == null || n.isNull() ? null : n.asDouble();
    }

    private Boolean getBoolean(JsonNode arr, int index) {
        JsonNode n = arr.get(index);
        return n != null && !n.isNull() && n.asBoolean();
    }
}
