package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.api.*;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.airport.internal.entity.AirportBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.resilience.annotation.ConcurrencyLimit;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service("bookingService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ModuleEventPublisher moduleEventPublisher;
    private final BookingMapper bookingMapper;
    private final TourBookingService tourBookingService;
    private final MailService mailService;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    @ConcurrencyLimit(20)
    public BookingDto createBooking(CreateBookingRequest request) {
        // Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingBooking.isPresent()) {
                log.info("Duplicate booking request detected with idempotency key: {}", request.getIdempotencyKey());
                return bookingMapper.toDto(existingBooking.get());
            }
        }

        String bookingCode = generateBookingCode();

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(request.getUserId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .contactChannel(request.getContactChannel())
                .contactId(request.getContactId())
                .idempotencyKey(request.getIdempotencyKey())
                .totalAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .bookingStatus(BookingStatus.pending)
                .customerNotes(request.getCustomerNotes())
                .items(new ArrayList<>())
                .build();

        booking = bookingRepository.save(booking);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                int quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                BigDecimal itemUnitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal itemTotalPrice = itemUnitPrice;
                calculatedTotal = calculatedTotal.add(itemTotalPrice);

                Long tourBookingDetailId = null;
                Long airportTransferBookingDetailId = null;
                Long intercityBookingDetailId = null;
                Long selfDriveBookingDetailId = null;
                boolean isTour = tourBookingService.isTour(itemReq.getServiceId());
                boolean isAirport = moduleEventPublisher.publishRequest("airport.transferPrice.existsById", itemReq.getServiceId(), Boolean.class)
                        || (itemReq.getServiceId() != null && itemReq.getServiceId().equals(101L));
                boolean isIntercity = itemReq.getServiceId() != null && itemReq.getServiceId().equals(102L);
                boolean isSelfDrive = itemReq.getServiceId() != null && itemReq.getServiceId().equals(103L);

                if (isTour) {
                    if (itemReq.getServiceOptionId() != null) {
                        VehicleCategory category = moduleEventPublisher.publishRequest("fleet.vehicleCategory.findById", itemReq.getServiceOptionId(), VehicleCategory.class);
                        if (category != null && category.getMaxSeat() != null && quantity > category.getMaxSeat()) {
                            throw new IllegalArgumentException("The number of guests (" + quantity + ") exceeds the vehicle's maximum seat capacity (" + category.getMaxSeat() + " seats)!");
                        }
                    }

                    java.time.Instant startTime = itemReq.getStartDate() != null
                            ? itemReq.getStartDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;
                    java.time.Instant endTime = itemReq.getEndDate() != null
                            ? itemReq.getEndDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;

                    tourBookingDetailId = tourBookingService.createTourBookingDetail(
                            null,
                            itemReq.getServiceId(),
                            quantity,
                            itemReq.getPickupLocation(),
                            request.getCustomerNotes(),
                            startTime,
                            endTime
                     );
                } else if (isAirport) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                        }
                    } catch (Exception ignored) {}

                    String tripType = ad.getOrDefault("tripType", "ONE_WAY").toString();
                    String outboundDirection = ad.getOrDefault("outboundDirection", "FROM_AIRPORT").toString();

                    java.time.LocalDateTime outboundPickupTime = itemReq.getStartDate();
                    java.time.LocalDate outboundDate = outboundPickupTime != null ? outboundPickupTime.toLocalDate() : null;
                    if (ad.containsKey("outboundDate") && ad.get("outboundDate") != null) {
                        try { outboundDate = java.time.LocalDate.parse(ad.get("outboundDate").toString()); } catch (Exception ignored) {}
                    }

                    String returnDirection = ad.get("returnDirection") != null ? ad.get("returnDirection").toString() : null;
                    java.time.LocalDate returnDate = null;
                    if (ad.containsKey("returnDate") && ad.get("returnDate") != null) {
                        try { returnDate = java.time.LocalDate.parse(ad.get("returnDate").toString()); } catch (Exception ignored) {}
                    }
                    java.time.LocalDateTime returnPickupTime = null;
                    if (ad.containsKey("returnPickupTime") && ad.get("returnPickupTime") != null) {
                        try { returnPickupTime = java.time.LocalDateTime.parse(ad.get("returnPickupTime").toString()); } catch (Exception ignored) {}
                    }

                    if (returnPickupTime == null && itemReq.getEndDate() != null && "ROUND_TRIP".equals(tripType)) {
                        returnPickupTime = itemReq.getEndDate();
                    }

                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try { vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString()); } catch (Exception ignored) {}
                    }
                    if (vehicleCategoryId == null && itemReq.getServiceId() != null) {
                        vehicleCategoryId = itemReq.getServiceId();
                    }

                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try { basePrice = new java.math.BigDecimal(ad.get("basePrice").toString()); } catch (Exception ignored) {}
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try { totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString()); } catch (Exception ignored) {}
                    }
                    String currency = ad.getOrDefault("currency", "VND").toString();

                    AirportBookingDetail detail = AirportBookingDetail.builder()
                            .tripType(tripType)
                            .routeId(null)
                            .vehicleCategoryId(vehicleCategoryId)
                            .outboundDirection(outboundDirection)
                            .outboundDate(outboundDate)
                            .outboundPickupTime(outboundPickupTime)
                            .outboundFlightNumber(itemReq.getFlightNumber())
                            .outboundPickupAddress(itemReq.getPickupLocation())
                            .outboundDropoffAddress(ad.get("outboundDropoffAddress") != null ? ad.get("outboundDropoffAddress").toString() : null)
                            .returnDirection(returnDirection)
                            .returnDate(returnDate)
                            .returnPickupTime(returnPickupTime)
                            .returnFlightNumber(ad.get("returnFlightNumber") != null ? ad.get("returnFlightNumber").toString() : null)
                            .returnPickupAddress(ad.get("returnPickupAddress") != null ? ad.get("returnPickupAddress").toString() : null)
                            .returnDropoffAddress(ad.get("returnDropoffAddress") != null ? ad.get("returnDropoffAddress").toString() : null)
                            .basePrice(basePrice)
                            .totalPrice(totalPrice)
                            .currency(currency)
                            .passenger(quantity)
                            .note(request.getCustomerNotes())
                            .build();
                    detail = moduleEventPublisher.publishRequest("airport.bookingDetail.save", detail, AirportBookingDetail.class);
                    airportTransferBookingDetailId = detail.getId();
                } else if (isIntercity) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                        }
                    } catch (Exception ignored) {}

                    Long routeId = null;
                    if (ad.containsKey("routeId") && ad.get("routeId") != null) {
                        try { routeId = Long.valueOf(ad.get("routeId").toString()); } catch (Exception ignored) {}
                    }
                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try { vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString()); } catch (Exception ignored) {}
                    }
                    String tripType = ad.getOrDefault("tripType", "ONE_WAY").toString();
                    java.time.LocalDate departureDate = null;
                    if (ad.containsKey("departureDate") && ad.get("departureDate") != null) {
                        try { departureDate = java.time.LocalDate.parse(ad.get("departureDate").toString()); } catch (Exception ignored) {}
                    }
                    java.time.LocalTime departureTime = null;
                    if (ad.containsKey("departureTime") && ad.get("departureTime") != null) {
                        try { departureTime = java.time.LocalTime.parse(ad.get("departureTime").toString()); } catch (Exception ignored) {}
                    }
                    java.time.LocalDate returnDate = null;
                    if (ad.containsKey("returnDate") && ad.get("returnDate") != null) {
                        try { returnDate = java.time.LocalDate.parse(ad.get("returnDate").toString()); } catch (Exception ignored) {}
                    }
                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try { basePrice = new java.math.BigDecimal(ad.get("basePrice").toString()); } catch (Exception ignored) {}
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try { totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString()); } catch (Exception ignored) {}
                    }

                    IntercityBookingDetail detail = IntercityBookingDetail.builder()
                            .routeId(routeId)
                            .vehicleCategoryId(vehicleCategoryId)
                            .tripType(tripType)
                            .pickupAddress(itemReq.getPickupLocation())
                            .dropoffAddress(ad.get("dropoffAddress") != null ? ad.get("dropoffAddress").toString() : null)
                            .departureDate(departureDate)
                            .departureTime(departureTime)
                            .returnDate(returnDate)
                            .passengerCount(quantity)
                            .note(request.getCustomerNotes())
                            .basePrice(basePrice)
                            .surchargeAmount(java.math.BigDecimal.ZERO)
                            .totalPrice(totalPrice)
                            .currency("VND")
                            .status("PENDING")
                            .build();
                    detail = moduleEventPublisher.publishRequest("rental.intercityBookingDetail.save", detail, IntercityBookingDetail.class);
                    intercityBookingDetailId = detail.getId();
                } else if (isSelfDrive) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                        }
                    } catch (Exception ignored) {}

                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try { vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString()); } catch (Exception ignored) {}
                    }
                    java.time.LocalDate startDate = null;
                    if (ad.containsKey("startDate") && ad.get("startDate") != null) {
                        try { startDate = java.time.LocalDate.parse(ad.get("startDate").toString()); } catch (Exception ignored) {}
                    }
                    java.time.LocalDate endDate = null;
                    if (ad.containsKey("endDate") && ad.get("endDate") != null) {
                        try { endDate = java.time.LocalDate.parse(ad.get("endDate").toString()); } catch (Exception ignored) {}
                    }
                    int rentalDays = 1;
                    if (startDate != null && endDate != null) {
                        rentalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    }
                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try { basePrice = new java.math.BigDecimal(ad.get("basePrice").toString()); } catch (Exception ignored) {}
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try { totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString()); } catch (Exception ignored) {}
                    }

                    SelfDriveBookingDetail detail = SelfDriveBookingDetail.builder()
                            .vehicleCategoryId(vehicleCategoryId)
                            .startDate(startDate)
                            .endDate(endDate)
                            .rentalDays(rentalDays)
                            .pickupLocation(itemReq.getPickupLocation())
                            .dropoffLocation(ad.get("dropoffLocation") != null ? ad.get("dropoffLocation").toString() : null)
                            .basePrice(basePrice)
                            .deposit(java.math.BigDecimal.ZERO)
                            .totalPrice(totalPrice)
                            .currency("VND")
                            .note(request.getCustomerNotes())
                            .depositStatus("PENDING")
                            .status("PENDING")
                            .build();
                    detail = moduleEventPublisher.publishRequest("rental.selfDriveBookingDetail.save", detail, SelfDriveBookingDetail.class);
                    selfDriveBookingDetailId = detail.getId();
                } else {
                    // ponytail: service module removed — skip service/option validation for now
                    // com.dotnt.booking_travel_backend.service.internal.entity.Service service = moduleEventPublisher.publishRequest("service.findById", itemReq.getServiceId(), com.dotnt.booking_travel_backend.service.internal.entity.Service.class);
                    // if (service == null) {
                    //     throw new IllegalArgumentException("Service not found with ID: " + itemReq.getServiceId());
                    // }
                    //
                    // ServiceOption option = moduleEventPublisher.publishRequest("service.option.findById", itemReq.getServiceOptionId(), ServiceOption.class);
                    // if (option == null) {
                    //     throw new IllegalArgumentException("Service Option not found with ID: " + itemReq.getServiceOptionId());
                    // }
                    //
                    // if (option.getService() == null || !option.getService().getId().equals(service.getId())) {
                    //     throw new IllegalArgumentException("Service Option ID " + itemReq.getServiceOptionId() + " does not belong to Service ID " + service.getId());
                    // }
                    //
                    // if (option.getSeatCapacity() != null && quantity > option.getSeatCapacity()) {
                    //     throw new IllegalArgumentException("The number of guests (" + quantity + ") exceeds the service option's seat capacity (" + option.getSeatCapacity() + " seats)!");
                    // }
                    //
                    // if (itemUnitPrice.compareTo(BigDecimal.ZERO) == 0 && option.getPrice() != null) {
                    //     itemUnitPrice = option.getPrice();
                    //     itemTotalPrice = itemUnitPrice;
                    // }
                }

                BookingItem item = BookingItem.builder()
                        .booking(booking)
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice)
                        .totalPrice(itemTotalPrice)
                        .dynamicDetails(itemReq.getDynamicDetails())
                        .tourBookingDetailId(tourBookingDetailId)
                        .airportTransferBookingDetailId(airportTransferBookingDetailId)
                        .intercityBookingDetailId(intercityBookingDetailId)
                        .selfDriveBookingDetailId(selfDriveBookingDetailId)
                        .build();

                item = bookingItemRepository.save(item);
                booking.getItems().add(item);

                if (isTour && tourBookingDetailId != null) {
                    tourBookingService.updateBookingItemId(tourBookingDetailId, item.getId());
                } else if (isAirport && airportTransferBookingDetailId != null) {
                    AirportBookingDetail detail = moduleEventPublisher.publishRequest("airport.bookingDetail.findById", airportTransferBookingDetailId, AirportBookingDetail.class);
                    if (detail != null) {
                        detail.setBookingItemId(item.getId());
                        moduleEventPublisher.publishRequest("airport.bookingDetail.save", detail, AirportBookingDetail.class);
                    }
                } else if (isIntercity && intercityBookingDetailId != null) {
                    IntercityBookingDetail detail = moduleEventPublisher.publishRequest("rental.intercityBookingDetail.findById", intercityBookingDetailId, IntercityBookingDetail.class);
                    if (detail != null) {
                        detail.setBookingItemId(item.getId());
                        moduleEventPublisher.publishRequest("rental.intercityBookingDetail.save", detail, IntercityBookingDetail.class);
                    }
                } else if (isSelfDrive && selfDriveBookingDetailId != null) {
                    SelfDriveBookingDetail detail = moduleEventPublisher.publishRequest("rental.selfDriveBookingDetail.findById", selfDriveBookingDetailId, SelfDriveBookingDetail.class);
                    if (detail != null) {
                        detail.setBookingItemId(item.getId());
                        moduleEventPublisher.publishRequest("rental.selfDriveBookingDetail.save", detail, SelfDriveBookingDetail.class);
                    }
                }
            }
        }

        booking.setTotalAmount(calculatedTotal);
        booking.setTotalPrice(calculatedTotal);
        booking = bookingRepository.save(booking);

        sendBookingEmail(booking);

        return bookingMapper.toDto(booking);
    }

    private void sendBookingEmail(Booking booking) {
        try {
            StringBuilder itemsHtml = new StringBuilder();
            for (BookingItem item : booking.getItems()) {
                itemsHtml.append("<tr>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getDynamicDetails() != null ? item.getDynamicDetails() : "Dịch vụ").append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(item.getUnitPrice()).append(" VND</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(item.getTotalPrice()).append(" VND</td>")
                        .append("</tr>");
            }

            String contactInfo = "Không có";
            if (booking.getContactChannel() != null && booking.getContactId() != null) {
                contactInfo = booking.getContactChannel().toUpperCase() + " (" + booking.getContactId() + ")";
            }

            String htmlContent = "<html><body>" +
                    "<h2>Yêu cầu đặt dịch vụ mới</h2>" +
                    "<p><strong>Mã đặt chỗ:</strong> " + booking.getBookingCode() + "</p>" +
                    "<p><strong>Khách hàng:</strong> " + booking.getCustomerName() + "</p>" +
                    "<p><strong>Số điện thoại:</strong> " + booking.getCustomerPhone() + "</p>" +
                    "<p><strong>Email:</strong> " + booking.getCustomerEmail() + "</p>" +
                    "<p><strong>Kênh liên hệ phụ:</strong> " + contactInfo + "</p>" +
                    "<p><strong>Ghi chú:</strong> " + (booking.getCustomerNotes() != null ? booking.getCustomerNotes() : "") + "</p>" +
                    "<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>" +
                    "<thead><tr style='background-color: #f2f2f2;'>" +
                    "<th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Dịch vụ</th>" +
                    "<th style='padding: 8px; border: 1px solid #ddd;'>Số lượng</th>" +
                    "<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Đơn giá</th>" +
                    "<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Thành tiền</th>" +
                    "</tr></thead>" +
                    "<tbody>" + itemsHtml.toString() + "</tbody>" +
                    "<tfoot><tr style='font-weight: bold;'>" +
                    "<td colspan='3' style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Tổng cộng:</td>" +
                    "<td style='padding: 8px; border: 1px solid #ddd; text-align: right; color: #ff9800;'>" + booking.getTotalAmount() + " VND</td>" +
                    "</tr></tfoot>" +
                    "</table>" +
                    "</body></html>";

            mailService.send(
                    "tongnguyenthanhdo@gmail.com",
                    "[Minh Khoa Travel] Yêu cầu đặt dịch vụ mới - " + booking.getBookingCode(),
                    htmlContent
            );
            log.info("Sent booking email confirmation for {}", booking.getBookingCode());
        } catch (Exception e) {
            log.error("Failed to send booking email", e);
        }
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBookingByCode(String code) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with code: " + code));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        if (request.getCustomerName() != null) booking.setCustomerName(request.getCustomerName());
        if (request.getCustomerEmail() != null) booking.setCustomerEmail(request.getCustomerEmail());
        if (request.getCustomerPhone() != null) booking.setCustomerPhone(request.getCustomerPhone());
        if (request.getTotalAmount() != null) booking.setTotalAmount(request.getTotalAmount());
        if (request.getBookingStatus() != null) booking.setBookingStatus(request.getBookingStatus());
        if (request.getCustomerNotes() != null) booking.setCustomerNotes(request.getCustomerNotes());

        booking = bookingRepository.save(booking);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        booking.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingDto> getMyBookings(Long userId) {
        return bookingRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private String generateBookingCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randNum = 100000 + SECURE_RANDOM.nextInt(900000);
        return "BK-" + dateStr + "-" + randNum;
    }
}
