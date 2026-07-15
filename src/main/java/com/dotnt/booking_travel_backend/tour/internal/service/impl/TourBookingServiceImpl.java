package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourBookingDetail;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TourBookingServiceImpl implements TourBookingService {

    private final TourBookingDetailRepository tourBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository tourRepository;

    @Override
    @Transactional
    public Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime
    ) {
        TourBookingDetail detail = TourBookingDetail.builder()
                .bookingItemId(bookingItemId)
                .meetingPoint(pickupLocation != null ? pickupLocation : "")
                .guestCount(guestCount)
                .note(note)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        detail = tourBookingDetailRepository.save(detail);
        return detail.getId();
    }

    @Override
    public boolean isTour(Long serviceId) {
        return tourRepository.existsById(serviceId);
    }

    @Override
    @Transactional
    public void updateBookingItemId(Long tourBookingDetailId, Long bookingItemId) {
        tourBookingDetailRepository.findById(tourBookingDetailId).ifPresent(detail -> {
            detail.setBookingItemId(bookingItemId);
            tourBookingDetailRepository.save(detail);
        });
    }
}
