package com.dotnt.booking_travel_backend.selfdrive.api.dtos;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveTypeDto {
    private Long id;
    private String code;
    private boolean active;
    private List<SelfDriveTypeTranslationDto> translations;
}
