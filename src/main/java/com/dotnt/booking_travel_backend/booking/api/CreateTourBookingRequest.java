package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTourBookingRequest {
    @NotBlank @Size(max = 100) private String customerName;
    @NotBlank @Email private String customerEmail;
    @NotBlank @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,15}$") private String customerPhone;
    @Size(max = 500) private String customerNotes;
    private String contactChannel;
    private String contactId;

    @NotNull private Long tourId;
    @NotNull @Min(1) private Integer guestCount;
    private Long vehicleCategoryId;
    private String pickupLocation;
    private String note;
    private String description;
    private String startTime;
    private String endTime;
    @NotNull @DecimalMin("0.0") private BigDecimal totalPrice;
}
