package com.dotnt.booking_travel_backend.shared.deposit;

import java.math.BigDecimal;

/**
 * Mapper/normalizer for {@link DepositPolicy}. Single source for the previously
 * per-module pattern "value != null ? value : default" and for the RATE -> PERCENT
 * alias the deposit engine already understands.
 */
public final class DepositPolicies {

    private DepositPolicies() {
    }

    /** Builds a policy from DTO values; null type/rate fall back to defaults (create semantics). */
    public static DepositPolicy from(String type, BigDecimal rate, BigDecimal amount) {
        return DepositPolicy.builder()
                .type(normalizeType(type))
                .rate(rate != null ? rate : DepositPolicy.DEFAULT_RATE)
                .amount(amount)
                .build();
    }

    /**
     * Merges DTO values into the current policy, keeping the current value when a DTO
     * field is null (partial-update semantics).
     */
    public static DepositPolicy merge(DepositPolicy current, String type, BigDecimal rate, BigDecimal amount) {
        DepositPolicy base = current != null ? current : new DepositPolicy();
        return DepositPolicy.builder()
                .type(type != null ? normalizeType(type)
                        : base.getType() != null ? base.getType() : DepositPolicy.DEFAULT_TYPE)
                .rate(rate != null ? rate
                        : base.getRate() != null ? base.getRate() : DepositPolicy.DEFAULT_RATE)
                .amount(amount != null ? amount : base.getAmount())
                .build();
    }

    /** Null-safe accessor: never returns null. */
    public static String type(DepositPolicy policy) {
        return policy != null && policy.getType() != null ? policy.getType() : DepositPolicy.DEFAULT_TYPE;
    }

    /** Null-safe accessor: never returns null. */
    public static BigDecimal rate(DepositPolicy policy) {
        return policy != null && policy.getRate() != null ? policy.getRate() : DepositPolicy.DEFAULT_RATE;
    }

    public static BigDecimal amount(DepositPolicy policy) {
        return policy != null ? policy.getAmount() : null;
    }

    private static String normalizeType(String type) {
        if (type == null || type.isBlank()) return DepositPolicy.DEFAULT_TYPE;
        String t = type.trim().toUpperCase();
        if ("RATE".equals(t)) t = DepositPolicy.DEFAULT_TYPE;
        if (!t.equals(DepositEngine.TYPE_PERCENT)
                && !t.equals(DepositEngine.TYPE_FIXED)
                && !t.equals(DepositEngine.TYPE_PER_PERSON)) {
            throw new IllegalArgumentException("Invalid deposit type: " + type);
        }
        return t;
    }
}