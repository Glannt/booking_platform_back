package com.dotnt.booking_travel_backend.shared.deposit;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DepositPoliciesTest {

    @Test
    void fromKeepsUnconfiguredValuesAsNull() {
        DepositPolicy p = DepositPolicies.from(null, null, null);
        assertNull(p.getType(), "no type means no deposit configured, not a default type");
        assertNull(p.getRate(), "no rate means nobody chose one — it must not be filled in");
        assertNull(p.getAmount());
        assertNull(DepositPolicies.configured(p));
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
    void configuredDistinguishesNeverHadAPolicyFromAnActualOne() {
        assertNull(DepositPolicies.configured(null));
        assertNull(DepositPolicies.configured(DepositPolicies.from(null, null, null)));
        assertNull(DepositPolicies.configured(DepositPolicies.from("", null, null)));
        assertNotNull(DepositPolicies.configured(DepositPolicies.from("PERCENT", new BigDecimal("10"), null)),
                "a chosen rate must not be thrown away");
    }

    @Test
    void amountIsNullSafe() {
        assertNull(DepositPolicies.amount(null));
    }

    @Test
    void rejectsUnknownTypeOnWrite() {
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.from("PRECENT", null, null));
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.from("custom", null, null));
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.merge(null, "FIXD", null, null));
        assertThrows(IllegalArgumentException.class, () -> DepositPolicies.from("PER_PERSON", null, null),
                "PER_PERSON is a pricing type, not a deposit type");
    }
}
