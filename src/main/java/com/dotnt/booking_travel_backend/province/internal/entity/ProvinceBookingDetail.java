package com.dotnt.booking_travel_backend.province.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "province_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceBookingDetail extends MasterEntity {
    @Column(name = "booking_item_id", nullable = false)
    private Long bookingItemId;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "itinerary", columnDefinition = "text")
    private String itinerary;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}

