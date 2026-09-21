package com.dotnt.booking_travel_backend.shared.deposit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

/**
 * Persistent deposit policy value object shared across modules that price services
 * (airport, intercity). Maps to the same column names the modules already use
 * (deposit_type, deposit_rate, deposit_amount) so no DB migration is required.
 * Normalization and defaults live in {@link DepositPolicies}.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositPolicy {

    public static final String DEFAULT_TYPE = DepositEngine.TYPE_PERCENT;
    /**
     * Matches DepositEngine defaults for INTERCITY/AIRPORT (both 20%).
     * ponytail: fixed write-time default. Service-type defaults (e.g. TOUR 30.00) are applied at
     * calculation time by DepositEngine.getDefaultRate, not at persistence — pass an explicit rate
     * to DepositPolicies.from/merge when a service needs a different stored default.
     */
    public static final BigDecimal DEFAULT_RATE = new BigDecimal("20.00");

    @Column(name = "deposit_type", length = 20)
    @Builder.Default
    private String type = DEFAULT_TYPE;

    @Column(name = "deposit_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rate = DEFAULT_RATE;

    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal amount;
}