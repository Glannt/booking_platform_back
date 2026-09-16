package com.dotnt.booking_travel_backend.promotion.internal.entity;

import com.dotnt.booking_travel_backend.shared.enums.ConditionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 50, nullable = false)
    private ConditionType conditionType;

    @Column(name = "operator", length = 10, nullable = false)
    private String operator;

    @Column(name = "condition_value", length = 255, nullable = false)
    private String conditionValue;
}
