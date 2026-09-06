package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.internal.entity.FlightTracking;
import com.dotnt.booking_travel_backend.booking.internal.repository.FlightTrackingRepository;
import com.dotnt.booking_travel_backend.booking.internal.service.OpenSkyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightTrackingController {

    private final RestClient restClient = RestClient.create();

    private final FlightTrackingRepository repository;
    private final ObjectMapper objectMapper;
    private final OpenSkyService openSkyService;

    @Value("${app.flight.api-key:}")
    private String apiKey;

    @GetMapping("/track/{flightNumber}")
    public ResponseEntity<?> trackFlight(@PathVariable String flightNumber) {
        String cleanedNumber = flightNumber.trim().toUpperCase();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Flight tracking service chưa được cấu hình. Vui lòng liên hệ quản trị viên."
            );
        }

        Optional<FlightTracking> cached = repository.findByFlightNumber(cleanedNumber);
        LocalDateTime now = LocalDateTime.now();
        if (cached.isPresent()) {
            FlightTracking tracking = cached.get();
            if (tracking.getLastCheckedAt() != null && tracking.getLastCheckedAt().isAfter(now.minusHours(1))) {
                return ResponseEntity.ok(tracking);
            }
            return ResponseEntity.ok(fetchFlightTracking(tracking, cleanedNumber, now));
        }

        FlightTracking tracking = new FlightTracking();
        tracking.setFlightNumber(cleanedNumber);
        return ResponseEntity.ok(fetchFlightTracking(tracking, cleanedNumber, now));
    }

    @GetMapping("/opensky/track")
    public ResponseEntity<?> trackOpenSky(@RequestParam("callsign") String callsign) {
        if (!openSkyService.isConfigured()) {
            throw new IllegalArgumentException(
                "OpenSky chưa được cấu hình. Vui lòng nhập OpenSky Client ID và Secret."
            );
        }
        try {
            var result = openSkyService.findByCallsign(callsign);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("OpenSky: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<FlightTracking>> getHistory() {
        List<FlightTracking> history = repository.findAll(
            Sort.by(Sort.Direction.DESC, "lastCheckedAt")
        );
        return ResponseEntity.ok(history);
    }

    private FlightTracking fetchFlightTracking(FlightTracking tracking, String flightNumber, LocalDateTime now) {
        try {
            String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey.trim() + "&flight_iata=" + flightNumber;
            String jsonResponse = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode firstFlight = data.get(0);

                String airlineName = firstFlight.path("airline").path("name").asText("Unknown Airline");
                tracking.setAirlineName(airlineName);

                String depAirport = firstFlight.path("departure").path("airport").asText("");
                String depIata = firstFlight.path("departure").path("iata").asText("");
                if (!depIata.isEmpty()) {
                    tracking.setDepartureAirport(depIata + (depAirport.isEmpty() ? "" : " (" + depAirport + ")"));
                } else {
                    tracking.setDepartureAirport(depAirport);
                }

                String arrAirport = firstFlight.path("arrival").path("airport").asText("");
                String arrIata = firstFlight.path("arrival").path("iata").asText("");
                if (!arrIata.isEmpty()) {
                    tracking.setArrivalAirport(arrIata + (arrAirport.isEmpty() ? "" : " (" + arrAirport + ")"));
                } else {
                    tracking.setArrivalAirport(arrAirport);
                }

                tracking.setScheduledDeparture(parseDateTime(firstFlight.path("departure").path("scheduled").asText(null)));
                tracking.setScheduledArrival(parseDateTime(firstFlight.path("arrival").path("scheduled").asText(null)));
                tracking.setActualDeparture(parseDateTime(firstFlight.path("departure").path("actual").asText(null)));
                tracking.setActualArrival(parseDateTime(firstFlight.path("arrival").path("actual").asText(null)));

                String status = firstFlight.path("flight_status").asText("Scheduled");
                if (status != null && !status.isEmpty()) {
                    status = Character.toUpperCase(status.charAt(0)) + status.substring(1);
                }
                tracking.setStatus(status);
                tracking.setLastCheckedAt(now);

                return repository.save(tracking);
            }

            throw new IllegalArgumentException("Không tìm thấy thông tin chuyến bay " + flightNumber);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể kết nối dịch vụ flight tracking: " + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
