package com.dotnt.booking_travel_backend.sharedroute.dto;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.DirectionType;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteDirectionDto {
    private Long id;
    private Long routeId;
    private DirectionType directionType;
    private String originCity;
    private String destinationCity;
    private BigDecimal originLat;
    private BigDecimal originLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private BigDecimal price;
    private BigDecimal distanceKm;
    private BigDecimal estimatedHours;
    private Boolean active;

    @Builder.Default
    private List<SharedOfficeDto> originOffices = new ArrayList<>();

    @Builder.Default
    private List<SharedOfficeDto> destinationOffices = new ArrayList<>();

    @Builder.Default
    private List<Long> originOfficeIds = new ArrayList<>();

    @Builder.Default
    private List<Long> destinationOfficeIds = new ArrayList<>();

    @Builder.Default
    private List<SharedRouteScheduleDto> schedules = new ArrayList<>();
}
