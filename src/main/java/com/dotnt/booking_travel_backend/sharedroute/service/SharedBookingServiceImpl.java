package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingRequest;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingResponse;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.*;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedOfficeRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteBookingRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteDirectionRepository;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedBookingServiceImpl implements ISharedBookingService {

    private final SharedRouteBookingRepository bookingRepository;
    private final SharedRouteDirectionRepository directionRepository;
    private final SharedRouteScheduleRepository scheduleRepository;
    private final SharedOfficeRepository officeRepository;

    @Override
    public SharedRouteBookingResponse createBooking(SharedRouteBookingRequest request) {
        if (request.getDirectionId() == null) {
            throw new IllegalArgumentException("Direction ID is required.");
        }
        if (request.getScheduleId() == null) {
            throw new IllegalArgumentException("Schedule ID is required.");
        }
        if (request.getTripDate() == null) {
            throw new IllegalArgumentException("Trip date is required.");
        }

        Long bookingGroupId = System.currentTimeMillis();
        int pax = request.getPassengerCount() != null && request.getPassengerCount() > 0 ? request.getPassengerCount() : 1;

        List<SharedRouteBooking> savedBookings = new ArrayList<>();

        // 1. Process Outbound Booking
        SharedRouteBooking outboundBooking = buildBookingLeg(
                bookingGroupId,
                request.getDirectionId(),
                request.getScheduleId(),
                request.getTripDate(),
                request.getPickupType() != null ? request.getPickupType() : PickupType.DOOR,
                request.getPickupOfficeId(),
                request.getPickupAddress(),
                request.getDropoffType() != null ? request.getDropoffType() : PickupType.DOOR,
                request.getDropoffOfficeId(),
                request.getDropoffAddress(),
                pax,
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getCustomerEmail(),
                request.getNote()
        );
        savedBookings.add(bookingRepository.save(outboundBooking));

        // 2. Process Return Booking (if round-trip)
        if (Boolean.TRUE.equals(request.getIsRoundTrip()) && request.getReturnDirectionId() != null && request.getReturnScheduleId() != null && request.getReturnTripDate() != null) {
            SharedRouteBooking returnBooking = buildBookingLeg(
                    bookingGroupId,
                    request.getReturnDirectionId(),
                    request.getReturnScheduleId(),
                    request.getReturnTripDate(),
                    request.getReturnPickupType() != null ? request.getReturnPickupType() : PickupType.DOOR,
                    request.getReturnPickupOfficeId(),
                    request.getReturnPickupAddress(),
                    request.getReturnDropoffType() != null ? request.getReturnDropoffType() : PickupType.DOOR,
                    request.getReturnDropoffOfficeId(),
                    request.getReturnDropoffAddress(),
                    pax,
                    request.getCustomerName(),
                    request.getCustomerPhone(),
                    request.getCustomerEmail(),
                    request.getNote()
            );
            savedBookings.add(bookingRepository.save(returnBooking));
        }

        return toResponse(bookingGroupId, savedBookings);
    }

    private SharedRouteBooking buildBookingLeg(
            Long bookingGroupId,
            Long directionId,
            Long scheduleId,
            LocalDate tripDate,
            PickupType pickupType,
            Long pickupOfficeId,
            String pickupAddress,
            PickupType dropoffType,
            Long dropoffOfficeId,
            String dropoffAddress,
            int pax,
            String customerName,
            String customerPhone,
            String customerEmail,
            String note
    ) {
        SharedRouteDirection direction = directionRepository.findById(directionId)
                .orElseThrow(() -> new IllegalArgumentException("Direction not found with id: " + directionId));
        SharedRouteSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));

        SharedOffice pickupOffice = null;
        if (pickupType == PickupType.OFFICE && pickupOfficeId != null) {
            pickupOffice = officeRepository.findById(pickupOfficeId).orElse(null);
        }

        SharedOffice dropoffOffice = null;
        if (dropoffType == PickupType.OFFICE && dropoffOfficeId != null) {
            dropoffOffice = officeRepository.findById(dropoffOfficeId).orElse(null);
        }

        BigDecimal unitPrice = direction.getPrice() != null ? direction.getPrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(pax));

        return SharedRouteBooking.builder()
                .direction(direction)
                .schedule(schedule)
                .tripDate(tripDate)
                .pickupType(pickupType)
                .pickupOffice(pickupOffice)
                .pickupAddress(pickupType == PickupType.DOOR ? pickupAddress : null)
                .dropoffType(dropoffType)
                .dropoffOffice(dropoffOffice)
                .dropoffAddress(dropoffType == PickupType.DOOR ? dropoffAddress : null)
                .passengerCount(pax)
                .totalPrice(totalPrice)
                .bookingGroupId(bookingGroupId)
                .status("CONFIRMED")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerEmail(customerEmail)
                .note(note)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SharedRouteBookingResponse getBookingByGroupId(Long bookingGroupId) {
        List<SharedRouteBooking> bookings = bookingRepository.findByBookingGroupId(bookingGroupId);
        if (bookings.isEmpty()) {
            throw new IllegalArgumentException("No bookings found with group id: " + bookingGroupId);
        }
        return toResponse(bookingGroupId, bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedRouteBookingResponse.BookingItemDto> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBookingStatus(Long bookingId, String status) {
        SharedRouteBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    private SharedRouteBookingResponse toResponse(Long bookingGroupId, List<SharedRouteBooking> bookings) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SharedRouteBookingResponse.BookingItemDto> items = new ArrayList<>();
        String customerName = null;
        String customerPhone = null;

        for (SharedRouteBooking b : bookings) {
            if (b.getTotalPrice() != null) {
                totalAmount = totalAmount.add(b.getTotalPrice());
            }
            if (customerName == null) customerName = b.getCustomerName();
            if (customerPhone == null) customerPhone = b.getCustomerPhone();
            items.add(toItemDto(b));
        }

        return SharedRouteBookingResponse.builder()
                .bookingGroupId(bookingGroupId)
                .totalAmount(totalAmount)
                .status("CONFIRMED")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .bookings(items)
                .build();
    }

    private SharedRouteBookingResponse.BookingItemDto toItemDto(SharedRouteBooking b) {
        return SharedRouteBookingResponse.BookingItemDto.builder()
                .id(b.getId())
                .directionId(b.getDirection() != null ? b.getDirection().getId() : null)
                .directionType(b.getDirection() != null && b.getDirection().getDirectionType() != null ? b.getDirection().getDirectionType().name() : null)
                .originCity(b.getDirection() != null ? b.getDirection().getOriginCity() : null)
                .destinationCity(b.getDirection() != null ? b.getDirection().getDestinationCity() : null)
                .scheduleId(b.getSchedule() != null ? b.getSchedule().getId() : null)
                .departureTime(b.getSchedule() != null ? b.getSchedule().getDepartureTime() : null)
                .tripDate(b.getTripDate())
                .pickupType(b.getPickupType())
                .pickupOfficeId(b.getPickupOffice() != null ? b.getPickupOffice().getId() : null)
                .pickupOfficeName(b.getPickupOffice() != null ? b.getPickupOffice().getName() : null)
                .pickupAddress(b.getPickupAddress())
                .dropoffType(b.getDropoffType())
                .dropoffOfficeId(b.getDropoffOffice() != null ? b.getDropoffOffice().getId() : null)
                .dropoffOfficeName(b.getDropoffOffice() != null ? b.getDropoffOffice().getName() : null)
                .dropoffAddress(b.getDropoffAddress())
                .passengerCount(b.getPassengerCount())
                .unitPrice(b.getDirection() != null ? b.getDirection().getPrice() : null)
                .totalPrice(b.getTotalPrice())
                .status(b.getStatus())
                .build();
    }
}
