package com.dotnt.booking_travel_backend.booking.api;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private String customerNotes;
    private String idempotencyKey;
    private String contactChannel;
    private String contactId;
    private List<CreateBookingItemRequest> items;
}
