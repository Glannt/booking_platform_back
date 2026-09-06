package com.dotnt.booking_travel_backend.rental;

import com.dotnt.booking_travel_backend.rental.api.RentalFacade;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityDepartureTime;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityBookingDetailRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityDepartureTimeRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntercityServiceTypeAndScheduleTest {

    @Autowired
    private IntercityRouteRepository routeRepository;

    @Autowired
    private IntercityRoutePriceRepository routePriceRepository;

    @Autowired
    private IntercityDepartureTimeRepository departureTimeRepository;

    @Autowired
    private IntercityBookingDetailRepository bookingDetailRepository;

    @Autowired
    private RentalFacade rentalFacade;

    private IntercityRoute testRoute;

    @BeforeEach
    void setUp() {
        testRoute = routeRepository.save(IntercityRoute.builder()
                .originCity("Nha Trang")
                .destinationCity("Đà Lạt")
                .distanceKm(new BigDecimal("135"))
                .estimatedHours(new BigDecimal("3.5"))
                .active(true)
                .hasDepartureTimes(true)
                .supportsShared(true)
                .sharedVehicleName("Dcar Limousine")
                .sharedPriceOneWay(new BigDecimal("250000.00"))
                .sharedPriceRoundTrip(new BigDecimal("480000.00"))
                .build());

        // Private car price: 10k/km
        routePriceRepository.save(IntercityRoutePrice.builder()
                .route(testRoute)
                .vehicleCategoryId(1L)
                .pricingType("PER_KM")
                .perKmAmount(new BigDecimal("10000.00"))
                .roundTripPrice(new BigDecimal("2500000.00")) // Override from 2.7m to 2.5m
                .currency("VND")
                .active(true)
                .build());

        // Outbound departure times: 08:00, 11:00, 13:00, 17:00
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("08:00").direction("OUTBOUND").orderIndex(1).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("11:00").direction("OUTBOUND").orderIndex(2).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("13:00").direction("OUTBOUND").orderIndex(3).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("17:00").direction("OUTBOUND").orderIndex(4).active(true).build());

        // Return departure times: 08:00, 13:00, 16:00, 17:00
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("08:00").direction("RETURN").orderIndex(1).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("13:00").direction("RETURN").orderIndex(2).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("16:00").direction("RETURN").orderIndex(3).active(true).build());
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(testRoute).departureTime("17:00").direction("RETURN").orderIndex(4).active(true).build());
    }

    @Test
    void testDepartureTimesByDirection() {
        List<IntercityDepartureTime> outbound = departureTimeRepository
                .findByRouteIdAndDirectionAndActiveTrueOrderByOrderIndexAsc(testRoute.getId(), "OUTBOUND");
        assertEquals(4, outbound.size());
        assertEquals("08:00", outbound.get(0).getDepartureTime());
        assertEquals("11:00", outbound.get(1).getDepartureTime());
        assertEquals("13:00", outbound.get(2).getDepartureTime());
        assertEquals("17:00", outbound.get(3).getDepartureTime());

        List<IntercityDepartureTime> returnTimes = departureTimeRepository
                .findByRouteIdAndDirectionAndActiveTrueOrderByOrderIndexAsc(testRoute.getId(), "RETURN");
        assertEquals(4, returnTimes.size());
        assertEquals("08:00", returnTimes.get(0).getDepartureTime());
        assertEquals("13:00", returnTimes.get(1).getDepartureTime());
        assertEquals("16:00", returnTimes.get(2).getDepartureTime());
        assertEquals("17:00", returnTimes.get(3).getDepartureTime());
    }

    @Test
    void testSharedBookingCreation() {
        // 2 passengers, round trip on shared limo
        // One way = 250k, round trip override = 480k / pax -> 2 pax = 960k
        BigDecimal expectedTotal = new BigDecimal("960000.00");
        BigDecimal expectedBase = new BigDecimal("500000.00"); // 2 * 250k

        Long detailId = rentalFacade.createIntercityBookingDetail(
                "SHARED",
                2,
                null, // No vehicle category required for shared
                testRoute.getId(),
                "ROUND_TRIP",
                LocalDateTime.of(2026, 9, 10, 8, 0),
                LocalDate.of(2026, 9, 12),
                LocalTime.of(16, 0),
                new BigDecimal("480000.00"),
                expectedBase,
                expectedTotal,
                "123 Tran Phu, Nha Trang",
                "Hotel Dalat Center",
                "Shared booking test"
        );

        assertNotNull(detailId);
        IntercityBookingDetail detail = bookingDetailRepository.findById(detailId).orElse(null);
        assertNotNull(detail);
        assertEquals("SHARED", detail.getServiceType());
        assertEquals(2, detail.getPassengerCount());
        assertNull(detail.getVehicleCategoryId());
        assertEquals(0, expectedTotal.compareTo(detail.getTotalPrice()));
        assertEquals(LocalTime.of(16, 0), detail.getReturnTime());
    }

    @Test
    void testPrivateBookingWithRoundTripOverride() {
        // 1 private vehicle, round trip with override: 2,500,000 instead of 2 * 1,350,000 = 2,700,000
        BigDecimal expectedOneWay = new BigDecimal("1350000.00");
        BigDecimal expectedTotal = new BigDecimal("2500000.00");

        Long detailId = rentalFacade.createIntercityBookingDetail(
                "PRIVATE",
                1,
                1L,
                testRoute.getId(),
                "ROUND_TRIP",
                LocalDateTime.of(2026, 9, 15, 9, 30),
                LocalDate.of(2026, 9, 17),
                LocalTime.of(14, 0),
                expectedOneWay,
                expectedOneWay,
                expectedTotal,
                "45 Le Thanh Ton, Nha Trang",
                "Dalat Palace",
                "Private booking custom time"
        );

        assertNotNull(detailId);
        IntercityBookingDetail detail = bookingDetailRepository.findById(detailId).orElse(null);
        assertNotNull(detail);
        assertEquals("PRIVATE", detail.getServiceType());
        assertEquals(1L, detail.getVehicleCategoryId());
        assertEquals(0, expectedTotal.compareTo(detail.getTotalPrice()));
        assertEquals(LocalTime.of(9, 30), detail.getDepartureTime());
    }
}
