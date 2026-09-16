package com.dotnt.booking_travel_backend.sharedroute.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "shared_route_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteBooking extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private SharedRouteDirection direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private SharedRouteSchedule schedule;

    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pickup_type", nullable = false, length = 20)
    private PickupType pickupType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_office_id")
    private SharedOffice pickupOffice;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "dropoff_type", nullable = false, length = 20)
    private PickupType dropoffType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropoff_office_id")
    private SharedOffice dropoffOffice;

    @Column(name = "dropoff_address", length = 500)
    private String dropoffAddress;

    @Column(name = "passenger_count", nullable = false)
    @Builder.Default
    private Integer passengerCount = 1;

    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "booking_group_id")
    private Long bookingGroupId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "customer_phone", length = 50)
    private String customerPhone;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
