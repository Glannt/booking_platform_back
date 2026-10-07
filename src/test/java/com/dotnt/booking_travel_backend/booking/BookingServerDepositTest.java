package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingItemRequest;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.api.CreateSelfDriveBookingRequest;
import com.dotnt.booking_travel_backend.booking.api.CreateTourBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingServiceImpl;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * The deposit is decided by the rows the booking was priced from, not by the request that
 * arrived. Before this, {@code createBooking} read {@code request.getDepositType/Rate/Amount},
 * so whoever booked could ask for a deposit of one đồng — the price was already server-trusted
 * by then and the deposit was the last figure still in the browser's hands.
 *
 * <p>The first two tests are the whole point: whatever a stored policy says is what the customer
 * pays upfront, and a policy that was never configured has no deposit at all — there is no module
 * default to fall back on, so an unconfigured service books at 0 rather than at a rate nobody
 * chose. The last test pins the contract itself — there is no field left in which a client-chosen
 * deposit could be expressed at all.
 */
@ExtendWith(MockitoExtension.class)
class BookingServerDepositTest {

    private static final long TOUR_ID = 50L;
    private static final long PACKAGE_ID = 10L;
    private static final long SELF_DRIVE_CATEGORY_ID = 7L;

    private static final long SERVER_TOUR_TOTAL = 1_300_000L;
    private static final long SERVER_SELF_DRIVE_TOTAL = 2_400_000L;
    /** The tour's own configured rate. */
    private static final BigDecimal TOUR_CONFIGURED_RATE = new BigDecimal("10");
    /** What a self-drive rental actually stores on this schema: FIXED, no rate. */
    private static final BigDecimal RENTAL_FIXED_DEPOSIT = new BigDecimal("500000");

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingItemRepository bookingItemRepository;
    @Mock
    private com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher moduleEventPublisher;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private TourBookingService tourBookingService;
    @Mock
    private com.dotnt.booking_travel_backend.shared.mailer.MailService mailService;
    @Mock
    private com.dotnt.booking_travel_backend.booking.internal.service.BookingEmailBuilder bookingEmailBuilder;
    @Mock
    private UserFacade userFacade;
    @Mock
    private FleetFacade fleetFacade;
    @Mock
    private AirportFacade airportFacade;
    @Mock
    private IntercityFacade intercityFacade;
    @Mock
    private SelfDriveFacade selfDriveFacade;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final AtomicReference<Booking> savedBooking = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId(1L);
            savedBooking.set(b);
            return b;
        });
        lenient().when(bookingItemRepository.save(any())).thenAnswer(inv -> {
            var i = inv.getArgument(0, com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem.class);
            i.setId(700L);
            return i;
        });
        lenient().when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
    }

    private CreateBookingRequest tourRequest() {
        return CreateBookingRequest.builder()
                .customerName("Nguyen Van A")
                .customerEmail("a@example.com")
                .customerPhone("0900000000")
                .totalAmount(BigDecimal.valueOf(SERVER_TOUR_TOTAL))
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(TOUR_ID)
                        .quantity(2)
                        .dynamicDetails("{\"tourPackageId\":" + PACKAGE_ID + ",\"adultCount\":2}")
                        .build()))
                .build();
    }

    private void givenPricedTour() {
        lenient().when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        lenient().when(tourBookingService.quoteTourPackage(eq(PACKAGE_ID), anyInt(), anyInt(), anyInt()))
                .thenReturn(BigDecimal.valueOf(SERVER_TOUR_TOTAL));
    }

    @Test
    void theStoredPolicyIsWhatTheCustomerPaysUpfront() {
        givenPricedTour();
        when(tourBookingService.resolveDepositPolicyForPackage(PACKAGE_ID))
                .thenReturn(DepositPolicy.builder().type("PERCENT").rate(TOUR_CONFIGURED_RATE).build());

        bookingService.createBooking(tourRequest());

        Booking booking = savedBooking.get();
        assertNotNull(booking, "the booking has to reach the database at all");
        assertEquals(0, TOUR_CONFIGURED_RATE.compareTo(booking.getDepositRate()),
                "the deposit is the package's own configured rate, not a default nobody chose");
        assertEquals(0, new BigDecimal("130000").compareTo(booking.getDepositAmount()),
                "10% of the server-priced 1,300,000");
        assertEquals("PERCENT", booking.getDepositType());
    }

    @Test
    void aSelfDriveRentalsFixedDepositIsStoredAsAFixedDeposit() {
        lenient().when(selfDriveFacade.quoteSelfDriveRental(eq(SELF_DRIVE_CATEGORY_ID), any(), any()))
                .thenReturn(BigDecimal.valueOf(SERVER_SELF_DRIVE_TOTAL));
        when(selfDriveFacade.resolveDepositPolicy(SELF_DRIVE_CATEGORY_ID))
                .thenReturn(DepositPolicy.builder().type("FIXED").amount(RENTAL_FIXED_DEPOSIT).build());

        bookingService.createBooking(CreateBookingRequest.builder()
                .customerName("Nguyen Van A")
                .customerEmail("a@example.com")
                .customerPhone("0900000000")
                .totalAmount(BigDecimal.valueOf(SERVER_SELF_DRIVE_TOTAL))
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(103L)
                        .quantity(3)
                        .dynamicDetails("{\"vehicleCategoryId\":" + SELF_DRIVE_CATEGORY_ID
                                + ",\"startDate\":\"2026-10-10\",\"endDate\":\"2026-10-13\"}")
                        .build()))
                .build());

        Booking booking = savedBooking.get();
        assertEquals("FIXED", booking.getDepositType(),
                "a rental configured as FIXED must not be turned into a percentage by the browser");
        assertEquals(0, RENTAL_FIXED_DEPOSIT.compareTo(booking.getDepositAmount()));
    }

    @Test
    void nothingConfiguredMeansTheBookingHasNoDepositAtAll() {
        givenPricedTour();
        when(tourBookingService.resolveDepositPolicyForPackage(PACKAGE_ID)).thenReturn(null);

        bookingService.createBooking(tourRequest());

        Booking booking = savedBooking.get();
        assertNull(booking.getDepositType(),
                "an unconfigured service has no deposit type: a default would be indistinguishable from a chosen rate");
        assertNull(booking.getDepositRate(),
                "and no rate — the browser, and nobody else, must not end up with a number here");
        assertEquals(0, BigDecimal.ZERO.compareTo(booking.getDepositAmount()),
                "no configured policy means no deposit");
        assertEquals(0, BigDecimal.valueOf(SERVER_TOUR_TOTAL).compareTo(booking.getTotalAmount()),
                "the price itself is unaffected: the whole total is due, just not upfront");
    }

    /**
     * T4.5 removed the fields rather than ignoring them, so this cannot regress by someone
     * quietly re-wiring a getter: the method has to be gone for the test to pass.
     */
    @Test
    void theCreateContractsCannotExpressAClientChosenDeposit() {
        for (Class<?> contract : List.of(CreateBookingRequest.class,
                CreateTourBookingRequest.class,
                CreateSelfDriveBookingRequest.class)) {
            for (String getter : List.of("getDepositType", "getDepositRate", "getDepositAmount")) {
                assertThrows(NoSuchMethodException.class, () -> contract.getMethod(getter),
                        contract.getSimpleName() + "." + getter
                                + " would put the deposit back in the browser's hands");
            }
        }
    }
}
