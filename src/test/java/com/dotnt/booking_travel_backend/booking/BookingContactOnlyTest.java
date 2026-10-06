package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingItemRequest;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingEmailBuilder;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingServiceImpl;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
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
 * A contact-only tour package has no price by design — it is a quote request. This test pins down
 * what that has to mean at booking time: the line item is stamped so the quote can never be
 * mistaken for revenue later, and the totals are 0đ because a quote with a number in it is a
 * contradiction.
 *
 * <p>The second case deliberately books a <em>priced</em> package through the same code path. The
 * summing of the booking total had to move for the contact-only zeroing to take effect, and a test
 * that only ever exercises the zeroed branch would happily pass even if that move had quietly
 * stopped adding up anything at all.
 */
@ExtendWith(MockitoExtension.class)
class BookingContactOnlyTest {

    private static final long TOUR_ID = 50L;
    private static final long PRICED_PACKAGE_ID = 10L;
    private static final long CONTACT_ONLY_PACKAGE_ID = 30L;
    private static final long UNIT_PRICE = 650_000L;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingItemRepository bookingItemRepository;
    @Mock
    private ModuleEventPublisher moduleEventPublisher;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private TourBookingService tourBookingService;
    @Mock
    private MailService mailService;
    @Mock
    private BookingEmailBuilder bookingEmailBuilder;
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

    /** Holds the entity the service built, for assertions after the call. */
    private final AtomicReference<Booking> savedBooking = new AtomicReference<>();
    private final AtomicReference<BookingItem> savedItem = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId(1L);
            savedBooking.set(b);
            return b;
        });
        when(bookingItemRepository.save(any(BookingItem.class))).thenAnswer(inv -> {
            BookingItem i = inv.getArgument(0);
            i.setId(700L);
            savedItem.set(i);
            return i;
        });
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
        when(tourBookingService.isTour(TOUR_ID)).thenReturn(true);
        lenient().when(tourBookingService.createTourBookingDetail(
                isNull(), eq(TOUR_ID), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(900L);
    }

    private CreateBookingRequest request(long packageId, long clientSuppliedUnitPrice) {
        return CreateBookingRequest.builder()
                .customerName("Nguyen Van A")
                .customerEmail("a@example.com")
                .customerPhone("0900000000")
                .totalAmount(BigDecimal.valueOf(clientSuppliedUnitPrice))
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(TOUR_ID)
                        .quantity(2)
                        .unitPrice(BigDecimal.valueOf(clientSuppliedUnitPrice))
                        .dynamicDetails("{\"tourPackageId\":" + packageId + "}")
                        .build()))
                .build();
    }

    @Test
    void contactOnlyPackageIsStampedAndTotalsZeroedRegardlessOfWhatTheClientSent() {
        when(tourBookingService.isContactOnlyPackage(CONTACT_ONLY_PACKAGE_ID)).thenReturn(true);

        // The client claims 1.300.000đ for a package that has no price at all.
        BookingDto dto = bookingService.createBooking(request(CONTACT_ONLY_PACKAGE_ID, UNIT_PRICE * 2));

        assertNotNull(dto);

        BookingItem item = savedItem.get();
        assertTrue(Boolean.TRUE.equals(item.getIsContactOnly()),
                "the row must record that it is a quote, or revenue later reads it as a sale");
        assertEquals(BigDecimal.ZERO, item.getUnitPrice());
        assertEquals(BigDecimal.ZERO, item.getTotalPrice());

        Booking booking = savedBooking.get();
        assertEquals(BigDecimal.ZERO, booking.getTotalAmount(),
                "a quote must not book at a figure nobody set");
        assertEquals(BigDecimal.ZERO, booking.getTotalPrice());
        assertEquals(BigDecimal.ZERO, booking.getDepositAmount(),
                "deposit follows the total, so a 0đ quote cannot ask for money up front");
    }

    @Test
    void pricedPackageKeepsTheClientPriceAndIsNotStamped() {
        when(tourBookingService.isContactOnlyPackage(PRICED_PACKAGE_ID)).thenReturn(false);

        bookingService.createBooking(request(PRICED_PACKAGE_ID, UNIT_PRICE));

        BookingItem item = savedItem.get();
        assertFalse(Boolean.TRUE.equals(item.getIsContactOnly()),
                "contact-only must be the exception, not a blanket stamp on every tour booking");
        assertEquals(BigDecimal.valueOf(UNIT_PRICE), item.getUnitPrice());
        assertEquals(BigDecimal.valueOf(UNIT_PRICE), item.getTotalPrice());

        Booking booking = savedBooking.get();
        assertEquals(BigDecimal.valueOf(UNIT_PRICE), booking.getTotalAmount(),
                "the total has to be summed per item — this is the code the zeroing change had to "
                        + "keep right, so a regression reads as 0 instead of the correct figure");
        assertEquals(BigDecimal.valueOf(UNIT_PRICE), booking.getTotalPrice());
    }
}
