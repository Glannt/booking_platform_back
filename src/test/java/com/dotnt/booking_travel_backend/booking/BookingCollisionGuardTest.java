package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import com.dotnt.booking_travel_backend.booking.api.UpdateBookingRequest;
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
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingEmailBuilder;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCollisionGuardTest {

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

    private Booking existingBooking;
    private Booking targetBooking;

    @BeforeEach
    void setUp() {
        // Existing confirmed booking assigned to vehicle 79A-123.45 from Sept 10 to Sept 15
        BookingItem existingItem = BookingItem.builder()
                .selfDriveBookingDetailId(101L)
                .build();
        existingBooking = Booking.builder()
                .bookingCode("BK-20260901-001")
                .bookingStatus(BookingStatus.confirmed)
                .assignedVehicle("79A-123.45")
                .items(new ArrayList<>(List.of(existingItem)))
                .build();
        existingBooking.setId(1L);

        // Target booking to update: dates from Sept 12 to Sept 16 (overlaps Sept 10 - Sept 15)
        BookingItem targetItem = BookingItem.builder()
                .selfDriveBookingDetailId(102L)
                .build();
        targetBooking = Booking.builder()
                .bookingCode("BK-20260901-002")
                .bookingStatus(BookingStatus.pending)
                .items(new ArrayList<>(List.of(targetItem)))
                .build();
        targetBooking.setId(2L);

        lenient().when(selfDriveFacade.getSelfDriveBookingDates(101L))
                .thenReturn(new LocalDate[]{LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15)});
        lenient().when(selfDriveFacade.getSelfDriveBookingDates(102L))
                .thenReturn(new LocalDate[]{LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 16)});
    }

    @Test
    void whenAssigningSameVehicleToOverlappingBooking_throwsIllegalArgumentException() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(targetBooking));
        when(bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(existingBooking, targetBooking));

        UpdateBookingRequest request = UpdateBookingRequest.builder()
                .assignedVehicle("79A-123.45")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.updateBooking(2L, request)
        );

        assertTrue(ex.getMessage().contains("79A-123.45"));
        assertTrue(ex.getMessage().contains("BK-20260901-001"));
        assertTrue(ex.getMessage().contains("Không thể phân công trùng lịch"));
    }

    @Test
    void whenExistingBookingIsCancelled_assignmentSucceeds() {
        existingBooking.setBookingStatus(BookingStatus.cancelled);

        when(bookingRepository.findById(2L)).thenReturn(Optional.of(targetBooking));
        when(bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(existingBooking, targetBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(targetBooking);
        when(bookingMapper.toDto(targetBooking)).thenReturn(new BookingDto());

        UpdateBookingRequest request = UpdateBookingRequest.builder()
                .assignedVehicle("79A-123.45")
                .build();

        assertDoesNotThrow(() -> bookingService.updateBooking(2L, request));
    }

    @Test
    void whenDatesDoNotOverlap_assignmentSucceeds() {
        // Change target booking dates to Sept 16 to Sept 20 (no overlap with Sept 10 - Sept 15)
        when(selfDriveFacade.getSelfDriveBookingDates(102L))
                .thenReturn(new LocalDate[]{LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 20)});

        when(bookingRepository.findById(2L)).thenReturn(Optional.of(targetBooking));
        when(bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(existingBooking, targetBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(targetBooking);
        when(bookingMapper.toDto(targetBooking)).thenReturn(new BookingDto());

        UpdateBookingRequest request = UpdateBookingRequest.builder()
                .assignedVehicle("79A-123.45")
                .build();

        assertDoesNotThrow(() -> bookingService.updateBooking(2L, request));
    }
}
