package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourPolicyDto;
import com.dotnt.booking_travel_backend.tour.internal.service.TourPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tour-policies")
@RequiredArgsConstructor
public class TourPolicyPublicController {

    private final TourPolicyService service;

    @GetMapping
    public ResponseEntity<List<TourPolicyDto>> getActivePolicies() {
        return ResponseEntity.ok(service.getActivePolicies());
    }
}
