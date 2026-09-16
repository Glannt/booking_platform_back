package com.dotnt.booking_travel_backend.selfdrive.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveTypeTranslationDto {
    private Long id;
    private Long rentalTypeId;
    private String locale;
    private String name;
    private String description;
}
