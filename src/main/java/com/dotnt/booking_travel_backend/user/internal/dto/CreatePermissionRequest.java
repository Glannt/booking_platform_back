package com.dotnt.booking_travel_backend.user.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {
    private String code;
    private String name;
    private String description;
}