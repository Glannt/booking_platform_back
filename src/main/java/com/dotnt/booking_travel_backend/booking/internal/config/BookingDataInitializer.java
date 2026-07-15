package com.dotnt.booking_travel_backend.booking.internal.config;

import com.dotnt.booking_travel_backend.booking.api.BookingStatus;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class BookingDataInitializer implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;

    @Override
    public void run(String... args) throws Exception {
        if (bookingRepository.count() == 0) {
            // Seed 1
            Booking b1 = Booking.builder()
                    .bookingCode("BK-20260701-110291")
                    .userId(1L)
                    .customerName("Nguyễn Văn A")
                    .customerEmail("vana@gmail.com")
                    .customerPhone("0905123456")
                    .totalAmount(new BigDecimal("1500000.00"))
                    .totalPrice(new BigDecimal("1500000.00"))
                    .bookingStatus(BookingStatus.confirmed)
                    .customerNotes("Cần xe sạch sẽ, tài xế không hút thuốc.")
                    .items(new ArrayList<>())
                    .build();

            b1 = bookingRepository.save(b1);

            BookingItem item1 = BookingItem.builder()
                    .booking(b1)
//                    .serviceId(1L)
//                    .serviceOptionId(11L)
                    .quantity(1)
                    .unitPrice(new BigDecimal("1500000.00"))
                    .totalPrice(new BigDecimal("1500000.00"))
//                    .startDate(LocalDateTime.now().plusDays(2))
//                    .endDate(LocalDateTime.now().plusDays(2).plusHours(4))
//                    .pickupLocation("Sân bay Cam Ranh (CXR)")
//                    .dropoffLocation("Khách sạn Novotel Nha Trang")
//                    .flightNumber("VJ-382")
//                    .serviceName("{\"vi\":\"Đón tiễn sân bay\",\"en\":\"Airport Transfer\",\"ko\":\"공항 픽업\",\"ru\":\"Трансфер в аэропорт\"}")
//                    .serviceOptionName("{\"vi\":\"Xe 4 chỗ\",\"en\":\"4-seater car\",\"ko\":\"4인승 차량\",\"ru\":\"4-местный автомобиль\"}")
                    .build();

            bookingItemRepository.save(item1);

            // Seed 2
            Booking b2 = Booking.builder()
                    .bookingCode("BK-20260701-901235")
                    .userId(2L)
                    .customerName("John Smith")
                    .customerEmail("john.smith@gmail.com")
                    .customerPhone("+12065550143")
                    .totalAmount(new BigDecimal("2500000.00"))
                    .totalPrice(new BigDecimal("1500000.00"))
                    .bookingStatus(BookingStatus.pending)
                    .customerNotes("Please coordinate guide fluent in English.")
                    .items(new ArrayList<>())
                    .build();

            b2 = bookingRepository.save(b2);

            BookingItem item2 = BookingItem.builder()
                    .booking(b2)
//                    .serviceId(2L)
//                    .serviceOptionId(12L)
                    .quantity(2)
                    .unitPrice(new BigDecimal("1250000.00"))
                    .totalPrice(new BigDecimal("2500000.00"))
//                    .startDate(LocalDateTime.now().plusDays(5))
//                    .endDate(LocalDateTime.now().plusDays(6))
//                    .pickupLocation("Khách sạn Mường Thanh Luxury")
//                    .dropoffLocation("Đảo du lịch Hòn Tằm")
//                    .serviceName("{\"vi\":\"Thuê Xe Tự Lái\",\"en\":\"Self-Drive Rental\",\"ko\":\"렌터카\",\"ru\":\"Аренда авто (без водителя)\"}")
//                    .serviceOptionName("{\"vi\":\"Xe 7 chỗ\",\"en\":\"7-seater car\",\"ko\":\"7인승 차량\",\"ru\":\"7-местный автомобиль\"}")
                    .build();

            bookingItemRepository.save(item2);

            System.out.println("Booking module data initialized successfully!");
        }
    }
}
