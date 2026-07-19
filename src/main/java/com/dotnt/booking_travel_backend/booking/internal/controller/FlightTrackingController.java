package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.internal.entity.FlightTracking;
import com.dotnt.booking_travel_backend.booking.internal.repository.FlightTrackingRepository;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightTrackingController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final RestClient restClient = RestClient.create();

    private final FlightTrackingRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${app.flight.api-key:}")
    private String apiKey;

    @GetMapping("/track/{flightNumber}")
    @SuccessResponse("Flight status tracked successfully!")
    public ResponseEntity<FlightTracking> trackFlight(@PathVariable String flightNumber) {
        String cleanedNumber = flightNumber.trim().toUpperCase();
        Optional<FlightTracking> cached = repository.findByFlightNumber(cleanedNumber);

        LocalDateTime now = LocalDateTime.now();
        if (cached.isPresent()) {
            FlightTracking tracking = cached.get();
            if (tracking.getLastCheckedAt() != null && tracking.getLastCheckedAt().isAfter(now.minusHours(1))) {
                return ResponseEntity.ok(tracking);
            }
            // Update cache
            return ResponseEntity.ok(fetchOrMockFlightTracking(tracking, cleanedNumber, now));
        } else {
            FlightTracking tracking = new FlightTracking();
            tracking.setFlightNumber(cleanedNumber);
            return ResponseEntity.ok(fetchOrMockFlightTracking(tracking, cleanedNumber, now));
        }
    }

    private FlightTracking fetchOrMockFlightTracking(FlightTracking tracking, String flightNumber, LocalDateTime now) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return updateMockFlightTracking(tracking, flightNumber, now);
        }

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
                
                // Parse airline name
                String airlineName = firstFlight.path("airline").path("name").asText("Unknown Airline");
                tracking.setAirlineName(airlineName);

                // Parse departure airport
                String depAirport = firstFlight.path("departure").path("airport").asText("");
                String depIata = firstFlight.path("departure").path("iata").asText("");
                if (!depIata.isEmpty()) {
                    tracking.setDepartureAirport(depIata + (depAirport.isEmpty() ? "" : " (" + depAirport + ")"));
                } else {
                    tracking.setDepartureAirport(depAirport);
                }

                // Parse arrival airport
                String arrAirport = firstFlight.path("arrival").path("airport").asText("");
                String arrIata = firstFlight.path("arrival").path("iata").asText("");
                if (!arrIata.isEmpty()) {
                    tracking.setArrivalAirport(arrIata + (arrAirport.isEmpty() ? "" : " (" + arrAirport + ")"));
                } else {
                    tracking.setArrivalAirport(arrAirport);
                }

                // Parse times
                tracking.setScheduledDeparture(parseDateTime(firstFlight.path("departure").path("scheduled").asText(null)));
                tracking.setScheduledArrival(parseDateTime(firstFlight.path("arrival").path("scheduled").asText(null)));
                tracking.setActualDeparture(parseDateTime(firstFlight.path("departure").path("actual").asText(null)));
                tracking.setActualArrival(parseDateTime(firstFlight.path("arrival").path("actual").asText(null)));

                // Parse status
                String status = firstFlight.path("flight_status").asText("Scheduled");
                if (status != null && !status.isEmpty()) {
                    status = Character.toUpperCase(status.charAt(0)) + status.substring(1);
                }
                tracking.setStatus(status);
                tracking.setLastCheckedAt(now);

                return repository.save(tracking);
            }
        } catch (Exception e) {
            System.err.println("Error calling Aviationstack API: " + e.getMessage());
        }

        return updateMockFlightTracking(tracking, flightNumber, now);
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

    private FlightTracking updateMockFlightTracking(FlightTracking tracking, String flightNumber, LocalDateTime now) {
        String airline = "Vietnam Airlines";
        String prefix = flightNumber.substring(0, Math.min(2, flightNumber.length()));

        if (prefix.equals("VJ")) {
            airline = "VietJet Air";
        } else if (prefix.equals("QH")) {
            airline = "Bamboo Airways";
        } else if (prefix.equals("VU")) {
            airline = "Vietravel Airlines";
        } else if (prefix.equals("VN")) {
            airline = "Vietnam Airlines";
        }

        tracking.setAirlineName(airline);
        tracking.setDepartureAirport("SGN (Tân Sơn Nhất)");
        tracking.setArrivalAirport("CXR (Cam Ranh)");

        // Mock scheduled times around today
        LocalDateTime scheduledDep = now.toLocalDate().atTime(10, 0);
        LocalDateTime scheduledArr = now.toLocalDate().atTime(11, 15);
        tracking.setScheduledDeparture(scheduledDep);
        tracking.setScheduledArrival(scheduledArr);

        // Mock actual status
        int statusSelect = SECURE_RANDOM.nextInt(3);
        if (statusSelect == 0) {
            tracking.setStatus("Scheduled");
            tracking.setActualDeparture(null);
            tracking.setActualArrival(null);
        } else if (statusSelect == 1) {
            tracking.setStatus("Delayed (+15m)");
            tracking.setActualDeparture(scheduledDep.plusMinutes(15));
            tracking.setActualArrival(scheduledArr.plusMinutes(15));
        } else {
            tracking.setStatus("Arrived");
            tracking.setActualDeparture(scheduledDep.minusMinutes(5));
            tracking.setActualArrival(scheduledArr.minusMinutes(3));
        }

        tracking.setLastCheckedAt(now);
        return repository.save(tracking);
    }
}
