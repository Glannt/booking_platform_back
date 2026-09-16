package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.CategoryAvailabilityDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveBookingDetailRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.SelfDriveAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SelfDriveFacade {

    private final SelfDriveBookingDetailRepository selfDriveBookingDetailRepository;
    private final SelfDriveAvailabilityService availabilityService;

    public CategoryAvailabilityDto checkSelfDriveAvailability(
            Long categoryId, LocalDate startDate, LocalDate endDate, String locale) {
        return availabilityService.checkAvailability(categoryId, startDate, endDate, locale);
    }

    public List<CategoryAvailabilityDto> getSelfDriveAvailabilityForAll(
            LocalDate startDate, LocalDate endDate, String locale) {
        return availabilityService.getAvailabilityForAllCategories(startDate, endDate, locale);
    }

    public Long createSelfDriveBookingDetail(
            Long serviceOptionId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal clientBasePrice,
            BigDecimal clientTotalPrice,
            String pickupLocation,
            String dropoffLocation
    ) {
        int rentalDays = 1;
        if (startDate != null && endDate != null) {
            rentalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (rentalDays <= 0) rentalDays = 1;
        }

        SelfDriveBookingDetail detail = SelfDriveBookingDetail.builder()
                .vehicleCategoryId(serviceOptionId)
                .startDate(startDate)
                .endDate(endDate)
                .rentalDays(rentalDays)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .basePrice(clientBasePrice)
                .deposit(BigDecimal.ZERO)
                .totalPrice(clientTotalPrice)
                .currency("VND")
                .depositStatus("PENDING")
                .status("PENDING")
                .build();

        detail = selfDriveBookingDetailRepository.save(detail);
        return detail.getId();
    }

    public void updateSelfDriveBookingItemId(Long detailId, Long bookingItemId) {
        SelfDriveBookingDetail detail = selfDriveBookingDetailRepository.findById(detailId).orElse(null);
        if (detail != null) {
            detail.setBookingItemId(bookingItemId);
            selfDriveBookingDetailRepository.save(detail);
        }
    }

    public LocalDate[] getSelfDriveBookingDates(Long selfDriveBookingDetailId) {
        SelfDriveBookingDetail detail = selfDriveBookingDetailRepository.findById(selfDriveBookingDetailId).orElse(null);
        if (detail != null && detail.getStartDate() != null && detail.getEndDate() != null) {
            return new LocalDate[]{detail.getStartDate(), detail.getEndDate()};
        }
        return null;
    }

    public Map<String, Object> getSelfDriveBookingEmailVariables(Long detailId, String locale) {
        SelfDriveBookingDetail detail = selfDriveBookingDetailRepository.findById(detailId).orElse(null);
        if (detail == null) return Map.of();

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("startDate", detail.getStartDate());
        detailMap.put("endDate", detail.getEndDate());
        detailMap.put("rentalDays", detail.getRentalDays());
        detailMap.put("pickupLocation", detail.getPickupLocation());
        detailMap.put("dropoffLocation", detail.getDropoffLocation());
        detailMap.put("vehicleCategoryId", detail.getVehicleCategoryId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("detail", detailMap);
        return variables;
    }
}
