package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourBookingDetail;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourTranslation;
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
    @Transactional(readOnly = true)
    public String getTourName(Long tourId) {
        if (tourId == null) return "Tour du lịch";
        return tourRepository.findById(tourId)
                .map(tour -> {
                    if (tour.getTranslations() != null) {
                        return tour.getTranslations().stream()
                                .filter(t -> "vi".equalsIgnoreCase(t.getLocale()))
                                .map(TourTranslation::getName)
                                .findFirst()
                                .orElseGet(() -> tour.getTranslations().stream()
                                        .map(TourTranslation::getName)
                                        .findFirst()
                                        .orElse("Tour #" + tourId));
                    }
                    return "Tour #" + tourId;
                })
                .orElse("Tour #" + tourId);
    }

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
        return createTourBookingDetail(
                bookingItemId, tourId, guestCount, pickupLocation, note, startTime, endTime,
                null, null, "PER_PERSON", guestCount, 0, 0
        );
    }

    @Override
    @Transactional
    public Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime,
            Long tourPackageId,
            String packageName,
            String pricingType,
            int adultCount,
            int childCount,
            int infantCount
    ) {
        TourBookingDetail detail = TourBookingDetail.builder()
                .bookingItemId(bookingItemId)
                .meetingPoint(pickupLocation != null ? pickupLocation : "")
                .guestCount(guestCount)
                .note(note)
                .startTime(startTime)
                .endTime(endTime)
                .tourPackageId(tourPackageId)
                .packageName(packageName)
                .pricingType(pricingType != null ? pricingType : "PER_PERSON")
                .adultCount(adultCount > 0 ? adultCount : guestCount)
                .childCount(childCount)
                .infantCount(infantCount)
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

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getTourBookingEmailVariables(Long detailId) {
        TourBookingDetail detail = tourBookingDetailRepository.findById(detailId).orElse(null);
        if (detail == null) return java.util.Map.of();

        java.util.Map<String, Object> detailMap = new java.util.HashMap<>();
        detailMap.put("guestCount", detail.getGuestCount());
        detailMap.put("meetingPoint", detail.getMeetingPoint());
        detailMap.put("note", detail.getNote());
        detailMap.put("startTime", detail.getStartTime());
        detailMap.put("endTime", detail.getEndTime());

        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("detail", detailMap);
        return variables;
    }
}
