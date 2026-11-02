package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourActivityDto;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivity;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourDay;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourDayActivityRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourDayRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourActivityService;
import lombok.RequiredArgsConstructor;
import com.dotnt.booking_travel_backend.tour.internal.mapper.TourMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourActivityServiceImpl implements ITourActivityService {
    private final TourDayActivityRepository activityRepository;
    private final TourDayRepository dayRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional
    public TourActivityDto create(Long tourDayId, TourActivityDto dto) {
        TourDay day = dayRepository.findById(tourDayId).orElseThrow(() -> new IllegalArgumentException("TourDay not found"));
        TourDayActivity a = TourDayActivity.builder()
                .tourDay(day)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .build();
        return toDto(activityRepository.save(a));
    }

    @Override
    public Page<TourActivityDto> list(Long tourDayId, Pageable pageable) {
        Page<TourDayActivity> page = activityRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public TourActivityDto get(Long id) {
        return activityRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public TourActivityDto update(Long id, TourActivityDto dto) {
        return activityRepository.findById(id).map(a -> {
            a.setStartTime(dto.getStartTime());
            a.setEndTime(dto.getEndTime());
            a.setLocation(dto.getLocation());
            return toDto(activityRepository.save(a));
        }).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        activityRepository.deleteById(id);
    }

    private TourActivityDto toDto(TourDayActivity a) {
        return tourMapper.toDto(a);
    }
}

