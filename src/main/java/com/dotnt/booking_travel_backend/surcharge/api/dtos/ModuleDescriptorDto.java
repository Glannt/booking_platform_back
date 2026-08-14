package com.dotnt.booking_travel_backend.surcharge.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDescriptorDto {
    private String moduleCode;
    private String moduleName;
}
