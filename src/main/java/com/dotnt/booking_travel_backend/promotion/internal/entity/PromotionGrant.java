package com.dotnt.booking_travel_backend.promotion.internal.entity;

import com.dotnt.booking_travel_backend.shared.enums.GrantReason;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_grants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "customer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "unique_code", length = 50, unique = true)
    private String uniqueCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "granted_reason", length = 30, nullable = false)
    private GrantReason grantedReason;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_order_id")
    private Long usedOrderId;
}
