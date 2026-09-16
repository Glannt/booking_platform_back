package com.dotnt.booking_travel_backend.sharedroute.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteScheduleDto {
    private Long id;
    private Long directionId;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime departureTime;

    private String daysOfWeek;
    private java.util.Set<String> operatingDays;
    private Boolean active;
    private String note;
}
