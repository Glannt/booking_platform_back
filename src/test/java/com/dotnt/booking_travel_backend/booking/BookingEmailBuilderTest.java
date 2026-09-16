package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingEmailBuilder;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingEmailBuilderTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private TourBookingService tourBookingService;
    @Mock
    private FleetFacade fleetFacade;
    @Mock
    private AirportFacade airportFacade;
    @Mock
    private IntercityFacade intercityFacade;
    @Mock
    private SelfDriveFacade selfDriveFacade;

    private BookingEmailBuilder emailBuilder;

    @BeforeEach
    void setUp() {
        emailBuilder = new BookingEmailBuilder(
                templateEngine,
                tourBookingService,
                fleetFacade,
                airportFacade,
                intercityFacade,
                selfDriveFacade
        );
        ReflectionTestUtils.setField(emailBuilder, "portalUrl", "http://localhost:3000/portal/bookings");
    }

    @Test
    void testCustomerEmailFallback() {
        Booking booking = createSampleBooking();

        String html = emailBuilder.buildEmailHtml(booking, false);

        assertNotNull(html);
        // Verify no hourglass emoji
        assertFalse(html.contains("⏳"), "Should not contain hourglass emoji");
        // Verify no blue branding
        assertFalse(html.contains("#2563eb"), "Should not contain blue color #2563eb");
        // Verify customer messaging
        assertTrue(html.contains("ĐÃ TIẾP NHẬN ĐƠN ĐẶT CHỖ"));
        assertTrue(html.contains("Nhân viên CSKH sẽ liên hệ"));
        assertTrue(html.contains("0987.654.321"));
        // Customer should NOT have portal link button
        assertFalse(html.contains("Vào Trang Quản Trị Xử Lý Đơn"));
        // Service name resolution
        assertTrue(html.contains("Nha Trang ➔ Đà Lạt"));
    }

    @Test
    void testAdminEmailFallback() {
        Booking booking = createSampleBooking();

        String html = emailBuilder.buildEmailHtml(booking, true);

        assertNotNull(html);
        // Verify no hourglass emoji
        assertFalse(html.contains("⏳"), "Should not contain hourglass emoji");
        // Verify no blue branding
        assertFalse(html.contains("#2563eb"), "Should not contain blue color #2563eb");
        // Verify admin messaging
        assertTrue(html.contains("CÓ ĐƠN ĐẶT CHỖ MỚI CẦN XỬ LÝ"));
        assertTrue(html.contains("đăng nhập trang quản trị"));
        // Admin SHOULD have portal link button
        assertTrue(html.contains("Vào Trang Quản Trị Xử Lý Đơn"));
        assertTrue(html.contains("http://localhost:3000/portal/bookings"));
    }

    private Booking createSampleBooking() {
        BookingItem item = BookingItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("875000"))
                .totalPrice(new BigDecimal("1750000"))
                .dynamicDetails("Category ID: 2, Pickup: Nha Trang, Dropoff: Đà Lạt")
                .build();

        List<BookingItem> items = new ArrayList<>();
        items.add(item);

        return Booking.builder()
                .bookingCode("BK-20260916-767995")
                .customerName("Minh Khoa Admin")
                .customerPhone("0987654321")
                .customerEmail("tongnguyenthanhdo@gmail.com")
                .totalAmount(new BigDecimal("1750000"))
                .items(items)
                .build();
    }
}
