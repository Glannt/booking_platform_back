package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRentCarBookingRequest {
    @NotBlank @Size(max = 100) private String customerName;
    @NotBlank @Email private String customerEmail;
    @NotBlank @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,15}$") private String customerPhone;
    @Size(max = 500) private String customerNotes;
    private String contactChannel;
    private String contactId;
    private String zaloId;
    private String kakaoTalkId;
    private String whatsapp;

    @NotNull private Long routeId;
    private Long vehicleCategoryId;
    private String serviceType;
    private Integer passengerCount;
    @NotBlank private String tripType;
    @NotNull private LocalDate departureDate;
    @NotBlank private String departureTime;
    private LocalDate returnDate;
    private String returnTime;
    @NotBlank private String pickupAddress;
    private String dropoffAddress;
    // No basePrice/totalPrice: a PRIVATE line is priced from price_list inside the facade and a
    // SHARED line is operator-quoted (books at 0 until the operator sets a figure in admin). No
    // deposit fields either: the deposit comes from the stored policy.
}
