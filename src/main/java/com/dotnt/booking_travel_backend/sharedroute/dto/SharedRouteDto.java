package com.dotnt.booking_travel_backend.sharedroute.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteDto {
    private Long id;
    private String name;
    private String vehicleName;
    private Boolean active;
    private String slug;
    private Long linkedIntercityRouteId;
    private String linkedIntercityRouteName;

    @Builder.Default
    private List<SharedRouteDirectionDto> directions = new ArrayList<>();
}
