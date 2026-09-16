package com.dotnt.booking_travel_backend.promotion.internal.entity;

import com.dotnt.booking_travel_backend.shared.enums.RedemptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_redemptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "promotion_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id")
    private PromotionGrant grant;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "discount_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RedemptionStatus status = RedemptionStatus.APPLIED;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;
}
