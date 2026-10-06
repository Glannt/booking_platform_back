package com.dotnt.booking_travel_backend.booking;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingItemRequest;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.api.ServerPriceMode;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingServiceImpl;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * The browser's price is a cross-check, not an instruction. Whichever of the three priced modules
 * builds the line — tour, self-drive, airport — the figure that reaches the database has to be the
 * one the server computed, whatever number was sent, and the gap has to be logged with enough of
 * both numbers to be sized against real traffic.
 *
 * <p>The last two tests pin down the other half of that contract: when the server has nothing to
 * price the line with, WARN keeps the booking alive on the client's figure so nothing is lost while
 * the gap is being measured, and ENFORCE refuses to sell an unpriced line instead of defaulting it
 * to free.
 */
@ExtendWith(MockitoExtension.class)
class BookingServerPriceTest {

    private static final long TOUR_ID = 50L;
    private static final long PACKAGE_ID = 10L;
    private static final long SELF_DRIVE_CATEGORY_ID = 7L;
    private static final long AIRPORT_ROUTE_ID = 3L;
    private static final long AIRPORT_CATEGORY_ID = 5L;

    private static final long SERVER_TOUR_TOTAL = 1_300_000L;
    private static final long SERVER_SELF_DRIVE_TOTAL = 2_400_000L;
    private static final long SERVER_AIRPORT_TOTAL = 950_000L;
    /** The lie under test: a line the server prices in the millions, claimed at one đồng. */
    private static final long FALSIFIED = 1L;

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
    private final AtomicReference<BookingItem> savedItem = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId(1L);
            savedBooking.set(b);
            return b;
        });
        lenient().when(bookingItemRepository.save(any(BookingItem.class))).thenAnswer(inv -> {
            BookingItem i = inv.getArgument(0);
            i.setId(700L);
            savedItem.set(i);
            return i;
        });
        lenient().when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
    }

    private CreateBookingRequest request(long serviceId, long clientTotal, String dynamicDetails) {
        return CreateBookingRequest.builder()
                .customerName("Nguyen Van A")
                .customerEmail("a@example.com")
                .customerPhone("0900000000")
                .totalAmount(BigDecimal.valueOf(clientTotal))
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(serviceId)
                        .quantity(2)
                        .unitPrice(BigDecimal.valueOf(clientTotal))
                        .dynamicDetails(dynamicDetails)
                        .build()))
                .build();
    }

    private void assertServerFigureWon(long serverTotal) {
        BookingItem item = savedItem.get();
        assertNotNull(item, "the line has to reach the database at all");
        assertEquals(BigDecimal.valueOf(serverTotal), item.getUnitPrice(),
                "the line price is the server's, not the browser's");
        assertEquals(BigDecimal.valueOf(serverTotal), item.getTotalPrice());

        Booking booking = savedBooking.get();
        assertEquals(BigDecimal.valueOf(serverTotal), booking.getTotalAmount(),
                "the booking total is rebuilt from the server-priced lines");
        assertEquals(BigDecimal.valueOf(serverTotal), booking.getTotalPrice());
    }

    @Test
    void tourLineStoresTheServerPriceWhateverTheBrowserSent() {
        lenient().when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        when(tourBookingService.quoteTourPackage(eq(PACKAGE_ID), anyInt(), anyInt(), anyInt()))
                .thenReturn(BigDecimal.valueOf(SERVER_TOUR_TOTAL));

        bookingService.createBooking(request(TOUR_ID, FALSIFIED,
                "{\"tourPackageId\":" + PACKAGE_ID + ",\"adultCount\":2}"));

        assertServerFigureWon(SERVER_TOUR_TOTAL);
    }

    @Test
    void selfDriveLineStoresTheServerPriceWhateverTheBrowserSent() {
        when(selfDriveFacade.quoteSelfDriveRental(eq(SELF_DRIVE_CATEGORY_ID), any(), any()))
                .thenReturn(BigDecimal.valueOf(SERVER_SELF_DRIVE_TOTAL));

        bookingService.createBooking(request(103L, FALSIFIED,
                "{\"vehicleCategoryId\":" + SELF_DRIVE_CATEGORY_ID
                        + ",\"startDate\":\"2026-10-10\",\"endDate\":\"2026-10-13\"}"));

        assertServerFigureWon(SERVER_SELF_DRIVE_TOTAL);
    }

    @Test
    void airportLineStoresTheServerPriceWhateverTheBrowserSent() {
        when(airportFacade.quoteAirportTransfer(eq(AIRPORT_CATEGORY_ID), eq(AIRPORT_ROUTE_ID), any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(SERVER_AIRPORT_TOTAL));

        bookingService.createBooking(request(101L, FALSIFIED,
                "{\"routeId\":" + AIRPORT_ROUTE_ID + ",\"vehicleCategoryId\":" + AIRPORT_CATEGORY_ID
                        + ",\"tripType\":\"ONE_WAY\"}"));

        assertServerFigureWon(SERVER_AIRPORT_TOTAL);
    }

    @Test
    void theMismatchIsLoggedWithBothFiguresAndTheBookingId() {
        lenient().when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        when(tourBookingService.quoteTourPackage(eq(PACKAGE_ID), anyInt(), anyInt(), anyInt()))
                .thenReturn(BigDecimal.valueOf(SERVER_TOUR_TOTAL));

        Logger logger = (Logger) LoggerFactory.getLogger(BookingServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            bookingService.createBooking(request(TOUR_ID, FALSIFIED,
                    "{\"tourPackageId\":" + PACKAGE_ID + ",\"adultCount\":2}"));
        } finally {
            logger.detachAppender(appender);
        }

        String logged = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(m -> m.contains("Price mismatch"))
                .findFirst()
                .orElse(null);
        assertNotNull(logged, "a disagreement nobody can see afterwards cannot be sized");
        assertTrue(logged.contains(String.valueOf(FALSIFIED)), "the browser's figure must be in the log: " + logged);
        assertTrue(logged.contains(String.valueOf(SERVER_TOUR_TOTAL)), "the server's figure must be in the log: " + logged);
        assertTrue(logged.contains("1"), "the booking id must be in the log: " + logged);
    }

    @Test
    void anUnpriceableLineKeepsTheClientFigureInWarnMode() {
        lenient().when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        when(tourBookingService.quoteTourPackage(eq(PACKAGE_ID), anyInt(), anyInt(), anyInt()))
                .thenReturn(null);

        bookingService.createBooking(request(TOUR_ID, SERVER_TOUR_TOTAL,
                "{\"tourPackageId\":" + PACKAGE_ID + ",\"adultCount\":2}"));

        assertEquals(BigDecimal.valueOf(SERVER_TOUR_TOTAL), savedItem.get().getTotalPrice(),
                "WARN exists precisely so a hole in the price list does not cost a booking while it is being measured");
    }

    @Test
    void anUnpriceableLineIsRefusedInEnforceMode() {
        lenient().when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        when(tourBookingService.quoteTourPackage(eq(PACKAGE_ID), anyInt(), anyInt(), anyInt()))
                .thenReturn(null);
        ReflectionTestUtils.setField(bookingService, "serverPriceMode", ServerPriceMode.ENFORCE);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request(TOUR_ID, FALSIFIED,
                        "{\"tourPackageId\":" + PACKAGE_ID + ",\"adultCount\":2}")));

        assertTrue(thrown.getMessage().contains("No server-side price"), thrown.getMessage());
        assertNull(savedItem.get(), "a refused line must not leave a half-written row behind");
    }
}
