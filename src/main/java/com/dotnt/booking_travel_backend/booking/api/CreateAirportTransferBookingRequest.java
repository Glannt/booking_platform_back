package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAirportTransferBookingRequest {
    @NotBlank @Size(max = 100) private String customerName;
    @NotBlank @Email private String customerEmail;
    @NotBlank @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,15}$") private String customerPhone;
    @Size(max = 500) private String customerNotes;
    private String contactChannel;
    private String contactId;
    private String zaloId;
    private String kakaoTalkId;
    private String whatsapp;

    @NotNull private Long vehicleCategoryId;
    @NotBlank private String tripType;
    @NotBlank private String outboundDirection;
    @NotNull private LocalDate outboundDate;
    @NotNull private LocalDateTime outboundPickupTime;
    private String outboundFlightNumber;
    @NotBlank private String outboundPickupAddress;
    private String outboundDropoffAddress;

    private String returnDirection;
    private LocalDate returnDate;
    private LocalDateTime returnPickupTime;
    private String returnFlightNumber;
    private String returnPickupAddress;
    private String returnDropoffAddress;

    private Long routeId;
    private BigDecimal actualKm;

    @DecimalMin("0.0") private BigDecimal basePrice;
    @NotNull @DecimalMin("0.0") private BigDecimal totalPrice;

    private Integer passengerCount;
    private BigDecimal luggageWeight;

    private String depositType;
    private BigDecimal depositRate;
    private BigDecimal depositAmount;
}
