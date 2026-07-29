package com.dotnt.booking_travel_backend.location.internal.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NominatimService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<NominatimResult> search(String query, int limit) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String urlString = String.format("%s?q=%s&format=json&limit=%d", BASE_URL, encodedQuery, limit);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("User-Agent", "BookingTravel/1.0 (https://booking-travel.com; admin@booking-travel.com)")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "vi")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            log.info("Nominatim request: {}", urlString);
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Nominatim status: {}", httpResponse.statusCode());

            if (httpResponse.statusCode() != 200) {
                log.warn("Nominatim returned status {}: {}", httpResponse.statusCode(),
                        httpResponse.body().length() < 500 ? httpResponse.body() : httpResponse.body().substring(0, 500));
                return List.of();
            }

            String body = httpResponse.body();
            if (body == null || body.isBlank() || body.equals("[]")) {
                log.info("Nominatim returned empty results for '{}'", query);
                return List.of();
            }

            log.info("Nominatim response ({} chars): {}", body.length(),
                    body.length() < 1000 ? body : body.substring(0, 1000));

            List<Map<String, Object>> rawResults = objectMapper.readValue(
                    body, new TypeReference<>() {});

            List<NominatimResult> results = new ArrayList<>();
            for (Map<String, Object> raw : rawResults) {
                try {
                    NominatimResult result = parseResult(raw);
                    if (result != null) {
                        results.add(result);
                    }
                } catch (Exception e) {
                    log.debug("Skipping unparseable Nominatim result: {}", e.getMessage());
                }
            }

            log.info("Nominatim: {} raw → {} valid results for '{}'", rawResults.size(), results.size(), query);
            return results;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Nominatim request interrupted for '{}': {}", query, e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Nominatim API call failed for query '{}': {}", query, e.getMessage(), e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private NominatimResult parseResult(Map<String, Object> raw) {
        String latStr = (String) raw.get("lat");
        String lonStr = (String) raw.get("lon");
        if (latStr == null || lonStr == null) return null;

        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);

        String name = extractName(raw);
        String displayName = (String) raw.get("display_name");
        String category = extractCategory(raw);

        Map<String, Object> address = (Map<String, Object>) raw.get("address");
        String addressStr = displayName;
        if (address != null) {
            String city = (String) address.get("city");
            String state = (String) address.get("state");
            String country = (String) address.get("country");
            StringBuilder sb = new StringBuilder();
            if (city != null && !city.isBlank()) sb.append(city);
            if (state != null && !state.isBlank()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(state);
            }
            if (country != null && !country.isBlank()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(country);
            }
            if (!sb.isEmpty()) addressStr = sb.toString();
        }

        return NominatimResult.builder()
                .latitude(lat)
                .longitude(lon)
                .name(name)
                .address(addressStr)
                .category(category)
                .build();
    }

    @SuppressWarnings("unchecked")
    private String extractName(Map<String, Object> raw) {
        Map<String, Object> address = (Map<String, Object>) raw.get("address");
        if (address != null) {
            String city = (String) address.get("city");
            String town = (String) address.get("town");
            String village = (String) address.get("village");
            String suburb = (String) address.get("suburb");

            String placeName = (city != null && !city.isBlank()) ? city
                    : (town != null && !town.isBlank()) ? town
                    : (village != null && !village.isBlank()) ? village
                    : (suburb != null && !suburb.isBlank()) ? suburb
                    : null;

            if (placeName != null) return placeName;
        }

        String name = (String) raw.get("name");
        if (name != null && !name.isBlank()) {
            return name.split(",")[0].trim();
        }

        String displayName = (String) raw.get("display_name");
        if (displayName != null) {
            return displayName.split(",")[0].trim();
        }

        return "Unknown";
    }

    @SuppressWarnings("unchecked")
    private String extractCategory(Map<String, Object> raw) {
        String type = (String) raw.get("type");
        String clazz = (String) raw.get("class");

        if (type == null) return "place";

        return switch (type.toLowerCase()) {
            case "city", "town", "village", "hamlet", "municipality" -> "city";
            case "hotel", "motel", "hostel", "guest_house", "apartment" -> "hotel";
            case "resort" -> "resort";
            case "beach" -> "beach";
            case "attraction", "museum", "monument", "memorial", "castle", "temple", "church", "mosque" -> "landmark";
            case "restaurant", "cafe", "bar", "pub" -> "restaurant";
            case "shop", "marketplace", "supermarket" -> "shopping";
            case "station", "airport", "bus_station", "railway" -> "transport";
            case "peak", "volcano", "cliff" -> "nature";
            default -> {
                if ("tourism".equals(clazz)) yield "landmark";
                if ("shop".equals(clazz)) yield "shopping";
                if ("amenity".equals(clazz)) yield "amenity";
                yield "place";
            }
        };
    }

    @Data
    @lombok.Builder
    public static class NominatimResult {
        private double latitude;
        private double longitude;
        private String name;
        private String address;
        private String category;
    }
}
