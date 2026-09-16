package com.dotnt.booking_travel_backend.promotion.internal.entity;

import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_scopes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "scope_type", "scope_ref_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", length = 50, nullable = false)
    private ScopeType scopeType;

    @Column(name = "scope_ref_id", length = 100)
    private String scopeRefId;
}
