package com.dotnt.booking_travel_backend.airport.internal.service.impl;

import com.dotnt.booking_travel_backend.airport.api.dtos.AirportDto;
import com.dotnt.booking_travel_backend.airport.api.dtos.AirportTranslationDto;
import com.dotnt.booking_travel_backend.airport.internal.entity.Airport;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTranslation;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTranslationRepository;
import com.dotnt.booking_travel_backend.airport.internal.service.IAirportService;
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
                .active(dto.isActive())
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
            repository.save(saved);
        }
        return toDto(saved);
    }

    @Override
    public Page<AirportDto> list(Pageable pageable) {
        Page<Airport> page = repository.findAll(pageable);
        return new PageImpl<>(
                page.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );
    }

    @Override
    public List<AirportDto> listActive() {
        return repository.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
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
            a.setActive(dto.isActive());

            if (dto.getTranslations() != null) {
                a.getTranslations().clear();
                repository.saveAndFlush(a);
                for (AirportTranslationDto tDto : dto.getTranslations()) {
                    AirportTranslation tr = AirportTranslation.builder()
                            .airport(a)
                            .locale(tDto.getLocale())
                            .name(tDto.getName())
                            .description(tDto.getDescription())
                            .build();
                    a.getTranslations().add(tr);
                }
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
                .active(a.isActive())
                .translations(translations)
                .build();
    }
}
