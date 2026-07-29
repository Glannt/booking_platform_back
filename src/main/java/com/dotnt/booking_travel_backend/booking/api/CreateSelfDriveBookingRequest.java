package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @NotNull private Long vehicleCategoryId;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    private Integer rentalDays;
    @NotBlank private String pickupLocation;
    private String dropoffLocation;
    @DecimalMin("0.0") private BigDecimal basePrice;
    @DecimalMin("0.0") private BigDecimal deposit;
    @NotNull @DecimalMin("0.0") private BigDecimal totalPrice;
}
