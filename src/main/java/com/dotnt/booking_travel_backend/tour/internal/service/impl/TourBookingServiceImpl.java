package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.tour.internal.entity.PriceListPerPerson;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourBookingDetail;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourTranslation;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourBookingServiceImpl implements TourBookingService {

    private final TourBookingDetailRepository tourBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository tourRepository;
    private final com.dotnt.booking_travel_backend.tour.internal.repository.TourPackageRepository tourPackageRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isContactOnlyPackage(Long tourPackageId) {
        if (tourPackageId == null) return false;
        return tourPackageRepository.findById(tourPackageId)
                .map(com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage::isContactOnly)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal quoteTourPackage(Long tourPackageId, int adultCount, int childCount, int infantCount) {
        if (tourPackageId == null) return null;

        TourPackage pkg = tourPackageRepository.findById(tourPackageId).orElse(null);
        if (pkg == null) {
            log.warn("No tour package {} to price this booking line with", tourPackageId);
            return null;
        }
        // Contact-only sells as a quote: having no price row is the design here, not a hole.
        if (pkg.isContactOnly()) return BigDecimal.ZERO;

        java.util.List<PriceListPerPerson> rows = pkg.getPrices() == null
                ? java.util.List.of()
                : pkg.getPrices().stream().filter(r -> r.isActive() && r.getPricePerPerson() != null).toList();
        if (rows.isEmpty()) {
            log.warn("Tour package {} ({}) has no active price row; there is no total to invent for it",
                    pkg.getId(), pkg.getName());
            return null;
        }

        return passengerTotal(rows, pkg, "ADULT", adultCount)
                .add(passengerTotal(rows, pkg, "CHILD", childCount))
                .add(passengerTotal(rows, pkg, "INFANT", infantCount));
    }

    @Override
    @Transactional(readOnly = true)
    public DepositPolicy resolveDepositPolicyForPackage(Long tourPackageId) {
        if (tourPackageId == null) return null;
        return tourPackageRepository.findById(tourPackageId)
                .map(pkg -> DepositPolicies.configured(pkg.getDepositPolicies()))
                .orElse(null);
    }

    private BigDecimal passengerTotal(java.util.List<PriceListPerPerson> rows, TourPackage pkg, String type, int count) {
        if (count <= 0) return BigDecimal.ZERO;
        return rows.stream()
                .filter(r -> type.equalsIgnoreCase(r.getPassengerType()))
                .findFirst()
                .map(r -> r.getPricePerPerson().multiply(BigDecimal.valueOf(count)))
                .orElseGet(() -> {
                    // A passenger type with no row books as free. For an infant that is correct
                    // policy; for an adult it is revenue quietly disappearing, so it is logged
                    // rather than assumed either way.
                    log.warn("Tour package {} ({}) has no active {} price row; {} of them book as free",
                            pkg.getId(), pkg.getName(), type, count);
                    return BigDecimal.ZERO;
                });
    }

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
