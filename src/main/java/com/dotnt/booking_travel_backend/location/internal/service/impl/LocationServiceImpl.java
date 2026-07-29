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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationTranslationRepository translationRepository;
    private final NominatimService nominatimService;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public List<LocationSearchResultDto> search(String query, String locale, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = query.toLowerCase().trim();
        String cacheKey = normalizedQuery + ":" + limit;

        // Step 1: Check Caffeine cache
        var cache = cacheManager.getCache("location_search");
        if (cache != null) {
            List<LocationSearchResultDto> cached = cache.get(cacheKey, List.class);
            if (cached != null) {
                log.debug("Cache HIT for search '{}'", query);
                return cached;
            }
        }

        // Step 2: Check DB
        List<Location> dbLocations = locationRepository.searchAnyLocale(query);
        log.info("Search '{}' → DB returned {} locations", query, dbLocations.size());
        if (!dbLocations.isEmpty()) {
            List<LocationSearchResultDto> results = dbLocations.stream()
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

            if (!results.isEmpty()) {
                if (cache != null) {
                    cache.put(cacheKey, results);
                }
                log.debug("DB HIT for search '{}': {} results", query, results.size());
                return results;
            }
        }

        // Step 3: DB miss — call Nominatim API
        log.info("Search '{}' → calling Nominatim API", query);
        List<NominatimService.NominatimResult> nominatimResults = nominatimService.search(query, limit);

        if (nominatimResults.isEmpty()) {
            return List.of();
        }

        // Save Nominatim results to DB
        List<LocationSearchResultDto> results = new ArrayList<>();
        for (NominatimService.NominatimResult mr : nominatimResults) {
            try {
                Location saved = saveNominatimResult(mr);
                LocationTranslation tr = resolveTranslation(saved, locale);
                if (tr != null) {
                    results.add(LocationSearchResultDto.builder()
                            .id(saved.getId())
                            .latitude(saved.getLatitude())
                            .longitude(saved.getLongitude())
                            .category(saved.getCategory())
                            .name(tr.getName())
                            .address(tr.getAddress())
                            .build());
                }
            } catch (Exception e) {
                log.warn("Failed to save Nominatim result '{}': {}", mr.getName(), e.getMessage());
            }
        }

        // Cache the results
        if (!results.isEmpty() && cache != null) {
            cache.put(cacheKey, results);
        }

        log.debug("Nominatim search '{}': saved {} results to DB", query, results.size());
        return results;
    }

    private Location saveNominatimResult(NominatimService.NominatimResult mr) {
        Location loc = Location.builder()
                .latitude(mr.getLatitude())
                .longitude(mr.getLongitude())
                .category(mr.getCategory())
                .active(true)
                .translations(new ArrayList<>())
                .build();

        loc.getTranslations().add(LocationTranslation.builder()
                .location(loc).locale("vi")
                .name(mr.getName())
                .address(mr.getAddress())
                .build());

        loc.getTranslations().add(LocationTranslation.builder()
                .location(loc).locale("en")
                .name(mr.getName())
                .address(mr.getAddress())
                .build());

        return locationRepository.save(loc);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "locations", key = "#locale")
    public List<LocationDto> listActive(String locale) {
        return locationRepository.findByActiveTrue().stream()
                .map(loc -> toDto(loc, locale))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "locations", key = "#id + '_' + #locale")
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
