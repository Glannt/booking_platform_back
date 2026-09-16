package com.dotnt.booking_travel_backend.tour;

import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourBookingDetail;
import com.dotnt.booking_travel_backend.tour.internal.entity.PriceListPerPerson;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPackageRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.impl.TourBookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TourPackageAndPricingTest {

    @Mock
    private TourBookingDetailRepository tourBookingDetailRepository;

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private TourBookingServiceImpl tourBookingService;

    private Tour tour;
    private TourPackage sharedCanoPackage;
    private TourPackage mudBathPackage;
    private TourPackage privateCanoPackage;

    @BeforeEach
    void setUp() {
        tour = Tour.builder()
                .active(true)
                .inclusions(
                        "- Cano ghép theo hành trình.\n" +
                        "- Bữa trưa theo thực đơn tour.\n" +
                        "- Vé tham quan theo chương trình."
                )
                .exclusions(
                        "- Mô tô nước.\n" +
                        "- Dù bay."
                )
                .build();
        tour.setId(1L);

        // Gói 1: Cano ghép - Tiêu chuẩn (650k / 450k / 0k)
        sharedCanoPackage = TourPackage.builder()
                .tour(tour)
                .name("Cano ghép – Tiêu chuẩn")
                .prices(List.of(
                        PriceListPerPerson.builder().passengerType("ADULT").pricePerPerson(new BigDecimal("650000")).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(new BigDecimal("450000")).build(),
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).build()
                ))
                .childPolicyNote("Trẻ em 1m–1m29: 450.000đ")
                .infantPolicyNote("Dưới 1m: Miễn phí")
                .highlights("- Cano ghép\n- Ăn trưa\n- Vé tham quan\n- Trẻ em 1m–1m29: 450.000đ")
                .active(true)
                .build();
        sharedCanoPackage.setId(10L);

        // Gói 2: Hòn Tằm - Tắm Bùn (950k / 700k / 0k)
        mudBathPackage = TourPackage.builder()
                .tour(tour)
                .name("Hòn Tằm – Tắm Bùn")
                .prices(List.of(
                        PriceListPerPerson.builder().passengerType("ADULT").pricePerPerson(new BigDecimal("950000")).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(new BigDecimal("700000")).build(),
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).build()
                ))
                .childPolicyNote("Trẻ em 1m–1m29: 700.000đ")
                .infantPolicyNote("Dưới 1m: Miễn phí")
                .active(true)
                .build();
        mudBathPackage.setId(20L);

        // Gói 3: Cano riêng - Linh hoạt (Liên hệ)
        privateCanoPackage = TourPackage.builder()
                .tour(tour)
                .name("Cano riêng – Linh hoạt")
                .isContactOnly(true)
                .prices(List.of())
                .active(true)
                .build();
        privateCanoPackage.setId(30L);
    }

    @Test
    @DisplayName("Calculate pricing per person: 2 Adults + 1 Child (1m-1m29) + 1 Infant (<1m)")
    void testCalculatePricingPerPerson() {
        int adultCount = 2;
        int childCount = 1;
        int infantCount = 1;

        BigDecimal adultPrice = sharedCanoPackage.getPrices().stream().filter(p -> "ADULT".equals(p.getPassengerType())).findFirst().get().getPricePerPerson();
        BigDecimal childPrice = sharedCanoPackage.getPrices().stream().filter(p -> "CHILD".equals(p.getPassengerType())).findFirst().get().getPricePerPerson();
        BigDecimal infantPrice = sharedCanoPackage.getPrices().stream().filter(p -> "INFANT".equals(p.getPassengerType())).findFirst().get().getPricePerPerson();

        BigDecimal total = adultPrice.multiply(BigDecimal.valueOf(adultCount))
                .add(childPrice.multiply(BigDecimal.valueOf(childCount)))
                .add(infantPrice.multiply(BigDecimal.valueOf(infantCount)));

        // 2 * 650.000 + 1 * 450.000 + 0 = 1.750.000 VND
        assertEquals(new BigDecimal("1750000"), total);
    }

    @Test
    @DisplayName("Calculate pricing for Mud Bath package: 3 Adults + 2 Children")
    void testCalculatePricingMudBath() {
        int adultCount = 3;
        int childCount = 2;

        BigDecimal adultPrice = mudBathPackage.getPrices().stream().filter(p -> "ADULT".equals(p.getPassengerType())).findFirst().get().getPricePerPerson();
        BigDecimal childPrice = mudBathPackage.getPrices().stream().filter(p -> "CHILD".equals(p.getPassengerType())).findFirst().get().getPricePerPerson();

        BigDecimal total = adultPrice.multiply(BigDecimal.valueOf(adultCount))
                .add(childPrice.multiply(BigDecimal.valueOf(childCount)));

        // 3 * 950.000 + 2 * 700.000 = 2.850.000 + 1.400.000 = 4.250.000 VND
        assertEquals(new BigDecimal("4250000"), total);
    }

    @Test
    @DisplayName("Private Canoe is Contact Only and has empty prices")
    void testPrivateCanoeContactOnly() {
        assertTrue(privateCanoPackage.isContactOnly());
        assertTrue(privateCanoPackage.getPrices().isEmpty());
    }

    @Test
    @DisplayName("Create TourBookingDetail with package and passenger breakdown")
    void testCreateTourBookingDetail() {
        when(tourBookingDetailRepository.save(any(TourBookingDetail.class))).thenAnswer(invocation -> {
            TourBookingDetail arg = invocation.getArgument(0);
            arg.setId(100L);
            return arg;
        });

        Instant now = Instant.now();
        Long detailId = tourBookingService.createTourBookingDetail(
                1L,
                tour.getId(),
                3,
                "Khách sạn Sheraton Nha Trang",
                "Đón lúc 8h sáng",
                now,
                null,
                sharedCanoPackage.getId(),
                sharedCanoPackage.getName(),
                "PER_PERSON",
                2,
                1,
                0
        );

        assertNotNull(detailId);
        assertEquals(100L, detailId);

        verify(tourBookingDetailRepository, times(1)).save(argThat(d ->
                d.getTourPackageId().equals(sharedCanoPackage.getId()) &&
                d.getPackageName().equals("Cano ghép – Tiêu chuẩn") &&
                d.getAdultCount() == 2 &&
                d.getChildCount() == 1 &&
                d.getInfantCount() == 0 &&
                d.getGuestCount() == 3
        ));
    }

    @Test
    @DisplayName("Verify bullet text parsing rule with '-' prefix")
    void testBulletParsingWithDash() {
        String inclusionsRaw = "- Cano ghép theo hành trình.\n- Bữa trưa theo thực đơn tour.\n- Vé tham quan theo chương trình.";
        List<String> items = Arrays.stream(inclusionsRaw.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.startsWith("-") ? s.substring(1).trim() : s)
                .collect(Collectors.toList());

        assertEquals(3, items.size());
        assertEquals("Cano ghép theo hành trình.", items.get(0));
        assertEquals("Bữa trưa theo thực đơn tour.", items.get(1));
        assertEquals("Vé tham quan theo chương trình.", items.get(2));
    }

    @Test
    @DisplayName("Verify PriceListPerPerson age and height condition flags and ranges")
    void testPriceListPerPersonAgeAndHeightConditions() {
        PriceListPerPerson adultPrice = PriceListPerPerson.builder()
                .passengerType("ADULT")
                .pricePerPerson(new BigDecimal("650000"))
                .applyAgeCondition(true)
                .minAge(12)
                .maxAge(null)
                .applyHeightCondition(true)
                .minHeight(130)
                .maxHeight(null)
                .build();

        assertTrue(adultPrice.isApplyAgeCondition());
        assertEquals(12, adultPrice.getMinAge());
        assertNull(adultPrice.getMaxAge());
        assertTrue(adultPrice.isApplyHeightCondition());
        assertEquals(130, adultPrice.getMinHeight());
        assertNull(adultPrice.getMaxHeight());

        PriceListPerPerson childPrice = PriceListPerPerson.builder()
                .passengerType("CHILD")
                .pricePerPerson(new BigDecimal("450000"))
                .applyAgeCondition(true)
                .minAge(5)
                .maxAge(11)
                .applyHeightCondition(true)
                .minHeight(100)
                .maxHeight(129)
                .build();

        assertTrue(childPrice.isApplyAgeCondition());
        assertEquals(5, childPrice.getMinAge());
        assertEquals(11, childPrice.getMaxAge());
        assertTrue(childPrice.isApplyHeightCondition());
        assertEquals(100, childPrice.getMinHeight());
        assertEquals(129, childPrice.getMaxHeight());
    }
}
