package com.dotnt.booking_travel_backend.airport;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The whole of the "make not-configured detectable" work hangs off one thing Hibernate does and
 * nothing else in this codebase pins down: a route whose deposit columns were never written has
 * to read back as <em>unconfigured</em>, not as a defaulted PERCENT/20%.
 *
 * <p>Everywhere else the defaulting accessors ({@link DepositPolicies#type},
 * {@link DepositPolicies#rate}) hide the difference on purpose, which is why the report behind
 * this phase saw 12 out of 12 airport rows "already at 20%". If Hibernate handed back a policy
 * pre-filled with a default type for an all-null row, {@code AirportFacade} would believe every
 * route was configured, would never fall through to the level below it, and the row-versus-route
 * precedence would be dead code again — the exact state this phase exists to end.
 */
@DataJpaTest
class AirportRouteDepositReadBackTest {

    @Autowired
    private EntityManager em;

    /**
     * {@code @CreatedDate} only fills itself in when JPA auditing is switched on, and the slice
     * this test runs in does not switch it on. The audit column is NOT NULL, so the row is given
     * its timestamp by hand; nothing about the deposit rides on it.
     */
    private AirportRoute persistAndReload(AirportRoute route) {
        route.setCreatedAt(java.time.LocalDateTime.now());
        em.persist(route);
        em.flush();
        em.clear();
        return em.find(AirportRoute.class, route.getId());
    }

    @Test
    void aRouteThatNeverHadADepositReadsBackAsUnconfigured() {
        AirportRoute reloaded = persistAndReload(AirportRoute.builder()
                .code("R-NO-DEPOSIT")
                .active(true)
                .build());

        DepositPolicy policy = reloaded.getDepositPolicy();
        assertNull(DepositPolicies.configured(policy),
                "an untouched deposit must come back unconfigured, not as a rate somebody chose — "
                        + "otherwise the level below this one can never be reached. raw=" + policy);
    }

    @Test
    void aRouteThatDoesHaveADepositStillKeepsIt() {
        AirportRoute reloaded = persistAndReload(AirportRoute.builder()
                .code("R-WITH-DEPOSIT")
                .active(true)
                .depositPolicy(DepositPolicy.builder()
                        .type("PERCENT")
                        .rate(new BigDecimal("10"))
                        .build())
                .build());

        DepositPolicy policy = reloaded.getDepositPolicy();
        assertNotNull(DepositPolicies.configured(policy), "a chosen rate must not be thrown away");
        assertEquals("PERCENT", policy.getType());
        assertEquals(0, new BigDecimal("10").compareTo(policy.getRate()));
    }
}
