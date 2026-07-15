package com.dotnt.booking_travel_backend.rental.internal.service.impl;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.Rental;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalTypeRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalTypeTranslationRepository;
import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeTranslationDto;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements IRentalService {
    private final RentalRepository rentalRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RentalTypeRepository rentalTypeRepository;
    private final RentalTypeTranslationRepository rentalTypeTranslationRepository;
    private final com.dotnt.booking_travel_backend.rental.internal.mapper.RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalDto create(RentalDto dto) {
        if (dto.getVehicleCategoryId() != null) {
            java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
            try {
                Class<?> cls = Class.forName("com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent");
                java.lang.reflect.Constructor<?> ctor = cls.getConstructor(Object.class, String.class, Object.class, java.util.concurrent.CompletableFuture.class);
                Object ev = ctor.newInstance(this, "vehicleCategory.exists", dto.getVehicleCategoryId(), future);
                applicationEventPublisher.publishEvent(ev);
                Boolean exists = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                if (exists == null || !exists) throw new IllegalArgumentException("Vehicle category not found");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("ModuleRequestEvent class not found", e);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Vehicle category validation failed", ex);
            }
        }
        Rental r = Rental.builder()
                .rentalTypeId(dto.getRentalTypeId())
                .driverRequired(dto.isDriverRequired())
                .deposit(dto.getDeposit())
                .minimumDays(dto.getMinimumDays())
                .active(dto.isActive())
                .vehicleCategoryId(dto.getVehicleCategoryId())
                .build();
        Rental saved = rentalRepository.save(r);
        return toDto(saved);
    }

    @Override
    public Page<RentalDto> list(Pageable pageable) {
        Page<Rental> page = rentalRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public RentalDto get(Long id) {
        return rentalRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public RentalDto update(Long id, RentalDto dto) {
        return rentalRepository.findById(id).map(r -> {
            if (dto.getVehicleCategoryId() != null) {
                java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
                try {
                    Class<?> cls = Class.forName("com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent");
                    java.lang.reflect.Constructor<?> ctor = cls.getConstructor(Object.class, String.class, Object.class, java.util.concurrent.CompletableFuture.class);
                    Object ev = ctor.newInstance(this, "vehicleCategory.exists", dto.getVehicleCategoryId(), future);
                    applicationEventPublisher.publishEvent(ev);
                    Boolean exists = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (exists == null || !exists) throw new IllegalArgumentException("Vehicle category not found");
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("ModuleRequestEvent class not found", e);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Vehicle category validation failed", ex);
                }
            }
            r.setRentalTypeId(dto.getRentalTypeId());
            r.setDriverRequired(dto.isDriverRequired());
            r.setDeposit(dto.getDeposit());
            r.setMinimumDays(dto.getMinimumDays());
            r.setActive(dto.isActive());
            r.setVehicleCategoryId(dto.getVehicleCategoryId());
            return toDto(rentalRepository.save(r));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        rentalRepository.deleteById(id);
    }

    private RentalDto toDto(Rental r) {
        RentalDto.RentalDtoBuilder builder = RentalDto.builder()
                .id(r.getId())
                .rentalTypeId(r.getRentalTypeId())
                .vehicleCategoryId(r.getVehicleCategoryId())
                .driverRequired(r.isDriverRequired())
                .deposit(r.getDeposit())
                .minimumDays(r.getMinimumDays())
                .active(r.isActive());
        if (r.getRentalTypeId() != null) {
            rentalTypeRepository.findById(r.getRentalTypeId()).ifPresent(rt -> {
                var translations = rentalTypeTranslationRepository.findByRentalTypeId(rt.getId()).stream().map(rentalMapper::toDto).map(t -> (RentalTypeTranslationDto) t).collect(Collectors.toList());
                RentalTypeDto rtDto = rentalMapper.toDto(rt);
                rtDto.setTranslations(translations);
                builder.rentalType(rtDto);
            });
        }
        return builder.build();
    }
}


