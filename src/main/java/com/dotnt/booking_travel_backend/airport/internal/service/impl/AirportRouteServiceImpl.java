package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRouteRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirportRouteServiceImpl implements IAirportRouteService {
    private final AirportRouteRepository repository;

    @Override
    public AirportRouteDto create(AirportRouteDto dto) {
        AirportRoute r = AirportRoute.builder()
                .code(dto.getCode())
                .airportName(dto.getAirportName())
                .pickupLocation(dto.getPickupLocation())
                .dropoffLocation(dto.getDropoffLocation())
                .distance(dto.getDistance())
                .duration(dto.getDuration())
                .active(dto.isActive())
                .build();
        AirportRoute saved = repository.save(r);
        return toDto(saved);
    }

    @Override
    public Page<AirportRouteDto> list(Pageable pageable) {
        Page<AirportRoute> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public AirportRouteDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public AirportRouteDto update(Long id, AirportRouteDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setAirportName(dto.getAirportName());
            r.setPickupLocation(dto.getPickupLocation());
            r.setDropoffLocation(dto.getDropoffLocation());
            r.setDistance(dto.getDistance());
            r.setDuration(dto.getDuration());
            r.setActive(dto.isActive());
            return toDto(repository.save(r));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AirportRouteDto toDto(AirportRoute r) {
        return AirportRouteDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .airportName(r.getAirportName())
                .pickupLocation(r.getPickupLocation())
                .dropoffLocation(r.getDropoffLocation())
                .distance(r.getDistance())
                .duration(r.getDuration())
                .active(r.isActive())
                .build();
    }
}

