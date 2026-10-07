package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSelfDriveBookingRequest {
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
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    private Integer rentalDays;
    @NotBlank private String pickupLocation;
    private String dropoffLocation;

    private Long seatAddOnId;
    private Long fuelAddOnId;
    private List<Long> addOnIds;

    // No basePrice/deposit/totalPrice: the server prices the line from the rental and carries
    // no client-chosen deposit (see SelfDriveFacade.resolveDepositPolicy).
    /** Applied server-side against the server's own price; the browser's discountAmount is not read. */
    private String discountCode;
}
