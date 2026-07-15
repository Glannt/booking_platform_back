package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalTypeDto {
    private Long id;
    private String code;
    private boolean active;
    private List<RentalTypeTranslationDto> translations;
}

