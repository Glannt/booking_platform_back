package com.dotnt.booking_travel_backend.booking.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_items")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class BookingItem extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "tour_booking_detail_id", nullable = true)
    private Long tourBookingDetailId; // Loose relation to decouple Booking and Service modules

    @Column(name = "airport_transfer_booking_detail_id", nullable = true)
    private Long airportTransferBookingDetailId; // Loose relation to decouple Booking and Service modules

    @Column(name = "rent_car_booking_detail_id", nullable = true)
    private Long rentCarBookingDetailId;

    @Column(name = "intercity_booking_detail_id", nullable = true)
    private Long intercityBookingDetailId;

    @Column(name = "self_drive_booking_detail_id", nullable = true)
    private Long selfDriveBookingDetailId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "dynamic_details", columnDefinition = "text")
    private String dynamicDetails; // JSON string

    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "price_snapshot", columnDefinition = "text")
    private String priceSnapshot;

}
