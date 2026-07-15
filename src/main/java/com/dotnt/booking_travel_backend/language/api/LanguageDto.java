package com.dotnt.booking_travel_backend.language.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageDto {
    private Long id;
    private String code;
    private String name;
    private String flagIcon;
    private Boolean isDefault;
    private String status;
    private Integer orderIndex;
}
