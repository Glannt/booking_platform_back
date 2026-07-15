package com.dotnt.booking_travel_backend.user.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolePermissionsRequest {
    private List<String> permissions;
}
