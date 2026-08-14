package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "airport_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportBookingDetail extends MasterEntity {
    @Column(name = "booking_item_id", nullable = true)
    private Long bookingItemId;

    // ── Trip type & references ──
    @Column(name = "trip_type", length = 20)
    private String tripType; // "ONE_WAY" | "ROUND_TRIP"

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    // ── Outbound (chiều đi) ──
    @Column(name = "outbound_direction", length = 20)
    private String outboundDirection; // "TO_AIRPORT" | "FROM_AIRPORT"

    @Column(name = "outbound_date")
    private LocalDate outboundDate;

    @Column(name = "outbound_pickup_time")
    private LocalDateTime outboundPickupTime;

    @Column(name = "outbound_flight_number", length = 50)
    private String outboundFlightNumber;

    @Column(name = "outbound_terminal", length = 50)
    private String outboundTerminal;

    @Column(name = "outbound_landing_time")
    private LocalDateTime outboundLandingTime;

    @Column(name = "outbound_pickup_address", length = 500)
    private String outboundPickupAddress;

    @Column(name = "outbound_dropoff_address", length = 500)
    private String outboundDropoffAddress;

    // ── Return (chiều về) — nullable khi ONE_WAY ──
    @Column(name = "return_direction", length = 20)
    private String returnDirection; // "TO_AIRPORT" | "FROM_AIRPORT"

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "return_pickup_time")
    private LocalDateTime returnPickupTime;

    @Column(name = "return_flight_number", length = 50)
    private String returnFlightNumber;

    @Column(name = "return_terminal", length = 50)
    private String returnTerminal;

    @Column(name = "return_landing_time")
    private LocalDateTime returnLandingTime;

    @Column(name = "return_pickup_address", length = 500)
    private String returnPickupAddress;

    @Column(name = "return_dropoff_address", length = 500)
    private String returnDropoffAddress;

    // ── Pricing ──
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "surcharge_amount", precision = 15, scale = 2)
    private BigDecimal surchargeAmount;

    public BigDecimal getNightSurcharge() {
        return surchargeAmount;
    }

    public void setNightSurcharge(BigDecimal nightSurcharge) {
        this.surchargeAmount = nightSurcharge;
    }

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", length = 10)
    private String currency;

    // ── Other ──
    @Column(name = "passenger")
    private Integer passenger;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}

