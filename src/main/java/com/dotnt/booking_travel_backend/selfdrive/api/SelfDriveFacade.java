package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.CategoryAvailabilityDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRental;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRentalPrice;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveBookingDetailRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalPriceRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.SelfDriveAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final SelfDriveRentalRepository rentalRepository;
    private final SelfDriveRentalPriceRepository priceRepository;

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

    /**
     * What this line is worth when the server prices it: the rental's active daily rate for that
     * vehicle category, times the same number of rental days the confirm page charges for — the
     * day count is floored at the rental's {@code minimumDays}, exactly as the browser does.
     *
     * <p>{@code null} means "cannot be priced at all": no active rental or no price row behind it.
     * That is a configuration hole to report, never a price of 0. A rental an admin marked
     * contact-only has no price on purpose and quotes as 0, the way a contact-only tour does.
     *
     * @return the server-computed line total, or {@code null} if the line cannot be priced
     */
    public BigDecimal quoteSelfDriveRental(Long vehicleCategoryId, LocalDate startDate, LocalDate endDate) {
        if (vehicleCategoryId == null) {
            log.warn("Cannot price a self-drive line without a vehicle category");
            return null;
        }

        // ponytail: rentals are 1:1 with a vehicle category today, so lowest id wins if that ever
        // stops holding — a wrong pick shows up as a logged delta, not as a silent wrong price.
        SelfDriveRental rental = rentalRepository.findByVehicleCategoryIdAndActiveTrue(vehicleCategoryId).stream()
                .min(java.util.Comparator.comparing(SelfDriveRental::getId))
                .orElse(null);
        if (rental == null) {
            log.warn("No active self-drive rental for vehicle category {}; cannot price this line", vehicleCategoryId);
            return null;
        }

        SelfDriveRentalPrice price = priceRepository
                .findByVehicleCategoryIdAndActiveTrueOrderById(vehicleCategoryId).stream()
                .filter(p -> rental.getId().equals(p.getRentalId()))
                .findFirst()
                .orElse(null);
        if (price == null || price.getAmount() == null) {
            if (rental.isContactOnly()) return BigDecimal.ZERO;
            log.warn("No active self-drive price row for rental {} / vehicle category {}; the client figure is what gets kept",
                    rental.getId(), vehicleCategoryId);
            return null;
        }

        int minimumDays = rental.getMinimumDays() != null ? Math.max(1, rental.getMinimumDays()) : 1;
        int billedDays = Math.max(minimumDays, chargedDays(startDate, endDate));
        return price.getAmount().multiply(BigDecimal.valueOf(billedDays));
    }

    /** Same day count the confirm page bills for: exclusive of the end date, never below 1. */
    private static int chargedDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 1;
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate);
        return days <= 0 ? 1 : days;
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
