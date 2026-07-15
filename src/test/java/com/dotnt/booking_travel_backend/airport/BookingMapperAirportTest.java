package com.dotnt.booking_travel_backend.airport;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportBookingDetail;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportBookingDetailRepository;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperAirportTest {

    @Mock
    private AirportBookingDetailRepository airportBookingDetailRepository;

    @Mock
    private TourBookingDetailRepository tourBookingDetailRepository;

    @InjectMocks
    private BookingMapper bookingMapper;

    private AirportBookingDetail oneWayDetail;
    private AirportBookingDetail roundTripDetail;

    @BeforeEach
    void setUp() throws Exception {
        // Inject a real ObjectMapper — @Mock gives null
        var omField = BookingMapper.class.getDeclaredField("objectMapper");
        omField.setAccessible(true);
        omField.set(bookingMapper, new ObjectMapper());

        oneWayDetail = AirportBookingDetail.builder()
                .tripType("ONE_WAY")
                .outboundDirection("FROM_AIRPORT")
                .outboundDate(LocalDate.of(2026, 7, 15))
                .outboundPickupTime(LocalDateTime.of(2026, 7, 15, 14, 30))
                .outboundFlightNumber("VN123")
                .outboundPickupAddress("Sân bay Cam Ranh (CXR)")
                .outboundDropoffAddress("123 Trần Phú, Nha Trang")
                .vehicleCategoryId(101L)
                .basePrice(new BigDecimal("300000"))
                .totalPrice(new BigDecimal("300000"))
                .currency("VND")
                .passenger(2)
                .note("Test note")
                .build();

        roundTripDetail = AirportBookingDetail.builder()
                .tripType("ROUND_TRIP")
                .outboundDirection("FROM_AIRPORT")
                .outboundDate(LocalDate.of(2026, 7, 15))
                .outboundPickupTime(LocalDateTime.of(2026, 7, 15, 14, 30))
                .outboundFlightNumber("VN123")
                .outboundPickupAddress("Sân bay Cam Ranh (CXR)")
                .outboundDropoffAddress("123 Trần Phú, Nha Trang")
                .returnDirection("TO_AIRPORT")
                .returnDate(LocalDate.of(2026, 7, 20))
                .returnPickupTime(LocalDateTime.of(2026, 7, 20, 10, 0))
                .returnFlightNumber("VJ456")
                .returnPickupAddress("123 Trần Phú, Nha Trang")
                .returnDropoffAddress("Sân bay Cam Ranh (CXR)")
                .vehicleCategoryId(102L)
                .basePrice(new BigDecimal("450000"))
                .totalPrice(new BigDecimal("832500"))
                .currency("VND")
                .passenger(3)
                .note("Round trip test")
                .build();
    }

    private BookingItem createAirportItem(Long airportDetailId) {
        BookingItem item = BookingItem.builder()
                .quantity(1)
                .unitPrice(new BigDecimal("300000"))
                .totalPrice(new BigDecimal("300000"))
                .airportTransferBookingDetailId(airportDetailId)
                .build();
        item.setId(1L);
        return item;
    }

    @Test
    void toDto_oneWayMapsOutboundFieldsCorrectly() {
        BookingItem item = createAirportItem(1L);
        when(airportBookingDetailRepository.findById(1L)).thenReturn(Optional.of(oneWayDetail));

        var dto = bookingMapper.toDto(item);

        assertEquals(LocalDateTime.of(2026, 7, 15, 14, 30), dto.getStartDate());
        assertEquals("Sân bay Cam Ranh (CXR)", dto.getPickupLocation());
        assertEquals("123 Trần Phú, Nha Trang", dto.getDropoffLocation());
        assertEquals("VN123", dto.getFlightNumber());
    }

    @Test
    void toDto_oneWayDynamicDetailsContainsStructuredJson() throws Exception {
        BookingItem item = createAirportItem(1L);
        when(airportBookingDetailRepository.findById(1L)).thenReturn(Optional.of(oneWayDetail));

        var dto = bookingMapper.toDto(item);

        assertNotNull(dto.getDynamicDetails());

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> ad = om.readValue(dto.getDynamicDetails(), Map.class);

        assertEquals("ONE_WAY", ad.get("tripType"));
        assertEquals("FROM_AIRPORT", ad.get("outboundDirection"));
        assertEquals("2026-07-15", ad.get("outboundDate"));
        assertEquals("VN123", ad.get("outboundFlightNumber"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("outboundPickupAddress"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("outboundDropoffAddress"));
        assertEquals(101, ((Number) ad.get("vehicleCategoryId")).intValue());
        assertEquals(300000, ((Number) ad.get("basePrice")).intValue());
        assertEquals("VND", ad.get("currency"));

        assertNull(ad.get("returnDirection"));
        assertNull(ad.get("returnDate"));
        assertNull(ad.get("returnFlightNumber"));
    }

    @Test
    void toDto_roundTripDynamicDetailsContainsBothLegs() throws Exception {
        BookingItem item = createAirportItem(2L);
        when(airportBookingDetailRepository.findById(2L)).thenReturn(Optional.of(roundTripDetail));

        var dto = bookingMapper.toDto(item);

        assertNotNull(dto.getDynamicDetails());

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> ad = om.readValue(dto.getDynamicDetails(), Map.class);

        assertEquals("ROUND_TRIP", ad.get("tripType"));

        assertEquals("FROM_AIRPORT", ad.get("outboundDirection"));
        assertEquals("2026-07-15", ad.get("outboundDate"));
        assertEquals("VN123", ad.get("outboundFlightNumber"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("outboundPickupAddress"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("outboundDropoffAddress"));

        assertEquals("TO_AIRPORT", ad.get("returnDirection"));
        assertEquals("2026-07-20", ad.get("returnDate"));
        assertEquals("2026-07-20T10:00", ad.get("returnPickupTime"));
        assertEquals("VJ456", ad.get("returnFlightNumber"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("returnPickupAddress"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("returnDropoffAddress"));

        assertEquals(450000, ((Number) ad.get("basePrice")).intValue());
        assertEquals(832500, ((Number) ad.get("totalPrice")).intValue());
        assertEquals("VND", ad.get("currency"));
        assertEquals(3, ((Number) ad.get("passenger")).intValue());
    }

    @Test
    void toDto_roundTripStartDateIsOutboundPickupTime() {
        BookingItem item = createAirportItem(2L);
        when(airportBookingDetailRepository.findById(2L)).thenReturn(Optional.of(roundTripDetail));

        var dto = bookingMapper.toDto(item);

        assertEquals(LocalDateTime.of(2026, 7, 15, 14, 30), dto.getStartDate());
        assertEquals("Sân bay Cam Ranh (CXR)", dto.getPickupLocation());
        assertEquals("123 Trần Phú, Nha Trang", dto.getDropoffLocation());
    }

    @Test
    void toDto_whenDetailNotFound_returnsBasicDto() {
        BookingItem item = createAirportItem(999L);
        when(airportBookingDetailRepository.findById(999L)).thenReturn(Optional.empty());

        var dto = bookingMapper.toDto(item);

        assertEquals(1L, dto.getId());
        assertNull(dto.getStartDate());
        assertNull(dto.getPickupLocation());
        assertNull(dto.getDropoffLocation());
        assertNull(dto.getFlightNumber());
        assertNull(dto.getDynamicDetails());
    }

    @Test
    void toDto_nonAirportItem_notAffected() {
        BookingItem item = BookingItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("500000"))
                .totalPrice(new BigDecimal("1000000"))
                .dynamicDetails("Some tour details")
                .build();
        item.setId(10L);

        var dto = bookingMapper.toDto(item);

        assertEquals("Some tour details", dto.getDynamicDetails());
        assertNull(dto.getStartDate());
        assertNull(dto.getFlightNumber());
    }
}
