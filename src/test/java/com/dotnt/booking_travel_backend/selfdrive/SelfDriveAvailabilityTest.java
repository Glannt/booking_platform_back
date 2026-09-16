package com.dotnt.booking_travel_backend.selfdrive;

import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.CategoryAvailabilityDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveBookingDetailRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.SelfDriveAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfDriveAvailabilityTest {

    @Mock
    private SelfDriveBookingDetailRepository detailRepository;

    @Mock
    private FleetFacade fleetFacade;

    @Mock
    private SelfDriveRentalRepository rentalRepository;

    @InjectMocks
    private SelfDriveAvailabilityService availabilityService;

    private final Long categoryId = 1L;
    private final LocalDate start = LocalDate.of(2026, 9, 10);
    private final LocalDate end = LocalDate.of(2026, 9, 15);

    @BeforeEach
    void setUp() {
        when(fleetFacade.getCategoryTotalVehicles(categoryId)).thenReturn(3);
        when(fleetFacade.getVehicleCategoryName(categoryId, "vi")).thenReturn("Xe 4 chỗ");
    }

    @Test
    void whenNoOverlappingBookings_allVehiclesAvailable() {
        when(detailRepository.findActiveOverlappingDetails(categoryId, start, end))
                .thenReturn(List.of());

        CategoryAvailabilityDto result = availabilityService.checkAvailability(categoryId, start, end, "vi");

        assertEquals(3, result.getTotalVehicles());
        assertEquals(3, result.getAvailableVehicles());
        assertEquals(0, result.getBookedVehicles());
        assertTrue(Boolean.TRUE.equals(result.getIsAvailable()));
    }

    @Test
    void whenOneBookingOverlaps_availableCountDecreases() {
        SelfDriveBookingDetail detail1 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 11))
                .endDate(LocalDate.of(2026, 9, 13))
                .build();

        when(detailRepository.findActiveOverlappingDetails(categoryId, start, end))
                .thenReturn(List.of(detail1));

        CategoryAvailabilityDto result = availabilityService.checkAvailability(categoryId, start, end, "vi");

        assertEquals(3, result.getTotalVehicles());
        assertEquals(2, result.getAvailableVehicles());
        assertEquals(1, result.getBookedVehicles());
        assertTrue(Boolean.TRUE.equals(result.getIsAvailable()));
    }

    @Test
    void whenConcurrentBookingsReachTotal_categoryIsSoldOut() {
        // Total is 3, simulated 3 bookings overlapping on Sept 12
        SelfDriveBookingDetail detail1 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 10))
                .endDate(LocalDate.of(2026, 9, 14))
                .build();
        SelfDriveBookingDetail detail2 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 12))
                .endDate(LocalDate.of(2026, 9, 13))
                .build();
        SelfDriveBookingDetail detail3 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 11))
                .endDate(LocalDate.of(2026, 9, 15))
                .build();

        when(detailRepository.findActiveOverlappingDetails(categoryId, start, end))
                .thenReturn(List.of(detail1, detail2, detail3));

        CategoryAvailabilityDto result = availabilityService.checkAvailability(categoryId, start, end, "vi");

        assertEquals(3, result.getTotalVehicles());
        assertEquals(0, result.getAvailableVehicles());
        assertEquals(3, result.getBookedVehicles());
        assertFalse(Boolean.TRUE.equals(result.getIsAvailable()));
    }

    @Test
    void whenNonConcurrentBookingsOverlapInterval_peakDailyConcurrencyIsUsed() {
        // detail 1: Sept 10 to Sept 11
        // detail 2: Sept 13 to Sept 15
        // They do NOT overlap each other, so max concurrent on any day is 1
        SelfDriveBookingDetail detail1 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 10))
                .endDate(LocalDate.of(2026, 9, 11))
                .build();
        SelfDriveBookingDetail detail2 = SelfDriveBookingDetail.builder()
                .startDate(LocalDate.of(2026, 9, 13))
                .endDate(LocalDate.of(2026, 9, 15))
                .build();

        when(detailRepository.findActiveOverlappingDetails(categoryId, start, end))
                .thenReturn(List.of(detail1, detail2));

        CategoryAvailabilityDto result = availabilityService.checkAvailability(categoryId, start, end, "vi");

        assertEquals(3, result.getTotalVehicles());
        assertEquals(2, result.getAvailableVehicles());
        assertEquals(1, result.getBookedVehicles());
        assertTrue(Boolean.TRUE.equals(result.getIsAvailable()));
    }
}
