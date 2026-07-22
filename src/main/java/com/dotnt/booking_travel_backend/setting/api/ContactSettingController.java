package com.dotnt.booking_travel_backend.setting.api;

import com.dotnt.booking_travel_backend.setting.internal.entity.ContactSetting;
import com.dotnt.booking_travel_backend.setting.internal.service.ContactSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact-settings")
@RequiredArgsConstructor
public class ContactSettingController {

    private final ContactSettingService service;

    @GetMapping
    public ResponseEntity<ContactSetting> get() {
        return ResponseEntity.ok(service.getContactSetting());
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ContactSetting> update(@RequestBody ContactSetting payload) {
        return ResponseEntity.ok(service.updateContactSetting(payload));
    }
}
