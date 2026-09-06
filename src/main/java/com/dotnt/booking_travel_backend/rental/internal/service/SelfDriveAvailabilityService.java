package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.rental.api.dtos.CategoryAvailabilityDto;
import com.dotnt.booking_travel_backend.rental.internal.entity.Rental;
import com.dotnt.booking_travel_backend.rental.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.repository.RentalRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.SelfDriveBookingDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfDriveAvailabilityService {

    private final SelfDriveBookingDetailRepository detailRepository;
    private final FleetFacade fleetFacade;
    private final RentalRepository rentalRepository;

    public CategoryAvailabilityDto checkAvailability(Long categoryId, LocalDate startDate, LocalDate endDate, String locale) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null || endDate.isBefore(startDate)) endDate = startDate;

        int total = fleetFacade.getCategoryTotalVehicles(categoryId);
        String catName = fleetFacade.getVehicleCategoryName(categoryId, locale);

        List<SelfDriveBookingDetail> overlapping = detailRepository.findActiveOverlappingDetails(categoryId, startDate, endDate);

        // Calculate maximum concurrent overlapping bookings on any single day within [startDate, endDate]
        int maxOccupied = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate day = current;
            int countOnDay = (int) overlapping.stream()
                    .filter(d -> !day.isBefore(d.getStartDate()) && !day.isAfter(d.getEndDate()))
                    .count();
            if (countOnDay > maxOccupied) {
                maxOccupied = countOnDay;
            }
            current = current.plusDays(1);
        }

        int available = Math.max(0, total - maxOccupied);
        boolean isAvailable = available > 0;

        return CategoryAvailabilityDto.builder()
                .vehicleCategoryId(categoryId)
                .categoryCode("cat-" + categoryId)
                .categoryName(catName)
                .totalVehicles(total)
                .availableVehicles(available)
                .bookedVehicles(maxOccupied)
                .isAvailable(isAvailable)
                .build();
    }

    public List<CategoryAvailabilityDto> getAvailabilityForAllCategories(LocalDate startDate, LocalDate endDate, String locale) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null || endDate.isBefore(startDate)) endDate = startDate;

        // Find all active self-drive rental category IDs
        List<Rental> selfDriveRentals = rentalRepository.findAll().stream()
                .filter(r -> !r.isDriverRequired() && r.isActive())
                .toList();

        Set<Long> categoryIds = new LinkedHashSet<>();
        for (Rental r : selfDriveRentals) {
            if (r.getVehicleCategoryId() != null) {
                categoryIds.add(r.getVehicleCategoryId());
            }
        }

        // Fallback: if no rentals configured yet, check standard 1, 2, 3
        if (categoryIds.isEmpty()) {
            categoryIds.addAll(List.of(1L, 2L, 3L));
        }

        List<CategoryAvailabilityDto> result = new ArrayList<>();
        for (Long catId : categoryIds) {
            result.add(checkAvailability(catId, startDate, endDate, locale));
        }
        return result;
    }
}
