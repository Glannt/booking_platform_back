package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportRouteDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRouteRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportRouteService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirportRouteServiceImpl implements IAirportRouteService {
    private final AirportRouteRepository repository;
    private final AirportRepository airportRepository;

    @Override
    @Transactional
    public AirportRouteDto create(AirportRouteDto dto) {
        AirportRoute r = AirportRoute.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .distanceKm(dto.getDistanceKm())
                .originName(dto.getOriginName())
                .destinationName(dto.getDestinationName())
                .originLat(dto.getOriginLat())
                .originLng(dto.getOriginLng())
                .destinationLat(dto.getDestinationLat())
                .destinationLng(dto.getDestinationLng())
                .active(dto.isActive())
                .depositPolicy(DepositPolicies.from(dto.getDepositType(), dto.getDepositRate(), dto.getDepositAmount()))
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
    @Transactional
    public AirportRouteDto update(Long id, AirportRouteDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setName(dto.getName());
            r.setDistanceKm(dto.getDistanceKm());
            r.setOriginName(dto.getOriginName());
            r.setDestinationName(dto.getDestinationName());
            r.setOriginLat(dto.getOriginLat());
            r.setOriginLng(dto.getOriginLng());
            r.setDestinationLat(dto.getDestinationLat());
            r.setDestinationLng(dto.getDestinationLng());
            if (dto.getAirportId() != null) {
                r.setAirport(airportRepository.findById(dto.getAirportId()).orElse(null));
            }
            r.setActive(dto.isActive());
            r.setDepositPolicy(DepositPolicies.merge(r.getDepositPolicy(), dto.getDepositType(), dto.getDepositRate(), dto.getDepositAmount()));
            return toDto(repository.save(r));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AirportRouteDto toDto(AirportRoute r) {
        String airportName = null;
        String airportCode = null;
        if (r.getAirport() != null) {
            airportName = r.getAirport().getName();
            airportCode = r.getAirport().getCode();
        }
        return AirportRouteDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .airportId(r.getAirport() != null ? r.getAirport().getId() : null)
                .airportName(airportName)
                .airportCode(airportCode)
                .name(r.getName())
                .distanceKm(r.getDistanceKm())
                .originName(r.getOriginName())
                .destinationName(r.getDestinationName())
                .originLat(r.getOriginLat())
                .originLng(r.getOriginLng())
                .destinationLat(r.getDestinationLat())
                .destinationLng(r.getDestinationLng())
                .active(r.isActive())
                // Raw accessors, not DepositPolicies.*: a route with no deposit of its own has to be
                // able to report null so the price row above it can take precedence (and so nothing
                // below it invents a 20% nobody configured).
                .depositType(r.getDepositPolicy() == null ? null : r.getDepositPolicy().getType())
                .depositRate(r.getDepositPolicy() == null ? null : r.getDepositPolicy().getRate())
                .depositAmount(r.getDepositPolicy() == null ? null : r.getDepositPolicy().getAmount())
                .build();
    }
}

