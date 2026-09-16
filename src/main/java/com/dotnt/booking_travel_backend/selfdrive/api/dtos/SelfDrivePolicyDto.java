package com.dotnt.booking_travel_backend.selfdrive.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDrivePolicyDto {
    private Long id;
    private String code;
    private String icon;
    private int sortOrder;
    private boolean active;
    private List<SelfDrivePolicyTranslationDto> translations;
}
