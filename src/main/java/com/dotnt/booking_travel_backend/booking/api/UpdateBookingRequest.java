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
    private String deletionReason;
    private String assignedDriver;
    private String assignedVehicle;
}
