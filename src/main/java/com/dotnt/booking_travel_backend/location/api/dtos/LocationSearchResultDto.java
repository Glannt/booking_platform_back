package com.dotnt.booking_travel_backend.location.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationSearchResultDto {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String category;
    private String name;
    private String address;
}
