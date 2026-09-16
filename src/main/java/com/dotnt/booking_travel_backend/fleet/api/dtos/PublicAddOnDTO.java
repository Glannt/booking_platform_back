package com.dotnt.booking_travel_backend.fleet.api.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicAddOnDTO {
    private Long id;
    private String categoryCode;
    private String code;
    private String name;
    private String description;
    private String value;
    private String icon;
    private Integer displayOrder;
}
