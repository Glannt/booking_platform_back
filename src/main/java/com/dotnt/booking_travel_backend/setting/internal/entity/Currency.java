package com.dotnt.booking_travel_backend.setting.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "currencies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class Currency extends MasterEntity {

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "rate_to_vnd", nullable = false, precision = 20, scale = 6)
    private BigDecimal rateToVnd;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive;

    @OneToMany(mappedBy = "currency", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CurrencyTranslation> translations = new ArrayList<>();
}
