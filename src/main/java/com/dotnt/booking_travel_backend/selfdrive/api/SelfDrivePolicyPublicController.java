package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDrivePolicyDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDrivePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/self-drive-policies")
@RequiredArgsConstructor
public class SelfDrivePolicyPublicController {

    private final ISelfDrivePolicyService service;

    @GetMapping
    public ResponseEntity<List<SelfDrivePolicyDto>> getActivePolicies() {
        return ResponseEntity.ok(service.getActivePolicies());
    }
}
