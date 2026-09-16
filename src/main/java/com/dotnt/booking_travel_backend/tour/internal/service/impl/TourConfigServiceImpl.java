package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourConfigDto;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourConfig;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourConfigRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.TourConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TourConfigServiceImpl implements TourConfigService {

    private final TourConfigRepository tourConfigRepository;

    @Override
    @Cacheable(value = "tourConfigs", key = "'default'")
    public TourConfigDto getConfig() {
        TourConfig config = tourConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> tourConfigRepository.save(TourConfig.builder()
                        .recommendedLimit(6)
                        .autoRecommendEnabled(true)
                        .build()));

        return toDto(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourConfigs", allEntries = true)
    public TourConfigDto updateConfig(TourConfigDto dto) {
        TourConfig config = tourConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> TourConfig.builder().build());

        if (dto.getRecommendedLimit() > 0) {
            config.setRecommendedLimit(dto.getRecommendedLimit());
        }
        config.setAutoRecommendEnabled(dto.isAutoRecommendEnabled());

        TourConfig saved = tourConfigRepository.save(config);
        return toDto(saved);
    }

    private TourConfigDto toDto(TourConfig entity) {
        return TourConfigDto.builder()
                .id(entity.getId())
                .recommendedLimit(entity.getRecommendedLimit())
                .autoRecommendEnabled(entity.isAutoRecommendEnabled())
                .build();
    }
}
