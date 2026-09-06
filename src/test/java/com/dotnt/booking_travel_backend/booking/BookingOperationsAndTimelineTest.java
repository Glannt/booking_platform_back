package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import com.dotnt.booking_travel_backend.booking.api.BookingTimelineDto;
import com.dotnt.booking_travel_backend.booking.api.UpdateBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingTimelineEvent;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingTimelineEventRepository;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingEmailBuilder;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingServiceImpl;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.rental.api.RentalFacade;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingOperationsAndTimelineTest {

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
    private RentalFacade rentalFacade;
    @Mock
    private BookingTimelineEventRepository bookingTimelineEventRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = Booking.builder()
                .bookingCode("BK-20260906-111111")
                .customerName("Nguyễn Văn A")
                .customerEmail("vana@example.com")
                .customerPhone("0912345678")
                .totalAmount(new BigDecimal("2000000"))
                .totalPrice(new BigDecimal("2000000"))
                .bookingStatus(BookingStatus.pending)
                .paymentStatus("UNPAID")
                .paidAmount(BigDecimal.ZERO)
                .depositAmount(BigDecimal.ZERO)
                .receivedAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        sampleBooking.setId(100L);
    }

    @Test
    void testUpdateBooking_PaymentAndDispatchAndTimeline() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(bookingMapper.toDto(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            BigDecimal remaining = b.getTotalAmount().subtract(b.getPaidAmount()).max(BigDecimal.ZERO);
            return BookingDto.builder()
                    .id(b.getId())
                    .bookingCode(b.getBookingCode())
                    .totalAmount(b.getTotalAmount())
                    .depositAmount(b.getDepositAmount())
                    .receivedAmount(b.getReceivedAmount())
                    .paidAmount(b.getPaidAmount())
                    .remainingAmount(remaining)
                    .paymentStatus(b.getPaymentStatus())
                    .assignedDriver(b.getAssignedDriver())
                    .assignedVehicle(b.getAssignedVehicle())
                    .build();
        });

        UpdateBookingRequest request = UpdateBookingRequest.builder()
                .depositAmount(new BigDecimal("500000"))
                .receivedAmount(new BigDecimal("1000000"))
                .assignedDriver("Tài xế Tuấn")
                .assignedVehicle("79A-123.45")
                .paymentStatus("DEPOSITED")
                .build();

        BookingDto result = bookingService.updateBooking(100L, request);

        assertEquals(new BigDecimal("500000"), result.getDepositAmount());
        assertEquals(new BigDecimal("1000000"), result.getReceivedAmount());
        assertEquals(new BigDecimal("1500000"), result.getPaidAmount());
        assertEquals(new BigDecimal("500000"), result.getRemainingAmount());
        assertEquals("DEPOSITED", result.getPaymentStatus());
        assertEquals("Tài xế Tuấn", result.getAssignedDriver());
        assertEquals("79A-123.45", result.getAssignedVehicle());

        // Verify timeline events recorded for payment and dispatch
        ArgumentCaptor<BookingTimelineEvent> captor = ArgumentCaptor.forClass(BookingTimelineEvent.class);
        verify(bookingTimelineEventRepository, atLeast(2)).save(captor.capture());

        List<BookingTimelineEvent> events = captor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> "DISPATCH_UPDATE".equals(e.getEventType())));
        assertTrue(events.stream().anyMatch(e -> "PAYMENT_UPDATE".equals(e.getEventType())));
    }

    @Test
    void testDeleteBooking_SafeguardThrowsWhenMoneyPaid() {
        sampleBooking.setDepositAmount(new BigDecimal("500000"));
        sampleBooking.setPaidAmount(new BigDecimal("500000"));
        sampleBooking.setPaymentStatus("DEPOSITED");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.deleteBooking(100L, "Muốn xóa");
        });

        assertTrue(ex.getMessage().contains("đã ghi nhận dòng tiền"));
        assertNull(sampleBooking.getDeletedAt(), "Booking must NOT be soft deleted when money is deposited");
    }

    @Test
    void testDeleteBooking_SafeguardAllowsWhenNoMoneyAndLogsReason() {
        sampleBooking.setDepositAmount(BigDecimal.ZERO);
        sampleBooking.setReceivedAmount(BigDecimal.ZERO);
        sampleBooking.setPaidAmount(BigDecimal.ZERO);
        sampleBooking.setPaymentStatus("UNPAID");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        bookingService.deleteBooking(100L, "Khách hàng nhập trùng đơn");

        assertNotNull(sampleBooking.getDeletedAt());
        assertEquals("Khách hàng nhập trùng đơn", sampleBooking.getDeletionReason());

        ArgumentCaptor<BookingTimelineEvent> captor = ArgumentCaptor.forClass(BookingTimelineEvent.class);
        verify(bookingTimelineEventRepository).save(captor.capture());
        assertEquals("DELETED", captor.getValue().getEventType());
        assertTrue(captor.getValue().getDescription().contains("Khách hàng nhập trùng đơn"));
    }

    @Test
    void testGetBookingTimeline_ReturnsOrderedEvents() {
        BookingTimelineEvent e1 = BookingTimelineEvent.builder()
                .id(1L)
                .bookingId(100L)
                .eventType("CREATED")
                .title("Tạo đơn")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        BookingTimelineEvent e2 = BookingTimelineEvent.builder()
                .id(2L)
                .bookingId(100L)
                .eventType("PAYMENT_UPDATE")
                .title("Cọc tiền")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(bookingTimelineEventRepository.findByBookingIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of(e1, e2));

        List<BookingTimelineDto> timeline = bookingService.getBookingTimeline(100L);
        assertEquals(2, timeline.size());
        assertEquals("CREATED", timeline.get(0).getEventType());
        assertEquals("PAYMENT_UPDATE", timeline.get(1).getEventType());
    }
}
