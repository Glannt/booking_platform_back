package com.dotnt.booking_travel_backend.shared.deposit;

import java.math.BigDecimal;

/**
 * Mapper/normalizer for {@link DepositPolicy}. Single source for the RATE -> PERCENT
 * alias the deposit engine already understands.
 */
public final class DepositPolicies {

    private DepositPolicies() {
    }

    /**
     * Builds a policy from DTO values. A null or blank type means the caller configured nothing,
     * so the policy stays null-typed rather than being filled with a default rate — an
     * unconfigured service must book at 0, not at some rate nobody chose.
     */
    public static DepositPolicy from(String type, BigDecimal rate, BigDecimal amount) {
        return DepositPolicy.builder()
                .type(normalizeType(type))
                .rate(rate)
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
                .type(type != null ? normalizeType(type) : base.getType())
                .rate(rate != null ? rate : base.getRate())
                .amount(amount != null ? amount : base.getAmount())
                .build();
    }

    public static BigDecimal amount(DepositPolicy policy) {
        return policy != null ? policy.getAmount() : null;
    }

    /**
     * The policy exactly as stored, or {@code null} when the row never had one. This is what the
     * booking flow reads: {@code null} means "no deposit configured", which is 0 — there is no
     * default to substitute.
     */
    public static DepositPolicy configured(DepositPolicy policy) {
        if (policy == null || policy.getType() == null || policy.getType().isBlank()) return null;
        return policy;
    }

    /** @return the normalized type, or {@code null} when nothing is configured. */
    private static String normalizeType(String type) {
        if (type == null || type.isBlank()) return null;
        String t = type.trim().toUpperCase();
        if ("RATE".equals(t)) t = DepositEngine.TYPE_PERCENT;
        if (!t.equals(DepositEngine.TYPE_PERCENT) && !t.equals(DepositEngine.TYPE_FIXED)) {
            throw new IllegalArgumentException("Invalid deposit type: " + type);
        }
        return t;
    }
}
