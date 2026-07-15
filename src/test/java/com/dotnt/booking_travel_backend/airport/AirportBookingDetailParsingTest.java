package com.dotnt.booking_travel_backend.airport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the airport booking detail JSON parsing logic.
 * This mirrors the parsing code in BookingServiceImpl.createBooking().
 */
class AirportBookingDetailParsingTest {

    private final ObjectMapper om = new ObjectMapper();

    private Map<String, Object> parseDynamicDetails(String json) {
        // Mirrors the real code in BookingServiceImpl: try/catch around readValue
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return om.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Test
    void parseOneWayDetails() {
        String json = """
            {
              "tripType": "ONE_WAY",
              "outboundDirection": "FROM_AIRPORT",
              "outboundDate": "2026-07-15",
              "outboundPickupTime": "2026-07-15T14:30:00",
              "outboundFlightNumber": "VN123",
              "outboundPickupAddress": "Sân bay Cam Ranh (CXR)",
              "outboundDropoffAddress": "123 Trần Phú, Nha Trang",
              "returnDirection": null,
              "returnDate": null,
              "returnPickupTime": null,
              "returnFlightNumber": null,
              "returnPickupAddress": null,
              "returnDropoffAddress": null,
              "vehicleCategoryId": 101,
              "basePrice": 300000,
              "totalPrice": 300000,
              "currency": "VND"
            }
            """;

        Map<String, Object> ad = parseDynamicDetails(json);

        assertEquals("ONE_WAY", ad.get("tripType"));
        assertEquals("FROM_AIRPORT", ad.get("outboundDirection"));
        assertEquals("2026-07-15", ad.get("outboundDate"));
        assertEquals("2026-07-15T14:30:00", ad.get("outboundPickupTime"));
        assertEquals("VN123", ad.get("outboundFlightNumber"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("outboundPickupAddress"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("outboundDropoffAddress"));
        assertNull(ad.get("returnDirection"));
        assertNull(ad.get("returnDate"));
        assertNull(ad.get("returnFlightNumber"));
        // Jackson deserializes JSON integers as Integer, not Long
        assertEquals(101, ((Number) ad.get("vehicleCategoryId")).intValue());
        assertEquals(300000, ((Number) ad.get("basePrice")).intValue());
        assertEquals(300000, ((Number) ad.get("totalPrice")).intValue());
        assertEquals("VND", ad.get("currency"));
    }

    @Test
    void parseRoundTripDetails() {
        String json = """
            {
              "tripType": "ROUND_TRIP",
              "outboundDirection": "FROM_AIRPORT",
              "outboundDate": "2026-07-15",
              "outboundPickupTime": "2026-07-15T14:30:00",
              "outboundFlightNumber": "VN123",
              "outboundPickupAddress": "Sân bay Cam Ranh (CXR)",
              "outboundDropoffAddress": "123 Trần Phú, Nha Trang",
              "returnDirection": "TO_AIRPORT",
              "returnDate": "2026-07-20",
              "returnPickupTime": "2026-07-20T10:00:00",
              "returnFlightNumber": "VJ456",
              "returnPickupAddress": "123 Trần Phú, Nha Trang",
              "returnDropoffAddress": "Sân bay Cam Ranh (CXR)",
              "vehicleCategoryId": 102,
              "basePrice": 450000,
              "totalPrice": 832500,
              "currency": "VND"
            }
            """;

        Map<String, Object> ad = parseDynamicDetails(json);

        assertEquals("ROUND_TRIP", ad.get("tripType"));
        assertEquals("FROM_AIRPORT", ad.get("outboundDirection"));
        assertEquals("TO_AIRPORT", ad.get("returnDirection"));
        assertEquals("2026-07-20", ad.get("returnDate"));
        assertEquals("2026-07-20T10:00:00", ad.get("returnPickupTime"));
        assertEquals("VJ456", ad.get("returnFlightNumber"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("returnPickupAddress"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("returnDropoffAddress"));
        assertEquals(450000, ((Number) ad.get("basePrice")).intValue());
        assertEquals(832500, ((Number) ad.get("totalPrice")).intValue());
    }

    @Test
    void parseEmptyDynamicDetailsReturnsEmptyMap() {
        assertTrue(parseDynamicDetails(null).isEmpty());
        assertTrue(parseDynamicDetails("").isEmpty());
        assertTrue(parseDynamicDetails("  ").isEmpty());
    }

    @Test
    void parseWithMissingFieldsUsesDefaults() {
        String json = """
            {
              "tripType": "ONE_WAY"
            }
            """;

        Map<String, Object> ad = parseDynamicDetails(json);

        assertEquals("ONE_WAY", ad.get("tripType"));
        assertNull(ad.get("outboundDirection"));
        assertNull(ad.get("outboundDate"));
        assertNull(ad.get("returnDirection"));
        assertNull(ad.get("vehicleCategoryId"));
        assertNull(ad.get("basePrice"));
        assertNull(ad.get("totalPrice"));
    }

    @Test
    void buildAirportBookingDetail_oneWay() {
        String json = """
            {
              "tripType": "ONE_WAY",
              "outboundDirection": "FROM_AIRPORT",
              "outboundDate": "2026-07-15",
              "outboundPickupTime": "2026-07-15T14:30:00",
              "outboundFlightNumber": "VN123",
              "outboundPickupAddress": "Sân bay Cam Ranh (CXR)",
              "outboundDropoffAddress": "123 Trần Phú, Nha Trang",
              "vehicleCategoryId": 101,
              "basePrice": 300000,
              "totalPrice": 300000,
              "currency": "VND"
            }
            """;

        Map<String, Object> ad = parseDynamicDetails(json);

        // Extract fields like BookingServiceImpl does — always call .toString() then parse
        String tripType = ad.getOrDefault("tripType", "ONE_WAY").toString();
        String outboundDirection = ad.getOrDefault("outboundDirection", "FROM_AIRPORT").toString();
        LocalDate outboundDate = LocalDate.parse(ad.get("outboundDate").toString());
        LocalDateTime outboundPickupTime = LocalDateTime.parse(ad.get("outboundPickupTime").toString());
        Long vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString());
        java.math.BigDecimal basePrice = new java.math.BigDecimal(ad.get("basePrice").toString());
        java.math.BigDecimal totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString());

        assertEquals("ONE_WAY", tripType);
        assertEquals("FROM_AIRPORT", outboundDirection);
        assertEquals(LocalDate.of(2026, 7, 15), outboundDate);
        assertEquals(LocalDateTime.of(2026, 7, 15, 14, 30), outboundPickupTime);
        assertEquals(101L, vehicleCategoryId);
        assertEquals(0, new java.math.BigDecimal("300000").compareTo(basePrice));
        assertEquals(0, new java.math.BigDecimal("300000").compareTo(totalPrice));

        assertNull(ad.get("returnDirection"));
        assertNull(ad.get("returnDate"));
        assertNull(ad.get("returnPickupTime"));
    }

    @Test
    void buildAirportBookingDetail_roundTrip() {
        String json = """
            {
              "tripType": "ROUND_TRIP",
              "outboundDirection": "FROM_AIRPORT",
              "outboundDate": "2026-07-15",
              "outboundPickupTime": "2026-07-15T14:30:00",
              "outboundFlightNumber": "VN123",
              "outboundPickupAddress": "Sân bay Cam Ranh (CXR)",
              "outboundDropoffAddress": "123 Trần Phú, Nha Trang",
              "returnDirection": "TO_AIRPORT",
              "returnDate": "2026-07-20",
              "returnPickupTime": "2026-07-20T10:00:00",
              "returnFlightNumber": "VJ456",
              "returnPickupAddress": "123 Trần Phú, Nha Trang",
              "returnDropoffAddress": "Sân bay Cam Ranh (CXR)",
              "vehicleCategoryId": 102,
              "basePrice": 450000,
              "totalPrice": 832500,
              "currency": "VND"
            }
            """;

        Map<String, Object> ad = parseDynamicDetails(json);

        assertEquals("ROUND_TRIP", ad.get("tripType"));
        assertEquals("FROM_AIRPORT", ad.get("outboundDirection"));
        assertEquals(LocalDate.of(2026, 7, 15), LocalDate.parse(ad.get("outboundDate").toString()));
        assertEquals("VN123", ad.get("outboundFlightNumber"));

        assertEquals("TO_AIRPORT", ad.get("returnDirection"));
        assertEquals(LocalDate.of(2026, 7, 20), LocalDate.parse(ad.get("returnDate").toString()));
        assertEquals(LocalDateTime.of(2026, 7, 20, 10, 0), LocalDateTime.parse(ad.get("returnPickupTime").toString()));
        assertEquals("VJ456", ad.get("returnFlightNumber"));
        assertEquals("123 Trần Phú, Nha Trang", ad.get("returnPickupAddress"));
        assertEquals("Sân bay Cam Ranh (CXR)", ad.get("returnDropoffAddress"));
    }

    @Test
    void parseInvalidJsonReturnsEmptyMap() {
        // Mirrors real BookingServiceImpl behavior: catch returns empty map
        Map<String, Object> ad = parseDynamicDetails("{invalid json");
        assertTrue(ad.isEmpty());
    }

    @Test
    void parseLegacyTextFormatReturnsEmptyMap() {
        // Old format: "Trip Type: return, Outbound Flight: VN123..."
        // Not valid JSON — catch returns empty map
        String legacyText = "Trip Type: return, Outbound Flight: VN123, Return Flight: VJ456";
        Map<String, Object> ad = parseDynamicDetails(legacyText);
        assertTrue(ad.isEmpty());
    }
}
