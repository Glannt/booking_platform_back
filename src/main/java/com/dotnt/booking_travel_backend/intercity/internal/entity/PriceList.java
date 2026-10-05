package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "price_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceList extends MasterEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "price_type_id", nullable = false)
    private PriceType priceType;

    @Column(name = "scope_type", length = 50, nullable = false)
    @Builder.Default
    private String scopeType = "INTERCITY_ROUTE";

    @Column(name = "scope_ref_id", length = 100, nullable = false)
    private String scopeRefId;

    @Column(name = "scope_meta", columnDefinition = "TEXT")
    private String scopeMeta;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "base_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "effective_from", nullable = false)
    @Builder.Default
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Deposit is stored per price row, so ONE_WAY / ROUND_TRIP_SAME_DAY / DAILY_RATE each carry
     * their own. Left null when unconfigured on purpose — never defaulted to 20% at write time,
     * because that would make "never configured" indistinguishable from "explicitly 20%".
     */
    @Embedded
    private DepositPolicy depositPolicy;

    @OneToMany(mappedBy = "priceList", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PriceListTranslation> translations = new ArrayList<>();

    @OneToOne(mappedBy = "priceList", cascade = CascadeType.ALL, orphanRemoval = true)
    private PriceListRoute routeDetails;

    @OneToOne(mappedBy = "priceList", cascade = CascadeType.ALL, orphanRemoval = true)
    private PriceListDailyRate dailyRateDetails;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public void setActive(boolean active) {
        this.status = active ? "ACTIVE" : "INACTIVE";
    }
}

