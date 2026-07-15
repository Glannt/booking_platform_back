package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourDayDto;
import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourDay;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourDayRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourDayServiceImpl implements ITourDayService {
    private final TourDayRepository tourDayRepository;
    private final TourRepository tourRepository;

    @Override
    public TourDayDto create(Long tourId, TourDayDto dto) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new IllegalArgumentException("Tour not found"));
        TourDay day = TourDay.builder().tour(tour).dayNumber(dto.getDayNumber()).build();
        TourDay saved = tourDayRepository.save(day);
        return toDto(saved);
    }

    @Override
    public Page<TourDayDto> list(Long tourId, Pageable pageable) {
        Page<TourDay> page = tourDayRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public TourDayDto get(Long id) {
        return tourDayRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public TourDayDto update(Long id, TourDayDto dto) {
        return tourDayRepository.findById(id).map(d -> {
            d.setDayNumber(dto.getDayNumber());
            return toDto(tourDayRepository.save(d));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        tourDayRepository.deleteById(id);
    }

    private TourDayDto toDto(TourDay d) {
        return TourDayDto.builder().id(d.getId()).tourId(d.getTour().getId()).dayNumber(d.getDayNumber()).build();
    }
}

