package com.dotnt.booking_travel_backend.selfdrive.internal.service.impl;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRental;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveRentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeTranslationRepository;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeTranslationDto;

@Service
@RequiredArgsConstructor
public class SelfDriveRentalServiceImpl implements ISelfDriveRentalService {
    private final SelfDriveRentalRepository rentalRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SelfDriveTypeRepository rentalTypeRepository;
    private final SelfDriveTypeTranslationRepository rentalTypeTranslationRepository;
    private final com.dotnt.booking_travel_backend.selfdrive.internal.mapper.SelfDriveMapper selfDriveMapper;

    @Override
    @Transactional
    @CacheEvict(value = "rentals", allEntries = true)
    public SelfDriveRentalDto create(SelfDriveRentalDto dto) {
        if (dto.getVehicleCategoryId() != null) {
            java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
            try {
                Class<?> cls = Class.forName("com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent");
                java.lang.reflect.Constructor<?> ctor = cls.getConstructor(Object.class, String.class, Object.class, java.util.concurrent.CompletableFuture.class);
                Object ev = ctor.newInstance(this, "fleet.vehicleCategory.exists", dto.getVehicleCategoryId(), future);
                applicationEventPublisher.publishEvent(ev);
                Boolean exists = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                if (exists == null || !exists) throw new IllegalArgumentException("Vehicle category not found for id: " + dto.getVehicleCategoryId());
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("ModuleRequestEvent class not found", e);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Vehicle category validation failed: " + ex.getMessage(), ex);
            }
        }
        if (dto.isActive() && dto.getVehicleCategoryId() != null) {
            java.util.List<SelfDriveRental> existingActive = rentalRepository.findByVehicleCategoryIdAndActiveTrue(dto.getVehicleCategoryId());
            for (SelfDriveRental ex : existingActive) {
                ex.setActive(false);
                rentalRepository.save(ex);
            }
        }

        SelfDriveRental r = SelfDriveRental.builder()
                .rentalTypeId(dto.getRentalTypeId())
                .driverRequired(dto.isDriverRequired())
                .deposit(dto.getDeposit())
                .depositType(dto.getDepositType())
                .depositRate(dto.getDepositRate())
                .minimumDays(dto.getMinimumDays())
                .active(dto.isActive())
                .isContactOnly(dto.isContactOnly())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .vehicleCategoryId(dto.getVehicleCategoryId())
                .build();
        SelfDriveRental saved = rentalRepository.save(r);
        return toDto(saved);
    }

    @Override
    @Cacheable(value = "rentals", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<SelfDriveRentalDto> list(Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            effectivePageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("sortOrder").ascending().and(org.springframework.data.domain.Sort.by("id").ascending())
            );
        }
        Page<SelfDriveRental> page = rentalRepository.findAll(effectivePageable);
        return page.map(this::toDto);
    }

    @Override
    @Cacheable(value = "rentals", key = "#id")
    public SelfDriveRentalDto get(Long id) {
        return rentalRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentals", allEntries = true)
    public SelfDriveRentalDto update(Long id, SelfDriveRentalDto dto) {
        return rentalRepository.findById(id).map(r -> {
            if (dto.getVehicleCategoryId() != null) {
                java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
                try {
                    Class<?> cls = Class.forName("com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent");
                    java.lang.reflect.Constructor<?> ctor = cls.getConstructor(Object.class, String.class, Object.class, java.util.concurrent.CompletableFuture.class);
                    Object ev = ctor.newInstance(this, "fleet.vehicleCategory.exists", dto.getVehicleCategoryId(), future);
                    applicationEventPublisher.publishEvent(ev);
                    Boolean exists = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (exists == null || !exists) throw new IllegalArgumentException("Vehicle category not found for id: " + dto.getVehicleCategoryId());
                } catch (IllegalArgumentException ex) {
                    throw ex;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("ModuleRequestEvent class not found", e);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Vehicle category validation failed: " + ex.getMessage(), ex);
                }
            }

            if (dto.isActive() && dto.getVehicleCategoryId() != null) {
                java.util.List<SelfDriveRental> existingActive = rentalRepository.findByVehicleCategoryIdAndActiveTrue(dto.getVehicleCategoryId());
                for (SelfDriveRental ex : existingActive) {
                    if (!ex.getId().equals(id)) {
                        ex.setActive(false);
                        rentalRepository.save(ex);
                    }
                }
            }

            r.setRentalTypeId(dto.getRentalTypeId());
            r.setDriverRequired(dto.isDriverRequired());
            r.setDeposit(dto.getDeposit());
            r.setDepositType(dto.getDepositType());
            r.setDepositRate(dto.getDepositRate());
            r.setMinimumDays(dto.getMinimumDays());
            r.setActive(dto.isActive());
            if (dto.getIsContactOnly() != null) {
                r.setContactOnly(dto.isContactOnly());
            }
            if (dto.getSortOrder() != null) {
                r.setSortOrder(dto.getSortOrder());
            }
            r.setVehicleCategoryId(dto.getVehicleCategoryId());
            return toDto(rentalRepository.save(r));
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentals", allEntries = true)
    public void delete(Long id) {
        rentalRepository.deleteById(id);
    }

    private SelfDriveRentalDto toDto(SelfDriveRental r) {
        SelfDriveRentalDto.SelfDriveRentalDtoBuilder builder = SelfDriveRentalDto.builder()
                .id(r.getId())
                .rentalTypeId(r.getRentalTypeId())
                .vehicleCategoryId(r.getVehicleCategoryId())
                .driverRequired(r.isDriverRequired())
                .deposit(r.getDeposit())
                .depositType(r.getDepositType())
                .depositRate(r.getDepositRate())
                .minimumDays(r.getMinimumDays())
                .active(r.isActive())
                .isContactOnly(r.isContactOnly())
                .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0);
        if (r.getRentalTypeId() != null) {
            rentalTypeRepository.findById(r.getRentalTypeId()).ifPresent(rt -> {
                var translations = rentalTypeTranslationRepository.findByRentalTypeId(rt.getId()).stream().map(selfDriveMapper::toDto).map(t -> (SelfDriveTypeTranslationDto) t).collect(Collectors.toList());
                SelfDriveTypeDto rtDto = selfDriveMapper.toDto(rt);
                rtDto.setTranslations(translations);
                builder.rentalType(rtDto);
            });
        }
        return builder.build();
    }
}


