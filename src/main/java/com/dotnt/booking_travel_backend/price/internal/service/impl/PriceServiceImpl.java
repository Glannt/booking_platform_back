package com.dotnt.booking_travel_backend.price.internal.service.impl;

import com.dotnt.booking_travel_backend.price.api.dtos.PriceDto;
import com.dotnt.booking_travel_backend.price.internal.entity.Price;
import com.dotnt.booking_travel_backend.price.internal.repository.PriceRepository;
import com.dotnt.booking_travel_backend.price.internal.service.IPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements IPriceService {
    private final PriceRepository repository;

    @Override
    @Transactional
    public PriceDto create(PriceDto dto) {
        Price p = Price.builder()
                .businessType(dto.getBusinessType())
                .businessId(dto.getBusinessId())
                .vehicleCategoryId(dto.getVehicleCategoryId())
                .pricingUnit(dto.getPricingUnit())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .active(dto.isActive())
                .build();
        Price saved = repository.save(p);
        return toDto(saved);
    }

    @Override
    public Page<PriceDto> list(Pageable pageable, String businessType, Long businessId) {
        Page<Price> page;
        if (businessType != null && businessId != null) {
            page = new PageImpl<>(repository.findByBusinessTypeAndBusinessId(businessType, businessId), pageable, 0);
        } else {
            page = repository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public PriceDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public PriceDto update(Long id, PriceDto dto) {
        return repository.findById(id).map(p -> {
            p.setBusinessType(dto.getBusinessType());
            p.setBusinessId(dto.getBusinessId());
            p.setVehicleCategoryId(dto.getVehicleCategoryId());
            p.setPricingUnit(dto.getPricingUnit());
            p.setAmount(dto.getAmount());
            p.setCurrency(dto.getCurrency());
            p.setEffectiveFrom(dto.getEffectiveFrom());
            p.setEffectiveTo(dto.getEffectiveTo());
            p.setActive(dto.isActive());
            return toDto(repository.save(p));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private PriceDto toDto(Price p) {
        return PriceDto.builder()
                .id(p.getId())
                .businessType(p.getBusinessType())
                .businessId(p.getBusinessId())
                .vehicleCategoryId(p.getVehicleCategoryId())
                .pricingUnit(p.getPricingUnit())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .effectiveFrom(p.getEffectiveFrom())
                .effectiveTo(p.getEffectiveTo())
                .active(p.isActive())
                .build();
    }
}

