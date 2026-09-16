package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicAmenityDTO {
    private Long id;
    private String categoryCode;
    private String code;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
}
