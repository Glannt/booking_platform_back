package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.internal.entity.FlightTracking;
import com.dotnt.booking_travel_backend.booking.internal.repository.FlightTrackingRepository;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightTrackingController {

    private final FlightTrackingRepository repository;

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
            return ResponseEntity.ok(updateMockFlightTracking(tracking, cleanedNumber, now));
        } else {
            FlightTracking tracking = new FlightTracking();
            tracking.setFlightNumber(cleanedNumber);
            return ResponseEntity.ok(updateMockFlightTracking(tracking, cleanedNumber, now));
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
        Random random = new Random();
        int statusSelect = random.nextInt(3);
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
