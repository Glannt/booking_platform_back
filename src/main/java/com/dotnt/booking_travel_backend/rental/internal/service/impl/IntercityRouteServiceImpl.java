package com.dotnt.booking_travel_backend.rental.internal.service.impl;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityDepartureTimeDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRouteDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRoutePriceDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRouteTranslationDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRouteTranslation;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityDepartureTimeRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRouteRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRouteTranslationRepository;
import com.dotnt.booking_travel_backend.rental.internal.service.IIntercityRouteService;
import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
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
    private final ImageRepository imageRepository;

    private String resolveThumbnailUrl(Long thumbnailId) {
        if (thumbnailId == null) return null;
        return imageRepository.findById(thumbnailId)
                .map(Image::getFilePath)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "intercityRoutes", allEntries = true)
    public IntercityRouteDto create(IntercityRouteDto dto) {
        List<Image> routeImages = new ArrayList<>();
        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            routeImages = imageRepository.findAllById(dto.getImageIds());
        }

        IntercityRoute route = IntercityRoute.builder()
                .originCity(dto.getOriginCity())
                .destinationCity(dto.getDestinationCity())
                .distanceKm(dto.getDistanceKm())
                .estimatedHours(dto.getEstimatedHours())
                .originLat(dto.getOriginLat())
                .originLng(dto.getOriginLng())
                .destinationLat(dto.getDestinationLat())
                .destinationLng(dto.getDestinationLng())
                .active(Boolean.TRUE.equals(dto.getActive()))
                .hasDepartureTimes(Boolean.TRUE.equals(dto.getHasDepartureTimes()))
                .thumbnailId(dto.getThumbnailId())
                .thumbnailUrl(dto.getThumbnailUrl())
                .images(routeImages)
                .build();
        route = routeRepository.save(route);

        if (dto.getTranslations() != null) {
            for (var tr : dto.getTranslations()) {
                String slug = tr.getSlug() != null && !tr.getSlug().isBlank()
                        ? tr.getSlug()
                        : slugify(tr.getName());
                translationRepository.save(IntercityRouteTranslation.builder()
                        .route(route)
                        .locale(tr.getLocale())
                        .name(tr.getName())
                        .slug(slug)
                        .description(tr.getDescription())
                        .build());
            }
        }

        if (dto.getPrices() != null) {
            for (var pr : dto.getPrices()) {
                priceRepository.save(IntercityRoutePrice.builder()
                        .route(route)
                        .vehicleCategoryId(pr.getVehicleCategoryId())
                        .perKmAmount(pr.getPerKmAmount())
                        .currency(pr.getCurrency() != null ? pr.getCurrency() : "VND")
                        .active(pr.isActive())
                        .build());
            }
        }

        return get(route.getId());
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
            route.setDistanceKm(dto.getDistanceKm());
            route.setEstimatedHours(dto.getEstimatedHours());
            route.setOriginLat(dto.getOriginLat());
            route.setOriginLng(dto.getOriginLng());
            route.setDestinationLat(dto.getDestinationLat());
            route.setDestinationLng(dto.getDestinationLng());
            route.setActive(dto.getActive() != null ? dto.getActive() : route.isActive());
            route.setHasDepartureTimes(dto.getHasDepartureTimes() != null ? dto.getHasDepartureTimes() : route.isHasDepartureTimes());
            route.setThumbnailId(dto.getThumbnailId());
            route.setThumbnailUrl(dto.getThumbnailUrl());
            if (dto.getImageIds() != null) {
                route.setImages(imageRepository.findAllById(dto.getImageIds()));
            }
            route = routeRepository.save(route);

            translationRepository.findByRouteId(id).forEach(translationRepository::delete);
            translationRepository.flush();
            if (dto.getTranslations() != null) {
                for (var tr : dto.getTranslations()) {
                    String slug = tr.getSlug() != null && !tr.getSlug().isBlank()
                            ? tr.getSlug()
                            : slugify(tr.getName());
                    translationRepository.save(IntercityRouteTranslation.builder()
                            .route(route)
                            .locale(tr.getLocale())
                            .name(tr.getName())
                            .slug(slug)
                            .description(tr.getDescription())
                            .build());
                }
            }

            priceRepository.findByRouteId(id).forEach(priceRepository::delete);
            priceRepository.flush();
            if (dto.getPrices() != null) {
                for (var pr : dto.getPrices()) {
                    priceRepository.save(IntercityRoutePrice.builder()
                            .route(route)
                            .vehicleCategoryId(pr.getVehicleCategoryId())
                            .perKmAmount(pr.getPerKmAmount())
                            .currency(pr.getCurrency() != null ? pr.getCurrency() : "VND")
                            .active(pr.isActive())
                            .build());
                }
            }

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
        return translationRepository.findBySlug(slug)
                .map(tr -> get(tr.getRoute().getId()))
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(slug);
                        return get(id);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
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
                        .currency(pr.getCurrency()).active(pr.isActive()).build())
                .collect(Collectors.toList());

        var departureTimes = departureTimeRepository.findByRouteIdOrderByOrderIndexAsc(route.getId()).stream()
                .map(dt -> IntercityDepartureTimeDto.builder()
                        .id(dt.getId()).routeId(route.getId()).departureTime(dt.getDepartureTime())
                        .orderIndex(dt.getOrderIndex()).active(dt.isActive()).build())
                .collect(Collectors.toList());

        List<Long> imageIds = route.getImages() != null 
                ? route.getImages().stream().map(Image::getId).collect(Collectors.toList()) 
                : new ArrayList<>();

        return IntercityRouteDto.builder()
                .id(route.getId())
                .originCity(route.getOriginCity())
                .destinationCity(route.getDestinationCity())
                .distanceKm(route.getDistanceKm())
                .estimatedHours(route.getEstimatedHours())
                .originLat(route.getOriginLat())
                .originLng(route.getOriginLng())
                .destinationLat(route.getDestinationLat())
                .destinationLng(route.getDestinationLng())
                .active(route.isActive())
                .hasDepartureTimes(route.isHasDepartureTimes())
                .thumbnailId(route.getThumbnailId())
                .thumbnailUrl(resolveThumbnailUrl(route.getThumbnailId()))
                .imageIds(imageIds)
                .translations(translations)
                .prices(prices)
                .departureTimes(departureTimes)
                .build();
    }
}
