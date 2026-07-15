package com.dotnt.booking_travel_backend.booking.api;

import java.math.BigDecimal;

public record BookingCreatedEvent(Long bookingId, String bookingCode, BigDecimal totalAmount) {
}
