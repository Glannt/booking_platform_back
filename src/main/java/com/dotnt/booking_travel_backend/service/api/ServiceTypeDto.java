package com.dotnt.booking_travel_backend.service.api;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTypeDto {
    private Long id;
    private String code;
    private String icon;
    private int displayOrder;
    private boolean active;
    private List<ServiceTypeTranslationDto> translations;
}

