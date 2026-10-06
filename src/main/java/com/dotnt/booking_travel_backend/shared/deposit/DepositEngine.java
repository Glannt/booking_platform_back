package com.dotnt.booking_travel_backend.shared.deposit;

import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DepositEngine {

    private static final Logger log = LoggerFactory.getLogger(DepositEngine.class);

    public static final String TYPE_PERCENT = "PERCENT";
    public static final String TYPE_FIXED = "FIXED";

    public static BigDecimal roundToThousand(BigDecimal val) {
        if (val == null) return BigDecimal.ZERO;
        BigDecimal thousand = new BigDecimal("1000");
        return val.divide(thousand, 0, RoundingMode.HALF_UP).multiply(thousand);
    }

    /**
     * Calculates the deposit the customer pays upfront, from the policy stored on the rows the
     * booking was priced from.
     *
     * <p>There is no module default and no fallback rate. A row with no type, a PERCENT row with
     * no usable rate and a FIXED row with no amount are all the same fact — nobody configured a
     * deposit, so the deposit is {@code 0}. Substituting a default would make an unconfigured
     * service indistinguishable from one the business actually chose, both to the customer and in
     * the ledger, and the two used to disagree anyway (DTO said 20%, booking charged 30%).
     *
     * <p>{@code PERCENT} is a percentage of the booking total <em>after</em> any discount, so the
     * customer's deposit is a share of what they will really pay.
     */
    public static DepositCalculationResult calculateDeposit(DepositCalculationRequest req) {
        BigDecimal total = positive(req.getTotalAmount());
        String type = normalizeType(req.getDepositType());

        if (type == null) {
            return noDeposit(null, total);
        }
        if (!TYPE_PERCENT.equals(type) && !TYPE_FIXED.equals(type)) {
            // A type this engine does not know was never written through DepositPolicies, which
            // rejects it on the way in. Refusing to invent a rate for it, but keeping the value on
            // the booking so the bad row stays visible instead of being overwritten with null.
            log.warn("Unknown deposit type '{}' — no deposit applied", type);
            return noDeposit(type, total);
        }

        BigDecimal deposit;
        BigDecimal effectiveRate;

        if (TYPE_FIXED.equals(type)) {
            BigDecimal configured = req.getFixedDepositAmount();
            deposit = configured != null && configured.signum() > 0
                    ? roundToThousand(configured).min(total)
                    : BigDecimal.ZERO;
            effectiveRate = total.signum() > 0
                    ? deposit.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        } else {
            BigDecimal rate = positive(req.getDepositRate());
            effectiveRate = rate;
            deposit = rate.signum() > 0
                    ? roundToThousand(total.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)).min(total)
                    : BigDecimal.ZERO;
        }

        return DepositCalculationResult.builder()
                .depositType(type)
                .depositRate(effectiveRate)
                .depositAmount(deposit)
                .remainingAmount(total.subtract(deposit).max(BigDecimal.ZERO))
                .build();
    }

    private static DepositCalculationResult noDeposit(String type, BigDecimal total) {
        return DepositCalculationResult.builder()
                .depositType(type)
                .depositRate(null)
                .depositAmount(BigDecimal.ZERO)
                .remainingAmount(total)
                .build();
    }

    /** Accepts the legacy {@code RATE} alias; blank/null means "nothing configured". */
    private static String normalizeType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String type = raw.trim().toUpperCase();
        return "RATE".equals(type) ? TYPE_PERCENT : type;
    }

    private static BigDecimal positive(BigDecimal val) {
        return val != null && val.signum() > 0 ? val : BigDecimal.ZERO;
    }
}
