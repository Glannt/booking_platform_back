package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalTypeTranslationDto {
    private Long id;
    private Long rentalTypeId;
    private String locale;
    private String name;
    private String description;
}

