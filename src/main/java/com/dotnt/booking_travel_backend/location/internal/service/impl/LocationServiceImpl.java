package com.dotnt.booking_travel_backend.location.internal.service.impl;

import com.dotnt.booking_travel_backend.location.api.dtos.LocationDto;
import com.dotnt.booking_travel_backend.location.api.dtos.LocationSearchResultDto;
import com.dotnt.booking_travel_backend.location.api.dtos.LocationTranslationDto;
import com.dotnt.booking_travel_backend.location.internal.entity.Location;
import com.dotnt.booking_travel_backend.location.internal.entity.LocationTranslation;
import com.dotnt.booking_travel_backend.location.internal.repository.LocationRepository;
import com.dotnt.booking_travel_backend.location.internal.repository.LocationTranslationRepository;
import com.dotnt.booking_travel_backend.location.internal.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationTranslationRepository translationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocationSearchResultDto> search(String query, String locale, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<Location> locations = locationRepository.searchAnyLocale(query);

        return locations.stream()
                .limit(limit)
                .map(loc -> {
                    LocationTranslation tr = resolveTranslation(loc, locale);
                    if (tr == null) return null;
                    return LocationSearchResultDto.builder()
                            .id(loc.getId())
                            .latitude(loc.getLatitude())
                            .longitude(loc.getLongitude())
                            .category(loc.getCategory())
                            .name(tr.getName())
                            .address(tr.getAddress())
                            .build();
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDto> listActive(String locale) {
        return locationRepository.findByActiveTrue().stream()
                .map(loc -> toDto(loc, locale))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDto get(Long id, String locale) {
        return locationRepository.findById(id)
                .map(loc -> toDto(loc, locale))
                .orElse(null);
    }

    private LocationTranslation resolveTranslation(Location loc, String locale) {
        if (loc.getTranslations() == null || loc.getTranslations().isEmpty()) {
            return null;
        }

        // exact locale match
        return loc.getTranslations().stream()
                .filter(t -> t.getLocale().equals(locale))
                .findFirst()
                // fallback to en, then vi, then first available
                .orElseGet(() -> loc.getTranslations().stream()
                        .filter(t -> "en".equals(t.getLocale()))
                        .findFirst()
                        .orElseGet(() -> loc.getTranslations().stream()
                                .filter(t -> "vi".equals(t.getLocale()))
                                .findFirst()
                                .orElse(loc.getTranslations().get(0))));
    }

    private LocationDto toDto(Location loc, String locale) {
        LocationTranslation tr = resolveTranslation(loc, locale);
        List<LocationTranslationDto> translations = loc.getTranslations() != null
                ? loc.getTranslations().stream().map(t -> LocationTranslationDto.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .address(t.getAddress())
                        .description(t.getDescription())
                        .build()).collect(Collectors.toList())
                : List.of();

        return LocationDto.builder()
                .id(loc.getId())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .category(loc.getCategory())
                .active(loc.getActive())
                .name(tr != null ? tr.getName() : null)
                .address(tr != null ? tr.getAddress() : null)
                .description(tr != null ? tr.getDescription() : null)
                .translations(translations)
                .build();
    }
}
