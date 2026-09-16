package com.dotnt.booking_travel_backend.booking;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.DepositPolicy;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BookingDepositTest {

    @InjectMocks
    private BookingMapper bookingMapper;

    @Test
    void testRateDepositCalculation() {
        BigDecimal total = new BigDecimal("2500000"); // 2.5 mil
        BigDecimal rate = new BigDecimal("30"); // 30% -> 750,000
        BigDecimal deposit = DepositPolicy.calculateDeposit(
                DepositPolicy.TYPE_RATE, rate, null, total, "TOUR"
        );
        assertEquals(new BigDecimal("750000"), deposit);

        // Test rounding to 1,000 VND
        BigDecimal oddTotal = new BigDecimal("1234567");
        BigDecimal calculated = DepositPolicy.calculateDeposit(
                DepositPolicy.TYPE_RATE, new BigDecimal("20"), null, oddTotal, "INTERCITY"
        );
        // 1234567 * 0.20 = 246913.4 -> rounded to 247000
        assertEquals(new BigDecimal("247000"), calculated);
    }

    @Test
    void testFixedDepositCalculation() {
        BigDecimal total = new BigDecimal("5000000");
        BigDecimal fixedAmount = new BigDecimal("1000000");
        BigDecimal deposit = DepositPolicy.calculateDeposit(
                DepositPolicy.TYPE_FIXED, null, fixedAmount, total, "SELF_DRIVE"
        );
        assertEquals(new BigDecimal("1000000"), deposit);

        // Fixed amount capped at total
        BigDecimal excessiveFixed = new BigDecimal("6000000");
        BigDecimal capped = DepositPolicy.calculateDeposit(
                DepositPolicy.TYPE_FIXED, null, excessiveFixed, total, "SELF_DRIVE"
        );
        assertEquals(total, capped);
    }

    @Test
    void testBookingMapperRemainingAmount() {
        Booking booking = Booking.builder()
                .bookingCode("BK-DEP-001")
                .totalAmount(new BigDecimal("3000000"))
                .depositType("RATE")
                .depositRate(new BigDecimal("30"))
                .depositAmount(new BigDecimal("900000"))
                .receivedAmount(new BigDecimal("500000"))
                .paidAmount(new BigDecimal("1400000"))
                .build();
        booking.setId(1L);

        BookingDto dto = bookingMapper.toDto(booking);

        assertEquals(new BigDecimal("3000000"), dto.getTotalAmount());
        assertEquals("RATE", dto.getDepositType());
        assertEquals(new BigDecimal("30"), dto.getDepositRate());
        assertEquals(new BigDecimal("900000"), dto.getDepositAmount());
        assertEquals(new BigDecimal("500000"), dto.getReceivedAmount());
        assertEquals(new BigDecimal("1400000"), dto.getPaidAmount());
        // Remaining should be 3,000,000 - 1,400,000 = 1,600,000
        assertEquals(new BigDecimal("1600000"), dto.getRemainingAmount());
    }
}
