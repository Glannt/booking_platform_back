package com.dotnt.booking_travel_backend.intercity.internal.config;

import com.dotnt.booking_travel_backend.intercity.internal.entity.*;
import com.dotnt.booking_travel_backend.intercity.internal.repository.*;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(20)
public class IntercityDataInitializer implements CommandLineRunner {

    private final IntercityRouteRepository routeRepository;
    private final IntercityRouteTranslationRepository translationRepository;
    private final IntercityRoutePriceRepository priceRepository;
    private final IntercityDepartureTimeRepository departureTimeRepository;
    private final IIntercityPriceService priceService;

    @Override
    public void run(String... args) {
        if (routeRepository.count() == 0) {
            log.info("Seeding intercity routes data...");
            seedIntercityRoutes();
            seedDepartureTimes();
            log.info("Intercity data seeded.");
        }
        try {
            if (priceService.listAll().isEmpty()) {
                log.info("Intercity price lists are empty. Initializing standard default prices...");
                priceService.initDefaultPrices();
            }
        } catch (Exception e) {
            log.warn("Failed to initialize default intercity prices: {}", e.getMessage());
        }
    }

    private void seedIntercityRoutes() {
        seedRoute("Nha Trang", "Đà Lạt", "135", "3.5", true,
                new BigDecimal("12.24507"), new BigDecimal("109.1943"), new BigDecimal("11.940419"), new BigDecimal("108.458313"),
                "Nha Trang ⇌ Đà Lạt", "xe-nha-trang-di-da-lat", "Nha Trang ⇌ Da Lat", "nha-trang-to-da-lat", "나트랑 ⇌ 달랏", "nha-trang-da-lat-route", "Ня Чанг ⇌ Далат", "nha-trang-dalat-route",
                new long[]{10000, 11000, 16000});

        seedRoute("Nha Trang", "Mũi Né", "155", "4", false,
                new BigDecimal("12.24507"), new BigDecimal("109.1943"), new BigDecimal("10.9333"), new BigDecimal("108.2833"),
                "Nha Trang ⇌ Mũi Né", "xe-nha-trang-di-mui-ne", "Nha Trang ⇌ Mui Ne", "nha-trang-to-mui-ne", "나트랑 ⇌ 무이네", "nha-trang-mui-ne-route", "Ня Чанг ⇌ Муйне", "nha-trang-muine-route",
                new long[]{10000, 11000, 16000});

        seedRoute("Nha Trang", "Phan Rang", "100", "2.5", false,
                new BigDecimal("12.24507"), new BigDecimal("109.1943"), new BigDecimal("11.5667"), new BigDecimal("108.9833"),
                "Nha Trang ⇌ Phan Rang", "xe-nha-trang-di-phan-rang", "Nha Trang ⇌ Phan Rang", "nha-trang-to-phan-rang", "나트랑 ⇌ 판랑", "nha-trang-phan-rang-route", "Ня Чанг ⇌ Фанранг", "nha-trang-phanrang-route",
                new long[]{10000, 11000, 16000});

        seedRoute("Nha Trang", "Buôn Ma Thuột", "260", "5.5", false,
                new BigDecimal("12.24507"), new BigDecimal("109.1943"), new BigDecimal("12.6833"), new BigDecimal("108.0500"),
                "Nha Trang ⇌ Buôn Ma Thuột", "xe-nha-trang-di-buon-ma-thuot", "Nha Trang ⇌ Buon Ma Thuot", "nha-trang-to-buon-ma-thuot", "나트랑 ⇌ 봄마투옷", "nha-trang-buon-ma-thuot-route", "Ня Чанг ⇌ Бонматьют", "nha-trang-bonmatyut-route",
                new long[]{10000, 11000, 16000});
    }

    private void seedRoute(String origin, String dest, String dist, String hours, boolean hasDepartureTimes,
                           BigDecimal originLat, BigDecimal originLng, BigDecimal destLat, BigDecimal destLng,
                           String viName, String viSlug, String enName, String enSlug, String koName, String koSlug, String ruName, String ruSlug,
                           long[] perKmPrices) {
        IntercityRoute route = IntercityRoute.builder()
                .originCity(origin)
                .destinationCity(dest)
                .originName(origin)
                .destinationName(dest)
                .distanceKm(new BigDecimal(dist))
                .estimatedHours(new BigDecimal(hours))
                .originLat(originLat)
                .originLng(originLng)
                .destinationLat(destLat)
                .destinationLng(destLng)
                .active(true)
                .build();
        route = routeRepository.save(route);

        translationRepository.save(IntercityRouteTranslation.builder()
                .route(route).locale("vi").name(viName).slug(viSlug).description("Tuyến xe đi tỉnh từ " + origin + " đến " + dest).build());
        translationRepository.save(IntercityRouteTranslation.builder()
                .route(route).locale("en").name(enName).slug(enSlug).description("Intercity route from " + origin + " to " + dest).build());
        translationRepository.save(IntercityRouteTranslation.builder()
                .route(route).locale("ko").name(koName).slug(koSlug).description(origin + "에서 " + dest + "까지 노선").build());
        translationRepository.save(IntercityRouteTranslation.builder()
                .route(route).locale("ru").name(ruName).slug(ruSlug).description("Маршрут из " + origin + " в " + dest).build());

        Long[] vcIds = {1L, 2L, 3L};
        for (int i = 0; i < vcIds.length; i++) {
            priceRepository.save(IntercityRoutePrice.builder()
                    .route(route)
                    .vehicleCategoryId(vcIds[i])
                    .perKmAmount(new BigDecimal(perKmPrices[i]))
                    .pricingType("PER_KM")
                    .currency("VND")
                    .active(true)
                    .build());
        }
    }

    private void seedDepartureTimes() {
        if (departureTimeRepository.count() > 0) return;

        List<IntercityRoute> routes = routeRepository.findByActiveTrue();
        for (IntercityRoute route : routes) {
            boolean isNhaTrangToDaLat = "Nha Trang".equals(route.getOriginCity()) && "Đà Lạt".equals(route.getDestinationCity());
            if (!isNhaTrangToDaLat) continue;

            if (isNhaTrangToDaLat) {
                // Outbound: Nha Trang -> Đà Lạt
                saveDepartureTime(route, "08:00", 1, "OUTBOUND");
                saveDepartureTime(route, "11:00", 2, "OUTBOUND");
                saveDepartureTime(route, "13:00", 3, "OUTBOUND");
                saveDepartureTime(route, "17:00", 4, "OUTBOUND");

                // Return: Đà Lạt -> Nha Trang
                saveDepartureTime(route, "08:00", 1, "RETURN");
                saveDepartureTime(route, "13:00", 2, "RETURN");
                saveDepartureTime(route, "16:00", 3, "RETURN");
                saveDepartureTime(route, "17:00", 4, "RETURN");
            }
        }
    }

    private void saveDepartureTime(IntercityRoute route, String time, int orderIndex, String direction) {
        departureTimeRepository.save(IntercityDepartureTime.builder()
                .route(route)
                .departureTime(time)
                .direction(direction != null ? direction : "OUTBOUND")
                .orderIndex(orderIndex)
                .active(true)
                .build());
    }
}
