package com.dotnt.booking_travel_backend.booking.api;

import jakarta.validation.constraints.*;
import lombok.*;

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
    private String zaloId;
    private String kakaoTalkId;
    private String whatsapp;

    @NotNull private Long tourId;
    private Long tourPackageId;
    private String packageName;
    private Integer adultCount;
    private Integer childCount;
    private Integer infantCount;
    @NotNull @Min(1) private Integer guestCount;
    private Long vehicleCategoryId;
    private String pickupLocation;
    private String note;
    private String description;
    private String startTime;
    private String endTime;
    // No totalPrice: the server prices the line from the package (quoteTourPackage). A client
    // number was the last figure still in the browser's hands, and it could disagree with the
    // price_list by any amount — server-priced only.
    /** Applied server-side against the server's own price; the browser's discountAmount is not read. */
    private String discountCode;
}
