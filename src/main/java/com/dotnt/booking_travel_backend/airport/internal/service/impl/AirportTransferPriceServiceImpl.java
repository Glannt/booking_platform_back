package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTransferPriceDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTransferPriceRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportTransferPriceService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
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
public class AirportTransferPriceServiceImpl implements IAirportTransferPriceService {

    private final AirportTransferPriceRepository repository;

    @Override
    @Transactional
    public AirportTransferPriceDto create(AirportTransferPriceDto dto) {
        if (dto == null || dto.getAirportRouteId() == null || dto.getVehicleCategoryId() == null) {
            throw new IllegalArgumentException("Invalid input data");
        }
        return toDto(repository.save(toEntity(null, dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirportTransferPriceDto> list(Pageable pageable) {
        Page<AirportTransferPrice> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public AirportTransferPriceDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public AirportTransferPriceDto update(Long id, AirportTransferPriceDto dto) {
        return repository.findById(id)
                .map(existing -> repository.save(toEntity(existing.getId(), dto)))
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirportTransferPriceDto> listByRoute(Long routeId) {
        return repository.findByAirportRouteId(routeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AirportTransferPrice toEntity(Long id, AirportTransferPriceDto dto) {
        AirportTransferPrice entity = AirportTransferPrice.builder()
                .airportRouteId(dto.getAirportRouteId())
                .vehicleCategoryId(dto.getVehicleCategoryId())
                .baseKm(dto.getBaseKm())
                .baseAmount(dto.getBaseAmount())
                .perKmAmount(dto.getPerKmAmount())
                .zoneBufferKm(dto.getZoneBufferKm())
                .fixedOvercharge(dto.isFixedOvercharge())
                .feeOverchargeFixed(dto.getFeeOverchargeFixed())
                .currency(dto.getCurrency())
                .active(dto.isActive())
                .depositPolicy(DepositPolicies.from(dto.getDepositType(), dto.getDepositRate(), dto.getDepositAmount()))
                .build();
        entity.setId(id);
        return entity;
    }

    private AirportTransferPriceDto toDto(AirportTransferPrice p) {
        DepositPolicy dp = p.getDepositPolicy();
        return AirportTransferPriceDto.builder()
                .id(p.getId())
                .airportRouteId(p.getAirportRouteId())
                .vehicleCategoryId(p.getVehicleCategoryId())
                .baseKm(p.getBaseKm())
                .baseAmount(p.getBaseAmount())
                .perKmAmount(p.getPerKmAmount())
                .zoneBufferKm(p.getZoneBufferKm())
                .fixedOvercharge(p.isFixedOvercharge())
                .feeOverchargeFixed(p.getFeeOverchargeFixed())
                .currency(p.getCurrency())
                .active(p.isActive())
                .depositType(DepositPolicies.type(dp))
                .depositRate(DepositPolicies.rate(dp))
                .depositAmount(DepositPolicies.amount(dp))
                .build();
    }
}