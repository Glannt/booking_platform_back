package com.dotnt.booking_travel_backend.tour;

import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourBookingDetail;
import com.dotnt.booking_travel_backend.tour.internal.entity.PriceListPerPerson;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPackageRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.impl.TourBookingServiceImpl;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
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

    @Mock
    private com.dotnt.booking_travel_backend.tour.internal.repository.TourPackageRepository tourPackageRepository;

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

    // --- Publishable rule -----------------------------------------------------------
    // Price does not live on `tours` or even on `tour_packages` — it lives in
    // `price_list_per_person`. So "this package can be sold" means there is an active ADULT row
    // with a figure above zero, unless the admin deliberately marked it as a quote request.

    @Test
    @DisplayName("Publishable: a package with an active adult price can go live")
    void testPublishableWhenAnAdultPriceIsPresent() {
        assertTrue(sharedCanoPackage.isPublishable(), "rule A: priced, no flag needed");
        assertTrue(mudBathPackage.isPublishable());
    }

    @Test
    @DisplayName("Publishable: a contact-only package may go live with no price at all")
    void testPublishableWhenContactOnlyDespiteHavingNoPrice() {
        assertTrue(privateCanoPackage.isPublishable(), "rule B: quote request, still books");
    }

    @Test
    @DisplayName("Not publishable: no adult price and no contact-only flag")
    void testNotPublishableWhenThereIsNeitherAPriceNorAFlag() {
        TourPackage unpublishable = TourPackage.builder()
                .tour(tour)
                .name("Gói thiếu giá")
                .prices(List.of())
                .active(true)
                .build();

        assertFalse(unpublishable.isPublishable(),
                "rule C: without either, activating would advertise a trip nobody priced");
    }

    @Test
    @DisplayName("Not publishable: a zero adult price is a missing price, not a price")
    void testNotPublishableWhenTheAdultPriceIsZero() {
        TourPackage freeAdult = TourPackage.builder()
                .tour(tour)
                .name("Gói giá 0")
                .prices(List.of(
                        PriceListPerPerson.builder()
                                .passengerType("ADULT")
                                .pricePerPerson(BigDecimal.ZERO)
                                .build()
                ))
                .active(true)
                .build();

        assertFalse(freeAdult.isPublishable(), "0đ is what an empty price box stores");
    }

    @Test
    @DisplayName("Not publishable: a free infant cannot stand in for an adult price")
    void testNotPublishableWhenOnlyFreePassengerTypesArePriced() {
        TourPackage infantOnly = TourPackage.builder()
                .tour(tour)
                .name("Gói chỉ có trẻ sơ sinh")
                .prices(List.of(
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(BigDecimal.ZERO).build()
                ))
                .active(true)
                .build();

        assertFalse(infantOnly.isPublishable(),
                "infant is free by policy and child follows the itinerary — neither can price the trip");
    }

    @Test
    @DisplayName("Not publishable: a deactivated adult price no longer counts")
    void testNotPublishableWhenTheAdultPriceIsInactive() {
        TourPackage inactivePrice = TourPackage.builder()
                .tour(tour)
                .name("Gói giá đã tắt")
                .prices(List.of(
                        PriceListPerPerson.builder()
                                .passengerType("ADULT")
                                .pricePerPerson(new BigDecimal("650000"))
                                .active(false)
                                .build()
                ))
                .active(true)
                .build();

        assertFalse(inactivePrice.isPublishable(),
                "ticking the price off is how an admin takes a price out of service");
    }

    @Test
    @DisplayName("isContactOnlyPackage resolves the flag by package id and never throws on a bad id")
    void testIsContactOnlyPackageLookup() {
        when(tourPackageRepository.findById(30L))
                .thenReturn(java.util.Optional.of(privateCanoPackage));
        when(tourPackageRepository.findById(10L))
                .thenReturn(java.util.Optional.of(sharedCanoPackage));

        assertTrue(tourBookingService.isContactOnlyPackage(30L));
        assertFalse(tourBookingService.isContactOnlyPackage(10L));
        assertFalse(tourBookingService.isContactOnlyPackage(null), "no package id, nothing to look up");
        assertFalse(tourBookingService.isContactOnlyPackage(999L));
    }

    @Test
    @DisplayName("Deposit resolves from the booked package: configured stays, unconfigured stays null")
    void testResolveDepositPolicyForPackage() {
        sharedCanoPackage.setDepositPolicies(DepositPolicy.builder()
                .type("PERCENT").rate(new BigDecimal("10.00")).build());
        when(tourPackageRepository.findById(10L))
                .thenReturn(java.util.Optional.of(sharedCanoPackage));
        when(tourPackageRepository.findById(20L))
                .thenReturn(java.util.Optional.of(mudBathPackage));

        DepositPolicy configured = tourBookingService.resolveDepositPolicyForPackage(10L);
        assertNotNull(configured);
        assertEquals("PERCENT", configured.getType());
        assertEquals(0, new BigDecimal("10.00").compareTo(configured.getRate()));

        assertNull(tourBookingService.resolveDepositPolicyForPackage(20L),
                "an unconfigured package resolves to null — no default rate is substituted");
        assertNull(tourBookingService.resolveDepositPolicyForPackage(null),
                "no package id, nothing to look up");
    }
}
