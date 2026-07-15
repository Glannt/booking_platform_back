package com.dotnt.booking_travel_backend.rental.internal.service.impl;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRouteDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRoutePriceDto;
import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRouteTranslationDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRouteTranslation;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRouteRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRouteTranslationRepository;
import com.dotnt.booking_travel_backend.rental.internal.service.IIntercityRouteService;
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
public class IntercityRouteServiceImpl implements IIntercityRouteService {

    private final IntercityRouteRepository routeRepository;
    private final IntercityRouteTranslationRepository translationRepository;
    private final IntercityRoutePriceRepository priceRepository;

    @Override
    @Transactional
    public IntercityRouteDto create(IntercityRouteDto dto) {
        IntercityRoute route = IntercityRoute.builder()
                .originCity(dto.getOriginCity())
                .destinationCity(dto.getDestinationCity())
                .distanceKm(dto.getDistanceKm())
                .estimatedHours(dto.getEstimatedHours())
                .active(dto.isActive())
                .build();
        route = routeRepository.save(route);

        if (dto.getTranslations() != null) {
            for (var tr : dto.getTranslations()) {
                translationRepository.save(IntercityRouteTranslation.builder()
                        .route(route)
                        .locale(tr.getLocale())
                        .name(tr.getName())
                        .description(tr.getDescription())
                        .build());
            }
        }

        if (dto.getPrices() != null) {
            for (var pr : dto.getPrices()) {
                priceRepository.save(IntercityRoutePrice.builder()
                        .route(route)
                        .vehicleCategoryId(pr.getVehicleCategoryId())
                        .oneWayAmount(pr.getOneWayAmount())
                        .roundTripAmount(pr.getRoundTripAmount())
                        .currency(pr.getCurrency() != null ? pr.getCurrency() : "VND")
                        .active(pr.isActive())
                        .build());
            }
        }

        return get(route.getId());
    }

    @Override
    public Page<IntercityRouteDto> list(Pageable pageable) {
        Page<IntercityRoute> page = routeRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public List<IntercityRouteDto> listActive() {
        return routeRepository.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public IntercityRouteDto get(Long id) {
        return routeRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public IntercityRouteDto update(Long id, IntercityRouteDto dto) {
        return routeRepository.findById(id).map(route -> {
            route.setOriginCity(dto.getOriginCity());
            route.setDestinationCity(dto.getDestinationCity());
            route.setDistanceKm(dto.getDistanceKm());
            route.setEstimatedHours(dto.getEstimatedHours());
            route.setActive(dto.isActive());
            route = routeRepository.save(route);

            translationRepository.findByRouteId(id).forEach(translationRepository::delete);
            if (dto.getTranslations() != null) {
                for (var tr : dto.getTranslations()) {
                    translationRepository.save(IntercityRouteTranslation.builder()
                            .route(route)
                            .locale(tr.getLocale())
                            .name(tr.getName())
                            .description(tr.getDescription())
                            .build());
                }
            }

            priceRepository.findByRouteId(id).forEach(priceRepository::delete);
            if (dto.getPrices() != null) {
                for (var pr : dto.getPrices()) {
                    priceRepository.save(IntercityRoutePrice.builder()
                            .route(route)
                            .vehicleCategoryId(pr.getVehicleCategoryId())
                            .oneWayAmount(pr.getOneWayAmount())
                            .roundTripAmount(pr.getRoundTripAmount())
                            .currency(pr.getCurrency() != null ? pr.getCurrency() : "VND")
                            .active(pr.isActive())
                            .build());
                }
            }

            return toDto(route);
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        priceRepository.findByRouteId(id).forEach(priceRepository::delete);
        translationRepository.findByRouteId(id).forEach(translationRepository::delete);
        routeRepository.deleteById(id);
    }

    private IntercityRouteDto toDto(IntercityRoute route) {
        var translations = translationRepository.findByRouteId(route.getId()).stream()
                .map(tr -> IntercityRouteTranslationDto.builder()
                        .id(tr.getId()).routeId(route.getId()).locale(tr.getLocale())
                        .name(tr.getName()).description(tr.getDescription()).build())
                .collect(Collectors.toList());

        var prices = priceRepository.findByRouteId(route.getId()).stream()
                .map(pr -> IntercityRoutePriceDto.builder()
                        .id(pr.getId()).routeId(route.getId()).vehicleCategoryId(pr.getVehicleCategoryId())
                        .oneWayAmount(pr.getOneWayAmount()).roundTripAmount(pr.getRoundTripAmount())
                        .currency(pr.getCurrency()).active(pr.isActive()).build())
                .collect(Collectors.toList());

        return IntercityRouteDto.builder()
                .id(route.getId())
                .originCity(route.getOriginCity())
                .destinationCity(route.getDestinationCity())
                .distanceKm(route.getDistanceKm())
                .estimatedHours(route.getEstimatedHours())
                .active(route.isActive())
                .translations(translations)
                .prices(prices)
                .build();
    }
}
