package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportDto;
import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTranslationDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.Airport;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTranslation;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTranslationRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportService;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirportServiceImpl implements IAirportService {
    private final AirportRepository repository;
    private final AirportTranslationRepository translationRepository;

    @Override
    @Transactional
    public AirportDto create(AirportDto dto) {
        Airport a = Airport.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .location(dto.getLocation())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .active(dto.isActive())
                .isDefault(dto.isDefault())
                .translations(new ArrayList<>())
                .build();
        Airport saved = repository.save(a);

        if (dto.getTranslations() != null) {
            for (AirportTranslationDto tDto : dto.getTranslations()) {
                AirportTranslation tr = AirportTranslation.builder()
                        .airport(saved)
                        .locale(tDto.getLocale())
                        .name(tDto.getName())
                        .description(tDto.getDescription())
                        .build();
                saved.getTranslations().add(tr);
            }
            saved = repository.save(saved);
        }
        return toDto(saved);
    }

    @Override
    public Page<AirportDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    @Override
    public List<AirportDto> listActive() {
        return repository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AirportDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public AirportDto update(Long id, AirportDto dto) {
        return repository.findById(id).map(a -> {
            a.setCode(dto.getCode());
            a.setName(dto.getName());
            a.setLocation(dto.getLocation());
            a.setLatitude(dto.getLatitude());
            a.setLongitude(dto.getLongitude());
            a.setActive(dto.isActive());
            a.setDefault(dto.isDefault());

            if (dto.getTranslations() != null) {
                final Airport finalAirport = a;
                EntityMergeUtils.mergeCollection(
                        a.getTranslations(),
                        dto.getTranslations(),
                        tr -> tr.getLocale() != null ? tr.getLocale().toLowerCase() : null,
                        trDto -> trDto.getLocale() != null ? trDto.getLocale().toLowerCase() : null,
                        (existing, trDto) -> {
                            existing.setName(trDto.getName());
                            existing.setDescription(trDto.getDescription());
                        },
                        trDto -> AirportTranslation.builder()
                                .airport(finalAirport)
                                .locale(trDto.getLocale().toLowerCase())
                                .name(trDto.getName())
                                .description(trDto.getDescription())
                                .build()
                );
            }
            return toDto(repository.save(a));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AirportDto toDto(Airport a) {
        List<AirportTranslationDto> translations = a.getTranslations() != null
                ? a.getTranslations().stream().map(t -> AirportTranslationDto.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : List.of();
        return AirportDto.builder()
                .id(a.getId())
                .code(a.getCode())
                .name(a.getName())
                .location(a.getLocation())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .active(a.isActive())
                .isDefault(a.isDefault())
                .translations(translations)
                .build();
    }
}
