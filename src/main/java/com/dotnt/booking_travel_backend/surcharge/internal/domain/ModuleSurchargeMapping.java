package com.dotnt.booking_travel_backend.surcharge.internal.domain;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "module_surcharge_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_module_surcharge_rule", columnNames = {"module_code", "surcharge_rule_id"})
        },
        indexes = {
                @Index(name = "idx_mapping_module_code", columnList = "module_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleSurchargeMapping extends MasterEntity {

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surcharge_rule_id", nullable = false)
    private SurchargeRule surchargeRule;

    @Column(name = "apply_order", nullable = false)
    @Builder.Default
    private Integer applyOrder = 0;

    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private boolean isOptional = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
