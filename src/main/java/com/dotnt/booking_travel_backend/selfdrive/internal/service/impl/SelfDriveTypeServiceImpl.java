package com.dotnt.booking_travel_backend.selfdrive.internal.service.impl;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeTranslationDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveType;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveTypeTranslation;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeTranslationRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDriveTypeService;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelfDriveTypeServiceImpl implements ISelfDriveTypeService {
    private final SelfDriveTypeRepository repository;
    private final SelfDriveTypeTranslationRepository translationRepository;

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public SelfDriveTypeDto create(SelfDriveTypeDto dto) {
        SelfDriveType entity = SelfDriveType.builder()
                .code(dto.getCode())
                .active(dto.isActive())
                .build();
        SelfDriveType saved = repository.save(entity);
        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            List<SelfDriveTypeTranslation> translations = dto.getTranslations().stream().map(tr -> SelfDriveTypeTranslation.builder()
                    .selfDriveType(saved)
                    .locale(tr.getLocale())
                    .name(tr.getName())
                    .description(tr.getDescription())
                    .build()).collect(Collectors.toList());
            translationRepository.saveAll(translations);
        }
        return toDto(saved);
    }

    @Override
    public Page<SelfDriveTypeDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Cacheable(value = "rentalTypes", key = "#id")
    public SelfDriveTypeDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public SelfDriveTypeDto update(Long id, SelfDriveTypeDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setActive(dto.isActive());

            if (dto.getTranslations() != null) {
                final SelfDriveType finalR = r;
                EntityMergeUtils.mergeCollection(
                        r.getTranslations(),
                        dto.getTranslations(),
                        tr -> tr.getLocale() != null ? tr.getLocale().toLowerCase() : null,
                        trDto -> trDto.getLocale() != null ? trDto.getLocale().toLowerCase() : null,
                        (existing, trDto) -> {
                            existing.setName(trDto.getName());
                            existing.setDescription(trDto.getDescription());
                        },
                        trDto -> SelfDriveTypeTranslation.builder()
                                .selfDriveType(finalR)
                                .locale(trDto.getLocale().toLowerCase())
                                .name(trDto.getName())
                                .description(trDto.getDescription())
                                .build()
                );
            }

            SelfDriveType saved = repository.save(r);
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "rentalTypes", allEntries = true)
    public void delete(Long id) {
        translationRepository.findBySelfDriveTypeId(id).forEach(translationRepository::delete);
        repository.deleteById(id);
    }

    private SelfDriveTypeDto toDto(SelfDriveType r) {
        List<SelfDriveTypeTranslationDto> translations = translationRepository.findBySelfDriveTypeId(r.getId()).stream().map(tr -> SelfDriveTypeTranslationDto.builder()
                .id(tr.getId())
                .rentalTypeId(tr.getSelfDriveType() != null ? tr.getSelfDriveType().getId() : null)
                .locale(tr.getLocale())
                .name(tr.getName())
                .description(tr.getDescription())
                .build()).collect(Collectors.toList());
        return SelfDriveTypeDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .active(r.isActive())
                .translations(translations)
                .build();
    }
}
