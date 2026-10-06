package com.dotnt.booking_travel_backend.shared.deposit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

/**
 * Persistent deposit policy value object shared across modules that price services
 * (airport, intercity). Maps to the same column names the modules already use
 * (deposit_type, deposit_rate, deposit_amount) so no DB migration is required.
 * Normalization lives in {@link DepositPolicies}.
 *
 * <p>All three columns are nullable and stay null when nobody configured anything — there is no
 * default type and no default rate. A null {@code type} is how "no deposit" is expressed, so it
 * must never be filled in on the way out.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositPolicy {

    @Column(name = "deposit_type", length = 20)
    private String type;

    @Column(name = "deposit_rate", precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal amount;
}