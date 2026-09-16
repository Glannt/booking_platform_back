package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedOfficeDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDirectionDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteDto;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteScheduleDto;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.*;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedOfficeRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteDirectionRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteScheduleRepository;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedRouteServiceImpl implements ISharedRouteService {

    private final SharedRouteRepository routeRepository;
    private final SharedRouteDirectionRepository directionRepository;
    private final SharedOfficeRepository officeRepository;
    private final SharedRouteScheduleRepository scheduleRepository;
    private final ModuleEventPublisher moduleEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<SharedRouteDto> getAllRoutes(boolean activeOnly) {
        List<SharedRoute> routes = activeOnly ? routeRepository.findByActiveTrue() : routeRepository.findAll();
        return routes.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SharedRouteDto getRouteById(Long id) {
        SharedRoute route = routeRepository.findByIdWithDirections(id)
                .orElseThrow(() -> new IllegalArgumentException("Shared route not found with id: " + id));
        return toDto(route);
    }

    @Override
    @Transactional(readOnly = true)
    public SharedRouteDto getRouteBySlug(String slug) {
        SharedRoute route = routeRepository.findBySlugWithDirections(slug)
                .orElseThrow(() -> new IllegalArgumentException("Shared route not found with slug: " + slug));
        return toDto(route);
    }

    @Override
    public SharedRouteDto createRoute(SharedRouteDto dto) {
        String routeName = dto.getName();
        String originCity = "Điểm đi";
        String destCity = "Điểm đến";

        if (dto.getLinkedIntercityRouteId() != null) {
            String[] cities = null;
            try {
                cities = moduleEventPublisher.publishRequest(
                        "intercity.intercityRoute.findOriginDestById", dto.getLinkedIntercityRouteId(), String[].class);
            } catch (Exception e) {
                // ignore
            }
            if (cities != null && cities.length >= 2 && cities[0] != null && cities[1] != null) {
                originCity = cities[0];
                destCity = cities[1];
                if (routeName == null || routeName.trim().isEmpty()) {
                    routeName = originCity + " - " + destCity;
                }
            }
        }

        SharedRoute route = SharedRoute.builder()
                .name(routeName != null ? routeName : "Tuyến xe ghép")
                .vehicleName(dto.getVehicleName() != null ? dto.getVehicleName() : "Dcar Limousine")
                .active(dto.getActive() != null ? dto.getActive() : true)
                .slug(dto.getSlug())
                .linkedIntercityRouteId(dto.getLinkedIntercityRouteId())
                .build();
        route.ensureSlug();
        route = routeRepository.save(route);

        if (dto.getDirections() != null && !dto.getDirections().isEmpty()) {
            for (SharedRouteDirectionDto dirDto : dto.getDirections()) {
                createDirection(route, dirDto);
            }
        } else {
            // Default 2 directions if not specified: OUTBOUND and RETURN
            if (routeName != null && routeName.contains("-")) {
                String[] parts = routeName.split("[-⇌–]");
                if (parts.length > 0) originCity = parts[0].trim();
                if (parts.length > 1) destCity = parts[1].trim();
            }

            SharedRouteDirection outbound = SharedRouteDirection.builder()
                    .route(route)
                    .directionType(DirectionType.OUTBOUND)
                    .originCity(originCity)
                    .destinationCity(destCity)
                    .price(new BigDecimal("250000.00"))
                    .active(true)
                    .build();
            directionRepository.save(outbound);

            SharedRouteDirection returnDir = SharedRouteDirection.builder()
                    .route(route)
                    .directionType(DirectionType.RETURN)
                    .originCity(destCity)
                    .destinationCity(originCity)
                    .price(new BigDecimal("250000.00"))
                    .active(true)
                    .build();
            directionRepository.save(returnDir);
        }

        return getRouteById(route.getId());
    }

    @Override
    public SharedRouteDto updateRoute(Long id, SharedRouteDto dto) {
        SharedRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shared route not found with id: " + id));

        String routeName = dto.getName();
        if (dto.getLinkedIntercityRouteId() != null) {
            route.setLinkedIntercityRouteId(dto.getLinkedIntercityRouteId());
            String[] cities = null;
            try {
                cities = moduleEventPublisher.publishRequest(
                        "intercity.intercityRoute.findOriginDestById", dto.getLinkedIntercityRouteId(), String[].class);
            } catch (Exception e) {
                // ignore
            }
            if (cities != null && cities.length >= 2 && cities[0] != null && cities[1] != null && (routeName == null || routeName.trim().isEmpty())) {
                routeName = cities[0] + " - " + cities[1];
            }
        } else {
            route.setLinkedIntercityRouteId(null);
        }

        if (routeName != null) route.setName(routeName);
        if (dto.getVehicleName() != null) route.setVehicleName(dto.getVehicleName());
        if (dto.getActive() != null) route.setActive(dto.getActive());
        if (dto.getSlug() != null && !dto.getSlug().trim().isEmpty()) route.setSlug(dto.getSlug());
        route.ensureSlug();
        route = routeRepository.save(route);

        if (dto.getDirections() != null) {
            for (SharedRouteDirectionDto dirDto : dto.getDirections()) {
                if (dirDto.getId() != null && directionRepository.existsById(dirDto.getId())) {
                    updateDirection(dirDto.getId(), dirDto);
                } else if (dirDto.getDirectionType() != null) {
                    SharedRouteDirection existing = directionRepository.findByRouteIdAndDirectionType(route.getId(), dirDto.getDirectionType())
                            .orElse(null);
                    if (existing != null) {
                        updateDirection(existing.getId(), dirDto);
                    } else {
                        createDirection(route, dirDto);
                    }
                }
            }
        }

        return getRouteById(route.getId());
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public SharedRouteDirectionDto updateDirection(Long directionId, SharedRouteDirectionDto dto) {
        SharedRouteDirection direction = directionRepository.findById(directionId)
                .orElseThrow(() -> new IllegalArgumentException("Direction not found with id: " + directionId));

        if (dto.getOriginCity() != null) direction.setOriginCity(dto.getOriginCity());
        if (dto.getDestinationCity() != null) direction.setDestinationCity(dto.getDestinationCity());
        if (dto.getPrice() != null) direction.setPrice(dto.getPrice());
        if (dto.getDistanceKm() != null) direction.setDistanceKm(dto.getDistanceKm());
        if (dto.getEstimatedHours() != null) direction.setEstimatedHours(dto.getEstimatedHours());
        if (dto.getActive() != null) direction.setActive(dto.getActive());

        if (dto.getOriginOfficeIds() != null) {
            direction.setOriginOffices(officeRepository.findAllById(dto.getOriginOfficeIds()));
        }
        if (dto.getDestinationOfficeIds() != null) {
            direction.setDestinationOffices(officeRepository.findAllById(dto.getDestinationOfficeIds()));
        }

        direction = directionRepository.save(direction);

        // Update schedules if provided
        if (dto.getSchedules() != null) {
            final SharedRouteDirection finalDirection = direction;
            EntityMergeUtils.mergeCollection(
                    direction.getSchedules(),
                    dto.getSchedules(),
                    sch -> sch.getId() != null ? String.valueOf(sch.getId()) : (sch.getDepartureTime() + "_" + sch.getDaysOfWeek()),
                    schDto -> schDto.getId() != null ? String.valueOf(schDto.getId()) : (schDto.getDepartureTime() + "_" + schDto.getDaysOfWeek()),
                    (existing, schDto) -> {
                        existing.setDepartureTime(schDto.getDepartureTime());
                        existing.setDaysOfWeek(schDto.getDaysOfWeek());
                        existing.setActive(schDto.getActive() != null ? schDto.getActive() : true);
                        existing.setNote(schDto.getNote());
                        existing.setOperatingDays(parseOperatingDays(schDto));
                    },
                    schDto -> SharedRouteSchedule.builder()
                            .direction(finalDirection)
                            .departureTime(schDto.getDepartureTime())
                            .daysOfWeek(schDto.getDaysOfWeek())
                            .active(schDto.getActive() != null ? schDto.getActive() : true)
                            .note(schDto.getNote())
                            .operatingDays(parseOperatingDays(schDto))
                            .build()
            );
        }

        direction = directionRepository.save(direction);
        return toDirectionDto(direction);
    }

    @Override
    public SharedRouteScheduleDto addSchedule(Long directionId, SharedRouteScheduleDto dto) {
        SharedRouteDirection direction = directionRepository.findById(directionId)
                .orElseThrow(() -> new IllegalArgumentException("Direction not found with id: " + directionId));

        SharedRouteSchedule schedule = SharedRouteSchedule.builder()
                .direction(direction)
                .departureTime(dto.getDepartureTime())
                .daysOfWeek(dto.getDaysOfWeek())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .note(dto.getNote())
                .operatingDays(parseOperatingDays(dto))
                .build();
        schedule = scheduleRepository.save(schedule);
        return toScheduleDto(schedule);
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    private void createDirection(SharedRoute route, SharedRouteDirectionDto dirDto) {
        SharedRouteDirection direction = SharedRouteDirection.builder()
                .route(route)
                .directionType(dirDto.getDirectionType() != null ? dirDto.getDirectionType() : DirectionType.OUTBOUND)
                .originCity(dirDto.getOriginCity() != null ? dirDto.getOriginCity() : "")
                .destinationCity(dirDto.getDestinationCity() != null ? dirDto.getDestinationCity() : "")
                .originLat(dirDto.getOriginLat())
                .originLng(dirDto.getOriginLng())
                .destinationLat(dirDto.getDestinationLat())
                .destinationLng(dirDto.getDestinationLng())
                .price(dirDto.getPrice() != null ? dirDto.getPrice() : new BigDecimal("250000.00"))
                .distanceKm(dirDto.getDistanceKm())
                .estimatedHours(dirDto.getEstimatedHours())
                .active(dirDto.getActive() != null ? dirDto.getActive() : true)
                .build();

        if (dirDto.getOriginOfficeIds() != null && !dirDto.getOriginOfficeIds().isEmpty()) {
            direction.setOriginOffices(officeRepository.findAllById(dirDto.getOriginOfficeIds()));
        }
        if (dirDto.getDestinationOfficeIds() != null && !dirDto.getDestinationOfficeIds().isEmpty()) {
            direction.setDestinationOffices(officeRepository.findAllById(dirDto.getDestinationOfficeIds()));
        }

        direction = directionRepository.save(direction);

        if (dirDto.getSchedules() != null) {
            for (SharedRouteScheduleDto schDto : dirDto.getSchedules()) {
                if (schDto.getDepartureTime() != null) {
                    SharedRouteSchedule schedule = SharedRouteSchedule.builder()
                            .direction(direction)
                            .departureTime(schDto.getDepartureTime())
                            .daysOfWeek(schDto.getDaysOfWeek())
                            .active(schDto.getActive() != null ? schDto.getActive() : true)
                            .note(schDto.getNote())
                            .operatingDays(parseOperatingDays(schDto))
                            .build();
                    scheduleRepository.save(schedule);
                }
            }
        }
    }

    private Set<DayOfWeek> parseOperatingDays(SharedRouteScheduleDto schDto) {
        Set<DayOfWeek> days = new HashSet<>();
        if (schDto.getOperatingDays() != null && !schDto.getOperatingDays().isEmpty()) {
            for (String d : schDto.getOperatingDays()) {
                try {
                    days.add(DayOfWeek.valueOf(d.toUpperCase()));
                } catch (Exception ignored) {}
            }
        } else if (schDto.getDaysOfWeek() != null && !schDto.getDaysOfWeek().isBlank()) {
            String[] parts = schDto.getDaysOfWeek().split(",");
            for (String p : parts) {
                try {
                    int val = Integer.parseInt(p.trim());
                    if (val >= 1 && val <= 7) {
                        days.add(DayOfWeek.of(val));
                    }
                } catch (Exception e) {
                    try {
                        days.add(DayOfWeek.valueOf(p.trim().toUpperCase()));
                    } catch (Exception ignored) {}
                }
            }
        }
        if (days.isEmpty()) {
            days.addAll(List.of(DayOfWeek.values()));
        }
        return days;
    }

    private SharedRouteDto toDto(SharedRoute route) {
        List<SharedRouteDirectionDto> directionDtos = route.getDirections() != null
                ? route.getDirections().stream().map(this::toDirectionDto).collect(Collectors.toList())
                : new ArrayList<>();

        String linkedName = null;
        if (route.getLinkedIntercityRouteId() != null) {
            try {
                String[] cities = moduleEventPublisher.publishRequest(
                        "intercity.intercityRoute.findOriginDestById", route.getLinkedIntercityRouteId(), String[].class);
                if (cities != null && cities.length >= 2 && cities[0] != null && cities[1] != null) {
                    linkedName = cities[0] + " - " + cities[1];
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return SharedRouteDto.builder()
                .id(route.getId())
                .name(route.getName())
                .vehicleName(route.getVehicleName())
                .active(route.isActive())
                .slug(route.getSlug())
                .linkedIntercityRouteId(route.getLinkedIntercityRouteId())
                .linkedIntercityRouteName(linkedName)
                .directions(directionDtos)
                .build();
    }

    private SharedRouteDirectionDto toDirectionDto(SharedRouteDirection dir) {
        List<SharedOfficeDto> originOffices = dir.getOriginOffices() != null
                ? dir.getOriginOffices().stream().map(this::toOfficeDto).collect(Collectors.toList())
                : new ArrayList<>();

        List<SharedOfficeDto> destOffices = dir.getDestinationOffices() != null
                ? dir.getDestinationOffices().stream().map(this::toOfficeDto).collect(Collectors.toList())
                : new ArrayList<>();

        List<SharedRouteScheduleDto> schedules = dir.getSchedules() != null
                ? dir.getSchedules().stream().map(this::toScheduleDto).collect(Collectors.toList())
                : new ArrayList<>();

        return SharedRouteDirectionDto.builder()
                .id(dir.getId())
                .routeId(dir.getRoute() != null ? dir.getRoute().getId() : null)
                .directionType(dir.getDirectionType())
                .originCity(dir.getOriginCity())
                .destinationCity(dir.getDestinationCity())
                .originLat(dir.getOriginLat())
                .originLng(dir.getOriginLng())
                .destinationLat(dir.getDestinationLat())
                .destinationLng(dir.getDestinationLng())
                .price(dir.getPrice())
                .distanceKm(dir.getDistanceKm())
                .estimatedHours(dir.getEstimatedHours())
                .active(dir.isActive())
                .originOffices(originOffices)
                .destinationOffices(destOffices)
                .originOfficeIds(originOffices.stream().map(SharedOfficeDto::getId).collect(Collectors.toList()))
                .destinationOfficeIds(destOffices.stream().map(SharedOfficeDto::getId).collect(Collectors.toList()))
                .schedules(schedules)
                .build();
    }

    private SharedOfficeDto toOfficeDto(SharedOffice office) {
        return SharedOfficeDto.builder()
                .id(office.getId())
                .name(office.getName())
                .address(office.getAddress())
                .city(office.getCity())
                .latitude(office.getLatitude())
                .longitude(office.getLongitude())
                .active(office.isActive())
                .build();
    }

    private SharedRouteScheduleDto toScheduleDto(SharedRouteSchedule schedule) {
        Set<String> opDays = schedule.getOperatingDays() != null
                ? schedule.getOperatingDays().stream().map(Enum::name).collect(Collectors.toSet())
                : new HashSet<>();

        return SharedRouteScheduleDto.builder()
                .id(schedule.getId())
                .directionId(schedule.getDirection() != null ? schedule.getDirection().getId() : null)
                .departureTime(schedule.getDepartureTime())
                .daysOfWeek(schedule.getDaysOfWeek())
                .operatingDays(opDays)
                .active(schedule.isActive())
                .note(schedule.getNote())
                .build();
    }
}
