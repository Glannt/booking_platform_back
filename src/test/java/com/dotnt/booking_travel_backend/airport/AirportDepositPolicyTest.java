package com.dotnt.booking_travel_backend.airport;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportRoute;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportBookingDetailRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportRouteRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTransferPriceRepository;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The confirm page applies a two-level deposit lookup — transfer price row, then the route it
 * travels, then nothing at all. This is the server's copy of the same lookup, so the two cannot
 * drift, and so the deposit is read from the same row the price came off.
 *
 * <p>The middle case is the one that used to be unreachable: a row whose deposit columns were
 * never written has to be passed over, not mistaken for a configured 20%. {@code
 * AirportRouteDepositReadBackTest} pins that Hibernate reads it back unconfigured; this pins that
 * the caller then falls through. A service nobody configured a deposit for books with none.
 */
@ExtendWith(MockitoExtension.class)
class AirportDepositPolicyTest {

    private static final long ROUTE_ID = 3L;
    private static final long CATEGORY_ID = 5L;

    @Mock
    private AirportTransferPriceRepository airportTransferPriceRepository;
    @Mock
    private AirportBookingDetailRepository airportBookingDetailRepository;
    @Mock
    private SurchargeService surchargeService;
    @Mock
    private AirportRouteRepository airportRouteRepository;
    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportFacade airportFacade;

    /** A policy whose columns were all left null — the "never configured" reading. */
    private static DepositPolicy unconfigured() {
        return DepositPolicy.builder().type(null).rate(null).amount(null).build();
    }

    private void rowReturns(DepositPolicy policy) {
        when(airportTransferPriceRepository.findByAirportRouteIdAndVehicleCategoryId(ROUTE_ID, CATEGORY_ID))
                .thenReturn(Optional.of(AirportTransferPrice.builder().depositPolicy(policy).build()));
    }

    private void routeReturns(DepositPolicy policy) {
        when(airportRouteRepository.findById(ROUTE_ID))
                .thenReturn(Optional.of(AirportRoute.builder().code("R").active(true).depositPolicy(policy).build()));
    }

    @Test
    void aPolicyOnThePricedRowWins() {
        rowReturns(DepositPolicy.builder().type("PERCENT").rate(new BigDecimal("15")).build());

        DepositPolicy resolved = airportFacade.resolveDepositPolicy(ROUTE_ID, CATEGORY_ID);

        assertNotNull(resolved);
        assertEquals("PERCENT", resolved.getType());
        assertEquals(0, new BigDecimal("15").compareTo(resolved.getRate()));
        verifyNoInteractions(airportRouteRepository);
    }

    @Test
    void aRowThatWasNeverConfiguredFallsThroughToItsRoute() {
        rowReturns(unconfigured());
        routeReturns(DepositPolicy.builder().type("FIXED").amount(new BigDecimal("500000")).build());

        DepositPolicy resolved = airportFacade.resolveDepositPolicy(ROUTE_ID, CATEGORY_ID);

        assertNotNull(resolved, "an untouched row must not block the level below it");
        assertEquals("FIXED", resolved.getType());
        assertEquals(0, new BigDecimal("500000").compareTo(resolved.getAmount()));
    }

    @Test
    void neitherLevelConfiguredMeansNoPolicyAtAll() {
        rowReturns(unconfigured());
        routeReturns(unconfigured());

        assertNull(airportFacade.resolveDepositPolicy(ROUTE_ID, CATEGORY_ID),
                "with neither level configured there is no policy — and therefore no deposit");
    }
}
