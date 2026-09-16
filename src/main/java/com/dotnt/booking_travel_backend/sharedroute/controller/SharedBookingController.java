package com.dotnt.booking_travel_backend.sharedroute.controller;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingRequest;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingResponse;
import com.dotnt.booking_travel_backend.sharedroute.service.ISharedBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shared-bookings")
@RequiredArgsConstructor
public class SharedBookingController {

    private final ISharedBookingService bookingService;

    @PostMapping
    public ResponseEntity<SharedRouteBookingResponse> createBooking(@RequestBody SharedRouteBookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<SharedRouteBookingResponse> getByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(bookingService.getBookingByGroupId(groupId));
    }

    @GetMapping
    public ResponseEntity<List<SharedRouteBookingResponse.BookingItemDto>> getAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status != null) {
            bookingService.updateBookingStatus(id, status);
        }
        return ResponseEntity.noContent().build();
    }
}
