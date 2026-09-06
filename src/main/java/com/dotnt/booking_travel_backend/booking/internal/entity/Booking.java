package com.dotnt.booking_travel_backend.booking.internal.entity;

import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"items"})
@ToString(exclude = {"items"})
@Builder
public class Booking extends MasterEntity {

    @Column(name = "booking_code", nullable = false, unique = true, length = 100)
    private String bookingCode;

    @Column(name = "user_id")
    private Long userId; // Loose relation to decouple Booking and User modules

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false, length = 50)
    private String customerPhone;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private BookingStatus bookingStatus; // pending, confirmed, completed, cancelled

    @Column(name = "customer_notes", columnDefinition = "text")
    private String customerNotes;

    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "contact_channel", length = 50)
    private String contactChannel;

    @Column(name = "contact_id", length = 100)
    private String contactId;

    @Column(name = "zalo_id", length = 100)
    private String zaloId;

    @Column(name = "kakao_talk_id", length = 100)
    private String kakaoTalkId;

    @Column(name = "whatsapp", length = 100)
    private String whatsapp;

    @Column(name = "internal_notes", columnDefinition = "text")
    private String internalNotes;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "received_amount", precision = 15, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "assigned_driver", length = 255)
    private String assignedDriver;

    @Column(name = "assigned_vehicle", length = 255)
    private String assignedVehicle;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

}
