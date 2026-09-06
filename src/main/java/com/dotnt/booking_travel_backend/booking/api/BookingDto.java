package com.dotnt.booking_travel_backend.booking.api;

import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private String bookingCode;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private String customerNotes;
    private String idempotencyKey;
    private String contactChannel;
    private String contactId;
    private String zaloId;
    private String kakaoTalkId;
    private String whatsapp;
    private String internalNotes;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal paidAmount;
    private BigDecimal depositAmount;
    private BigDecimal receivedAmount;
    private BigDecimal remainingAmount;
    private String deletionReason;
    private String assignedDriver;
    private String assignedVehicle;
    private List<BookingItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
