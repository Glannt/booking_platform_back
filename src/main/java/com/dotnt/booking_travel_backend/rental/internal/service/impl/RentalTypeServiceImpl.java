package com.dotnt.booking_travel_backend.rental.internal.service.impl;

import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.RentalTypeTranslationDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalType;
import com.dotnt.booking_travel_backend.rental.internal.entity.RentalTypeTranslation;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalTypeRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalTypeTranslationRepository;
import com.dotnt.booking_travel_backend.rental.internal.service.IRentalTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalTypeServiceImpl implements IRentalTypeService {
    private final RentalTypeRepository repository;
    private final RentalTypeTranslationRepository translationRepository;

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public RentalTypeDto create(RentalTypeDto dto) {
        RentalType t = RentalType.builder().code(dto.getCode()).active(dto.isActive()).build();
        RentalType saved = repository.save(t);
        if (dto.getTranslations() != null) {
            var translations = dto.getTranslations().stream().map(tr -> RentalTypeTranslation.builder()
                    .rentalType(saved)
                    .locale(tr.getLocale())
                    .name(tr.getName())
                    .description(tr.getDescription())
                    .build()).collect(Collectors.toList());
            translationRepository.saveAll(translations);
        }
        return get(saved.getId());
    }

    @Override
    @Cacheable(value = "rentalTypes", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<RentalTypeDto> list(Pageable pageable) {
        Page<RentalType> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    @Cacheable(value = "rentalTypes", key = "#id")
    public RentalTypeDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public RentalTypeDto update(Long id, RentalTypeDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setActive(dto.isActive());
            RentalType saved = repository.save(r);
            // replace translations for simplicity
            translationRepository.findByRentalTypeId(saved.getId()).forEach(translationRepository::delete);
            if (dto.getTranslations() != null) {
                var translations = dto.getTranslations().stream().map(tr -> RentalTypeTranslation.builder()
                        .rentalType(saved)
                        .locale(tr.getLocale())
                        .name(tr.getName())
                        .description(tr.getDescription())
                        .build()).collect(Collectors.toList());
                translationRepository.saveAll(translations);
            }
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public void delete(Long id) {
        translationRepository.findByRentalTypeId(id).forEach(translationRepository::delete);
        repository.deleteById(id);
    }

    private RentalTypeDto toDto(RentalType r) {
        var translations = translationRepository.findByRentalTypeId(r.getId()).stream().map(tr -> RentalTypeTranslationDto.builder()
                .id(tr.getId()).rentalTypeId(tr.getRentalType().getId()).locale(tr.getLocale()).name(tr.getName()).description(tr.getDescription()).build()).collect(Collectors.toList());
        return RentalTypeDto.builder().id(r.getId()).code(r.getCode()).active(r.isActive()).translations(translations).build();
    }
}

