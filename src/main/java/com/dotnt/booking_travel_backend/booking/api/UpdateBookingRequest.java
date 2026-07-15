package com.dotnt.booking_travel_backend.booking.api;

import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private String customerNotes;
}
