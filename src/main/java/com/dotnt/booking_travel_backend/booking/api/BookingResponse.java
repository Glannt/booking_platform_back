package com.dotnt.booking_travel_backend.booking.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private BookingDto booking;
    private String message;
}
