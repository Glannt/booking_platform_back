package com.dotnt.booking_travel_backend.intercity.internal.service.impl;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityDepartureTimeDto;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityDepartureTime;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityDepartureTimeRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRouteRepository;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityDepartureTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntercityDepartureTimeServiceImpl implements IIntercityDepartureTimeService {

    private final IntercityDepartureTimeRepository departureTimeRepository;
    private final IntercityRouteRepository routeRepository;

    @Override
    @Cacheable(value = "intercityDepartureTimes", key = "'route_' + #routeId")
    public List<IntercityDepartureTimeDto> listByRoute(Long routeId) {
        return listByRoute(routeId, null);
    }

    @Override
    @Cacheable(value = "intercityDepartureTimes", key = "'route_' + #routeId + '_' + (#direction != null ? #direction : 'ALL')")
    public List<IntercityDepartureTimeDto> listByRoute(Long routeId, String direction) {
        List<IntercityDepartureTime> list = (direction != null && !direction.isBlank())
                ? departureTimeRepository.findByRouteIdAndDirectionOrderByOrderIndexAsc(routeId, direction.toUpperCase())
                : departureTimeRepository.findByRouteIdOrderByOrderIndexAsc(routeId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "intercityDepartureTimes", key = "'active_route_' + #routeId")
    public List<IntercityDepartureTimeDto> listActiveByRoute(Long routeId) {
        return listActiveByRoute(routeId, null);
    }

    @Override
    @Cacheable(value = "intercityDepartureTimes", key = "'active_route_' + #routeId + '_' + (#direction != null ? #direction : 'ALL')")
    public List<IntercityDepartureTimeDto> listActiveByRoute(Long routeId, String direction) {
        List<IntercityDepartureTime> list = (direction != null && !direction.isBlank())
                ? departureTimeRepository.findByRouteIdAndDirectionAndActiveTrueOrderByOrderIndexAsc(routeId, direction.toUpperCase())
                : departureTimeRepository.findByRouteIdAndActiveTrueOrderByOrderIndexAsc(routeId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "intercityDepartureTimes", key = "#id")
    public IntercityDepartureTimeDto get(Long id) {
        return departureTimeRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "intercityDepartureTimes", allEntries = true), @CacheEvict(value = "intercityRoutes", allEntries = true) })
    public IntercityDepartureTimeDto create(IntercityDepartureTimeDto dto) {
        IntercityRoute route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found: " + dto.getRouteId()));

        IntercityDepartureTime entity = IntercityDepartureTime.builder()
                .route(route)
                .departureTime(dto.getDepartureTime())
                .direction(dto.getDirection() != null && !dto.getDirection().isBlank() ? dto.getDirection().toUpperCase() : "OUTBOUND")
                .orderIndex(dto.getOrderIndex())
                .active(dto.isActive())
                .build();
        entity = departureTimeRepository.save(entity);
        return toDto(entity);
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "intercityDepartureTimes", allEntries = true), @CacheEvict(value = "intercityRoutes", allEntries = true) })
    public IntercityDepartureTimeDto update(Long id, IntercityDepartureTimeDto dto) {
        return departureTimeRepository.findById(id).map(entity -> {
            IntercityRoute route = routeRepository.findById(dto.getRouteId())
                    .orElseThrow(() -> new RuntimeException("Route not found: " + dto.getRouteId()));

            entity.setRoute(route);
            entity.setDepartureTime(dto.getDepartureTime());
            if (dto.getDirection() != null && !dto.getDirection().isBlank()) {
                entity.setDirection(dto.getDirection().toUpperCase());
            }
            entity.setOrderIndex(dto.getOrderIndex());
            entity.setActive(dto.isActive());
            entity = departureTimeRepository.save(entity);
            return toDto(entity);
        }).orElse(null);
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "intercityDepartureTimes", allEntries = true), @CacheEvict(value = "intercityRoutes", allEntries = true) })
    public void delete(Long id) {
        departureTimeRepository.deleteById(id);
    }

    private IntercityDepartureTimeDto toDto(IntercityDepartureTime entity) {
        return IntercityDepartureTimeDto.builder()
                .id(entity.getId())
                .routeId(entity.getRoute().getId())
                .departureTime(entity.getDepartureTime())
                .direction(entity.getDirection() != null ? entity.getDirection() : "OUTBOUND")
                .orderIndex(entity.getOrderIndex())
                .active(entity.isActive())
                .build();
    }
}
