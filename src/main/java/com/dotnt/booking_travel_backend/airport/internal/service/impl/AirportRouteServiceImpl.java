package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRouteRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirportRouteServiceImpl implements IAirportRouteService {
    private final AirportRouteRepository repository;
    private final AirportRepository airportRepository;

    @Override
    public AirportRouteDto create(AirportRouteDto dto) {
        AirportRoute r = AirportRoute.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .active(dto.isActive())
                .build();
        if (dto.getAirportId() != null) {
            r.setAirport(airportRepository.findById(dto.getAirportId()).orElse(null));
        }
        AirportRoute saved = repository.save(r);
        return toDto(saved);
    }

    @Override
    public Page<AirportRouteDto> list(Pageable pageable) {
        Page<AirportRoute> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public List<AirportRouteDto> listActiveByAirport(Long airportId) {
        return repository.findByAirportIdAndActiveTrue(airportId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AirportRouteDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public AirportRouteDto update(Long id, AirportRouteDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setName(dto.getName());
            if (dto.getAirportId() != null) {
                r.setAirport(airportRepository.findById(dto.getAirportId()).orElse(null));
            }
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
                .airportId(r.getAirport() != null ? r.getAirport().getId() : null)
                .name(r.getName())
                .active(r.isActive())
                .build();
    }
}

