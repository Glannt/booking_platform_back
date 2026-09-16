package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "self_drive_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveBookingDetail extends MasterEntity {

    @Column(name = "booking_item_id")
    private Long bookingItemId;

    @Column(name = "vehicle_category_id", nullable = false)
    private Long vehicleCategoryId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "rental_days", nullable = false)
    private Integer rentalDays;

    @Column(name = "pickup_location", length = 500)
    private String pickupLocation;

    @Column(name = "dropoff_location", length = 500)
    private String dropoffLocation;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "deposit", precision = 15, scale = 2)
    private BigDecimal deposit;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "id_card_image_url", length = 500)
    private String idCardImageUrl;

    @Column(name = "driver_license_image_url", length = 500)
    private String driverLicenseImageUrl;

    @Column(name = "contract_image_url", length = 500)
    private String contractImageUrl;

    @Column(name = "deposit_status", length = 30)
    private String depositStatus;

    @Column(name = "status", length = 30)
    private String status;
}
