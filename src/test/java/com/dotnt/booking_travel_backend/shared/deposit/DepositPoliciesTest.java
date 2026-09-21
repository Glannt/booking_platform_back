package com.dotnt.booking_travel_backend.shared.deposit;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DepositPoliciesTest {

    @Test
    void fromFallsBackToDefaultsWhenNull() {
        DepositPolicy p = DepositPolicies.from(null, null, null);
        assertEquals("PERCENT", p.getType());
        assertEquals(0, new BigDecimal("20.00").compareTo(p.getRate()));
        assertNull(p.getAmount());
    }

    @Test
    void fromNormalizesAliasAndCase() {
        DepositPolicy p = DepositPolicies.from("rate", new BigDecimal("30"), new BigDecimal("150000"));
        assertEquals("PERCENT", p.getType());
        assertEquals(0, new BigDecimal("30").compareTo(p.getRate()));
        assertEquals(0, new BigDecimal("150000").compareTo(p.getAmount()));
        assertEquals("FIXED", DepositPolicies.from("fixed", null, null).getType());
    }

    @Test
    void mergeKeepsCurrentWhenDtoFieldIsNull() {
        DepositPolicy current = DepositPolicies.from("FIXED", new BigDecimal("15"), new BigDecimal("100000"));
        DepositPolicy merged = DepositPolicies.merge(current, null, new BigDecimal("25"), null);
        assertEquals("FIXED", merged.getType());
        assertEquals(0, new BigDecimal("25").compareTo(merged.getRate()));
        assertEquals(0, new BigDecimal("100000").compareTo(merged.getAmount()));
    }

    @Test
    void accessorsAreNullSafe() {
        assertEquals("PERCENT", DepositPolicies.type(null));
        assertEquals(0, new BigDecimal("20.00").compareTo(DepositPolicies.rate(null)));
        assertNull(DepositPolicies.amount(null));
        assertEquals("PERCENT", DepositPolicies.type(new DepositPolicy()));
        assertEquals(0, new BigDecimal("20.00").compareTo(DepositPolicies.rate(new DepositPolicy())));
    }

    @Test
    void rejectsUnknownTypeOnWrite() {
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.from("PRECENT", null, null));
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.from("custom", null, null));
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.merge(null, "FIXD", null, null));
    }
}