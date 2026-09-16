package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDirectionDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteScheduleDto;

import java.util.List;

public interface ISharedRouteService {
    List<SharedRouteDto> getAllRoutes(boolean activeOnly);
    SharedRouteDto getRouteById(Long id);
    SharedRouteDto getRouteBySlug(String slug);
    SharedRouteDto createRoute(SharedRouteDto dto);
    SharedRouteDto updateRoute(Long id, SharedRouteDto dto);
    void deleteRoute(Long id);

    SharedRouteDirectionDto updateDirection(Long directionId, SharedRouteDirectionDto dto);

    SharedRouteScheduleDto addSchedule(Long directionId, SharedRouteScheduleDto dto);
    void deleteSchedule(Long scheduleId);
}
