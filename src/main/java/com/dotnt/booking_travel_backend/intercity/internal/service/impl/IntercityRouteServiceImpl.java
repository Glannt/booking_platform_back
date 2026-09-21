package com.dotnt.booking_travel_backend.intercity.internal.service.impl;

import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityDepartureTimeDto;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityRouteDto;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityRoutePriceDto;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityRouteTranslationDto;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityDepartureTime;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRouteTranslation;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityDepartureTimeRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRouteRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRouteTranslationRepository;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityRouteService;
import com.dotnt.booking_travel_backend.shared.component.ComponentListConverter;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class IntercityRouteServiceImpl implements IIntercityRouteService {

    private final IntercityRouteRepository routeRepository;
    private final IntercityRouteTranslationRepository translationRepository;
    private final IntercityRoutePriceRepository priceRepository;
    private final IntercityDepartureTimeRepository departureTimeRepository;
    private final ModuleEventPublisher moduleEventPublisher;

    private String resolveThumbnailUrl(Long thumbnailId) {
        if (thumbnailId == null) return null;
        try {
            return moduleEventPublisher.publishRequest(
                    "media.image.resolveThumbnailUrl", thumbnailId, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "intercityRoutes", allEntries = true)
    public IntercityRouteDto create(IntercityRouteDto dto) {
        List<Long> imageIds = dto.getImageIds() != null ? new ArrayList<>(dto.getImageIds()) : new ArrayList<>();

        String slug = dto.getSlug() != null && !dto.getSlug().isBlank()
                ? slugify(dto.getSlug())
                : slugify(dto.getOriginCity() + " " + dto.getDestinationCity());

        IntercityRoute route = IntercityRoute.builder()
                .originCity(dto.getOriginCity())
                .destinationCity(dto.getDestinationCity())
                .slug(slug)
                .components(ComponentListConverter.fromJsonString(dto.getComponents()))
                .distanceKm(dto.getDistanceKm())
                .estimatedHours(dto.getEstimatedHours())
                .originLat(dto.getOriginLat())
                .originLng(dto.getOriginLng())
                .destinationLat(dto.getDestinationLat())
                .destinationLng(dto.getDestinationLng())
                .active(Boolean.TRUE.equals(dto.getActive()))
                .isContactOnly(dto.isContactOnly())
                .thumbnailId(dto.getThumbnailId())
                .thumbnailUrl(dto.getThumbnailUrl())
                .imageIds(imageIds)
                .build();
        route = routeRepository.save(route);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            for (var tr : dto.getTranslations()) {
                String trSlug = tr.getSlug() != null && !tr.getSlug().isBlank()
                        ? slugify(tr.getSlug())
                        : slug;
                translationRepository.save(IntercityRouteTranslation.builder()
                        .route(route)
                        .locale(tr.getLocale())
                        .name(tr.getName())
                        .slug(trSlug)
                        .description(tr.getDescription())
                        .build());
            }
        } else {
            String defaultName = dto.getOriginCity() + " - " + dto.getDestinationCity();
            translationRepository.save(IntercityRouteTranslation.builder()
                    .route(route)
                    .locale("vi")
                    .name(defaultName)
                    .slug(slug)
                    .description("Tuyến xe liên tỉnh " + defaultName)
                    .build());
        }

        if (dto.getPrices() != null) {
            for (var pr : dto.getPrices()) {
                priceRepository.save(IntercityRoutePrice.builder()
                        .route(route)
                        .vehicleCategoryId(pr.getVehicleCategoryId())
                        .perKmAmount(pr.getPerKmAmount())
                        .pricingType(pr.getPricingType() != null && !pr.getPricingType().isBlank() ? pr.getPricingType().toUpperCase() : "PER_KM")
                        .fixedPriceOneWay(pr.getFixedPriceOneWay())
                        .fixedPriceRoundTrip(pr.getFixedPriceRoundTrip())
                        .roundTripPrice(pr.getRoundTripPrice())
                        .roundTripPerKmAmount(pr.getRoundTripPerKmAmount())
                        .currency(pr.getCurrency() != null ? pr.getCurrency() : "VND")
                        .depositPolicy(DepositPolicies.from(pr.getDepositType(), pr.getDepositRate(), pr.getDepositAmount()))
                        .active(pr.isActive())
                        .build());
            }
        }

        if (dto.getDepartureTimes() != null) {
            for (var dt : dto.getDepartureTimes()) {
                departureTimeRepository.save(IntercityDepartureTime.builder()
                        .route(route)
                        .departureTime(dt.getDepartureTime())
                        .direction(dt.getDirection() != null ? dt.getDirection() : "OUTBOUND")
                        .orderIndex(dt.getOrderIndex())
                        .active(dt.isActive())
                        .build());
            }
        }

        return toDto(route);
    }

    @Override
    @Cacheable(value = "intercityRoutes", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<IntercityRouteDto> list(Pageable pageable) {
        Page<IntercityRoute> page = routeRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    @Cacheable(value = "intercityRoutes", key = "'active'")
    public List<IntercityRouteDto> listActive() {
        return routeRepository.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "intercityRoutes", key = "#id")
    public IntercityRouteDto get(Long id) {
        return routeRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "intercityRoutes", allEntries = true)
    public IntercityRouteDto update(Long id, IntercityRouteDto dto) {
        return routeRepository.findById(id).map(route -> {
            route.setOriginCity(dto.getOriginCity());
            route.setDestinationCity(dto.getDestinationCity());
            if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
                route.setSlug(slugify(dto.getSlug()));
            } else if (route.getSlug() == null || route.getSlug().isBlank()) {
                route.setSlug(slugify(dto.getOriginCity() + " " + dto.getDestinationCity()));
            }
            if (dto.getComponents() != null) {
                route.setComponents(ComponentListConverter.fromJsonString(dto.getComponents()));
            }
            route.setDistanceKm(dto.getDistanceKm());
            route.setEstimatedHours(dto.getEstimatedHours());
            route.setOriginLat(dto.getOriginLat());
            route.setOriginLng(dto.getOriginLng());
            route.setDestinationLat(dto.getDestinationLat());
            route.setDestinationLng(dto.getDestinationLng());
            route.setActive(dto.getActive() != null ? dto.getActive() : route.isActive());
            if (dto.getIsContactOnly() != null) {
                route.setContactOnly(dto.isContactOnly());
            }
            route.setThumbnailId(dto.getThumbnailId());
            route.setThumbnailUrl(dto.getThumbnailUrl());
            if (dto.getImageIds() != null) {
                route.setImageIds(new ArrayList<>(dto.getImageIds()));
            }

            // 1. Merge translations (key = locale)
            final IntercityRoute finalRoute = route;
            final String currentSlug = route.getSlug();
            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                EntityMergeUtils.mergeCollection(
                        route.getTranslations(),
                        dto.getTranslations(),
                        tr -> tr.getLocale() != null ? tr.getLocale().toLowerCase() : null,
                        trDto -> trDto.getLocale() != null ? trDto.getLocale().toLowerCase() : null,
                        (existing, trDto) -> {
                            existing.setName(trDto.getName());
                            String slug = trDto.getSlug() != null && !trDto.getSlug().isBlank()
                                    ? slugify(trDto.getSlug())
                                    : currentSlug;
                            existing.setSlug(slug);
                            existing.setDescription(trDto.getDescription());
                        },
                        trDto -> {
                            String slug = trDto.getSlug() != null && !trDto.getSlug().isBlank()
                                    ? slugify(trDto.getSlug())
                                    : currentSlug;
                            return IntercityRouteTranslation.builder()
                                    .route(finalRoute)
                                    .locale(trDto.getLocale().toLowerCase())
                                    .name(trDto.getName())
                                    .slug(slug)
                                    .description(trDto.getDescription())
                                    .build();
                        }
                );
            }

            // 2. Merge prices (key = vehicleCategoryId or id)
            if (dto.getPrices() != null) {
                EntityMergeUtils.mergeCollection(
                        route.getPrices(),
                        dto.getPrices(),
                        pr -> pr.getId() != null ? pr.getId() : pr.getVehicleCategoryId(),
                        prDto -> prDto.getId() != null ? prDto.getId() : prDto.getVehicleCategoryId(),
                        (existing, prDto) -> {
                            existing.setVehicleCategoryId(prDto.getVehicleCategoryId());
                            existing.setPerKmAmount(prDto.getPerKmAmount());
                            existing.setPricingType(prDto.getPricingType() != null && !prDto.getPricingType().isBlank()
                                    ? prDto.getPricingType().toUpperCase() : "PER_KM");
                            existing.setFixedPriceOneWay(prDto.getFixedPriceOneWay());
                            existing.setFixedPriceRoundTrip(prDto.getFixedPriceRoundTrip());
                            existing.setRoundTripPrice(prDto.getRoundTripPrice());
                            existing.setRoundTripPerKmAmount(prDto.getRoundTripPerKmAmount());
                            existing.setCurrency(prDto.getCurrency() != null ? prDto.getCurrency() : "VND");
                            existing.setDepositPolicy(DepositPolicies.from(prDto.getDepositType(), prDto.getDepositRate(), prDto.getDepositAmount()));
                            existing.setActive(prDto.isActive());
                        },
                        prDto -> IntercityRoutePrice.builder()
                                .route(finalRoute)
                                .vehicleCategoryId(prDto.getVehicleCategoryId())
                                .perKmAmount(prDto.getPerKmAmount())
                                .pricingType(prDto.getPricingType() != null && !prDto.getPricingType().isBlank()
                                        ? prDto.getPricingType().toUpperCase() : "PER_KM")
                                .fixedPriceOneWay(prDto.getFixedPriceOneWay())
                                .fixedPriceRoundTrip(prDto.getFixedPriceRoundTrip())
                                .roundTripPrice(prDto.getRoundTripPrice())
                                .roundTripPerKmAmount(prDto.getRoundTripPerKmAmount())
                                .currency(prDto.getCurrency() != null ? prDto.getCurrency() : "VND")
                                .depositPolicy(DepositPolicies.from(prDto.getDepositType(), prDto.getDepositRate(), prDto.getDepositAmount()))
                                .active(prDto.isActive())
                                .build()
                );
            }

            route = routeRepository.save(route);
            return get(route.getId());
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "intercityRoutes", allEntries = true)
    public void delete(Long id) {
        priceRepository.findByRouteId(id).forEach(priceRepository::delete);
        translationRepository.findByRouteId(id).forEach(translationRepository::delete);
        routeRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "intercityRoutes", key = "'slug_' + #slug")
    public IntercityRouteDto getBySlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return routeRepository.findBySlug(slug)
                .map(this::toDto)
                .orElseGet(() -> translationRepository.findBySlug(slug)
                        .map(tr -> get(tr.getRoute().getId()))
                        .orElseGet(() -> {
                            try {
                                Long id = Long.parseLong(slug);
                                return get(id);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }));
    }

    private String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "route-" + System.currentTimeMillis();
        }
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        String noDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase()
                .replace("đ", "d")
                .replace("Đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private IntercityRouteDto toDto(IntercityRoute route) {
        var translations = translationRepository.findByRouteId(route.getId()).stream()
                .map(tr -> IntercityRouteTranslationDto.builder()
                        .id(tr.getId()).routeId(route.getId()).locale(tr.getLocale())
                        .name(tr.getName()).slug(tr.getSlug()).description(tr.getDescription()).build())
                .collect(Collectors.toList());

        var prices = priceRepository.findByRouteId(route.getId()).stream()
                .map(pr -> IntercityRoutePriceDto.builder()
                        .id(pr.getId()).routeId(route.getId()).vehicleCategoryId(pr.getVehicleCategoryId())
                        .perKmAmount(pr.getPerKmAmount())
                        .pricingType(pr.getPricingType())
                        .fixedPriceOneWay(pr.getFixedPriceOneWay())
                        .fixedPriceRoundTrip(pr.getFixedPriceRoundTrip())
                        .roundTripPrice(pr.getRoundTripPrice())
                        .roundTripPerKmAmount(pr.getRoundTripPerKmAmount())
                        .depositType(DepositPolicies.type(pr.getDepositPolicy()))
                        .depositRate(DepositPolicies.rate(pr.getDepositPolicy()))
                        .depositAmount(DepositPolicies.amount(pr.getDepositPolicy()))
                        .currency(pr.getCurrency()).active(pr.isActive()).build())
                .collect(Collectors.toList());

        var departureTimes = departureTimeRepository.findByRouteIdOrderByOrderIndexAsc(route.getId()).stream()
                .map(dt -> IntercityDepartureTimeDto.builder()
                        .id(dt.getId()).routeId(route.getId()).departureTime(dt.getDepartureTime())
                        .direction(dt.getDirection() != null ? dt.getDirection() : "OUTBOUND")
                        .orderIndex(dt.getOrderIndex()).active(dt.isActive()).build())
                .collect(Collectors.toList());

        List<Long> imageIds = route.getImageIds() != null ? route.getImageIds() : new ArrayList<>();

        Long linkedSharedRouteId = null;
        try {
            linkedSharedRouteId = moduleEventPublisher.publishRequest(
                    "sharedroute.findByLinkedIntercityRouteId", route.getId(), Long.class);
        } catch (Exception e) {
            // ignore
        }
        boolean hasSharedRoute = linkedSharedRouteId != null;

        return IntercityRouteDto.builder()
                .id(route.getId())
                .originCity(route.getOriginCity())
                .destinationCity(route.getDestinationCity())
                .slug(route.getSlug())
                .components(ComponentListConverter.toJsonString(route.getComponents()))
                .distanceKm(route.getDistanceKm())
                .estimatedHours(route.getEstimatedHours())
                .originLat(route.getOriginLat())
                .originLng(route.getOriginLng())
                .destinationLat(route.getDestinationLat())
                .destinationLng(route.getDestinationLng())
                .active(route.isActive())
                .isContactOnly(route.isContactOnly())
                .thumbnailId(route.getThumbnailId())
                .thumbnailUrl(resolveThumbnailUrl(route.getThumbnailId()))
                .imageIds(imageIds)
                .translations(translations)
                .prices(prices)
                .departureTimes(departureTimes)
                .linkedSharedRouteId(linkedSharedRouteId)
                .hasSharedRoute(hasSharedRoute)
                .build();
    }
}
