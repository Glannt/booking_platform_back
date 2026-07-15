package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "intercity_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityBookingDetail extends MasterEntity {

    @Column(name = "booking_item_id")
    private Long bookingItemId;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "vehicle_category_id", nullable = false)
    private Long vehicleCategoryId;

    @Column(name = "trip_type", nullable = false, length = 20)
    private String tripType;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "dropoff_address", length = 500)
    private String dropoffAddress;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "return_time")
    private LocalTime returnTime;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "surcharge_amount", precision = 15, scale = 2)
    private BigDecimal surchargeAmount;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "status", length = 30)
    private String status;
}
