package com.dotnt.booking_travel_backend.shared.deposit;

import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest;
import com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DepositEngine {

    public static final String TYPE_PERCENT = "PERCENT";
    public static final String TYPE_FIXED = "FIXED";
    public static final String TYPE_PER_PERSON = "PER_PERSON";

    public static final BigDecimal DEFAULT_TOUR_RATE = new BigDecimal("30.00");
    public static final BigDecimal DEFAULT_INTERCITY_RATE = new BigDecimal("20.00");
    public static final BigDecimal DEFAULT_AIRPORT_RATE = new BigDecimal("20.00");
    public static final BigDecimal DEFAULT_SELF_DRIVE_RATE = new BigDecimal("30.00");

    public static BigDecimal getDefaultRate(String serviceType) {
        if (serviceType == null) return DEFAULT_INTERCITY_RATE;
        return switch (serviceType.toUpperCase()) {
            case "TOUR", "TOURS" -> DEFAULT_TOUR_RATE;
            case "SELF_DRIVE", "SELFDRIVE" -> DEFAULT_SELF_DRIVE_RATE;
            case "AIRPORT", "AIRPORT_TRANSFER" -> DEFAULT_AIRPORT_RATE;
            default -> DEFAULT_INTERCITY_RATE;
        };
    }

    public static BigDecimal roundToThousand(BigDecimal val) {
        if (val == null) return BigDecimal.ZERO;
        BigDecimal thousand = new BigDecimal("1000");
        return val.divide(thousand, 0, RoundingMode.HALF_UP).multiply(thousand);
    }

    /**
     * Calculates deposit according to depositType (PERCENT, FIXED, or PER_PERSON).
     * Note: PERCENT and FIXED are calculated from the total price.
     */
    public static DepositCalculationResult calculateDeposit(DepositCalculationRequest req) {
        BigDecimal total = req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return DepositCalculationResult.builder()
                    .depositType(req.getDepositType() != null ? req.getDepositType() : TYPE_PERCENT)
                    .depositRate(req.getDepositRate())
                    .depositAmount(BigDecimal.ZERO)
                    .remainingAmount(BigDecimal.ZERO)
                    .description("Không yêu cầu đặt cọc")
                    .build();
        }

        String type = req.getDepositType() != null ? req.getDepositType().toUpperCase() : TYPE_PERCENT;
        // Normalize alias
        if ("RATE".equalsIgnoreCase(type)) {
            type = TYPE_PERCENT;
        }

        BigDecimal calculatedDeposit = BigDecimal.ZERO;
        BigDecimal effectiveRate = req.getDepositRate();
        String desc = "";

        if (TYPE_PER_PERSON.equalsIgnoreCase(type)) {
            int adults = Math.max(0, req.getAdultCount() != null ? req.getAdultCount() : 0);
            int children = Math.max(0, req.getChildCount() != null ? req.getChildCount() : 0);
            BigDecimal adultDep = req.getDepositPerAdult() != null ? req.getDepositPerAdult() : BigDecimal.ZERO;
            BigDecimal childDep = req.getDepositPerChild() != null ? req.getDepositPerChild() : BigDecimal.ZERO;

            BigDecimal perPersonTotal = adultDep.multiply(BigDecimal.valueOf(adults))
                    .add(childDep.multiply(BigDecimal.valueOf(children)));
            
            // If perPerson rates are 0, fallback to PERCENT
            if (perPersonTotal.compareTo(BigDecimal.ZERO) > 0) {
                calculatedDeposit = roundToThousand(perPersonTotal).min(total);
                effectiveRate = total.compareTo(BigDecimal.ZERO) > 0
                        ? calculatedDeposit.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                desc = String.format("Cọc vé: %d vé người lớn, %d vé trẻ em", adults, children);
            } else {
                type = TYPE_PERCENT;
                BigDecimal rate = (req.getDepositRate() != null && req.getDepositRate().compareTo(BigDecimal.ZERO) > 0)
                        ? req.getDepositRate()
                        : getDefaultRate(req.getServiceType());
                effectiveRate = rate;
                BigDecimal raw = total.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                calculatedDeposit = roundToThousand(raw).min(total);
                desc = String.format("Đặt cọc %,.0f%%", rate);
            }
        } else if (TYPE_FIXED.equalsIgnoreCase(type)) {
            BigDecimal fixed = req.getFixedDepositAmount() != null ? req.getFixedDepositAmount() : BigDecimal.ZERO;
            calculatedDeposit = roundToThousand(fixed).min(total);
            effectiveRate = total.compareTo(BigDecimal.ZERO) > 0
                    ? calculatedDeposit.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            desc = String.format("Tiền cọc: %,.0f đ", calculatedDeposit);
        } else {
            // Default: PERCENT (calculated from total amount)
            type = TYPE_PERCENT;
            BigDecimal rate = (req.getDepositRate() != null && req.getDepositRate().compareTo(BigDecimal.ZERO) > 0)
                    ? req.getDepositRate()
                    : getDefaultRate(req.getServiceType());
            effectiveRate = rate;
            BigDecimal raw = total.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            calculatedDeposit = roundToThousand(raw).min(total);
            desc = String.format("Đặt cọc %,.0f%%", rate);
        }

        BigDecimal remaining = total.subtract(calculatedDeposit).max(BigDecimal.ZERO);

        return DepositCalculationResult.builder()
                .depositType(type)
                .depositRate(effectiveRate)
                .depositAmount(calculatedDeposit)
                .remainingAmount(remaining)
                .description(desc)
                .build();
    }
}
