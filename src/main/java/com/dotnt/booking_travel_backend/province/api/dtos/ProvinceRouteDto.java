package com.dotnt.booking_travel_backend.province.api.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceRouteDto {
    private Long id;
    private String code;
    private String origin;
    private String destination;
    private Double distance;
    private Integer duration;
    private boolean active;
}

