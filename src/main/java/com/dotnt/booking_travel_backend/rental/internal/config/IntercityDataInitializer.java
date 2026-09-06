package com.dotnt.booking_travel_backend.rental.internal.config;

import com.dotnt.booking_travel_backend.rental.internal.entity.*;
import com.dotnt.booking_travel_backend.rental.internal.repository.*;
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
    private final RentalTypeRepository rentalTypeRepository;
    private final RentalTypeTranslationRepository rentalTypeTranslationRepository;
    private final RentalRepository rentalRepository;
    private final RentalPriceRepository rentalPriceRepository;

    @Override
    public void run(String... args) {
        if (routeRepository.count() > 0) return;

        log.info("Seeding intercity routes and self-drive data...");

        seedRentalTypes();
        seedSelfDriveRentals();
        seedIntercityRoutes();
        seedDepartureTimes();

        log.info("Intercity + self-drive data seeded.");
    }

    private void seedRentalTypes() {
        if (rentalTypeRepository.count() > 0) return;

        RentalType selfDrive = createRentalType("SELF_DRIVE", true);
        createTranslation(selfDrive, "vi", "Thuê xe tự lái", "Thuê xe không có tài xế, giao xe tận nơi");
        createTranslation(selfDrive, "en", "Self-Drive Rental", "Rent a car without driver, delivered to your location");
        createTranslation(selfDrive, "ko", "셀프드라이브 렌터카", "기사 없이 차량 대여, 위치까지 배달");
        createTranslation(selfDrive, "ru", "Аренда авто (без водителя)", "Аренда автомобиля без водителя, доставка на место");

        RentalType intercity = createRentalType("INTERCITY", true);
        createTranslation(intercity, "vi", "Thuê xe đi tỉnh có tài xế", "Thuê xe có tài xế chạy chuyên tuyến các tỉnh");
        createTranslation(intercity, "en", "Intercity Chauffeured", "Hire a driver for intercity travel");
        createTranslation(intercity, "ko", "지역 간 차량 대여 (기사 포함)", "기사가 포함된 지역 간 차량 대여");
        createTranslation(intercity, "ru", "Междугородняя аренда (с водителем)", "Аренда автомобиля с водителем между городами");
    }

    private void seedSelfDriveRentals() {
        if (rentalRepository.count() > 0) return;

        RentalType selfDriveType = rentalTypeRepository.findAll().stream()
                .filter(rt -> "SELF_DRIVE".equals(rt.getCode()))
                .findFirst().orElse(null);
        if (selfDriveType == null) return;

        Long[] vcIds = {1L, 2L, 3L, 4L, 5L};
        BigDecimal[] deposits = {
                new BigDecimal("500000"), new BigDecimal("500000"),
                new BigDecimal("600000"), new BigDecimal("800000"), new BigDecimal("1000000")
        };
        BigDecimal[] dailyRates = {
                new BigDecimal("700000"), new BigDecimal("800000"),
                new BigDecimal("800000"), new BigDecimal("1200000"), new BigDecimal("1600000")
        };

        for (int i = 0; i < vcIds.length; i++) {
            Rental rental = Rental.builder()
                    .rentalTypeId(selfDriveType.getId())
                    .vehicleCategoryId(vcIds[i])
                    .driverRequired(false)
                    .deposit(deposits[i])
                    .minimumDays(1)
                    .active(true)
                    .build();
            rental = rentalRepository.save(rental);

            rentalPriceRepository.save(RentalPrice.builder()
                    .rentalId(rental.getId())
                    .vehicleCategoryId(vcIds[i])
                    .currency("VND")
                    .amount(dailyRates[i])
                    .active(true)
                    .build());
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
                .hasDepartureTimes(hasDepartureTimes)
                .supportsShared(true)
                .sharedVehicleName("Dcar Limousine")
                .sharedPriceOneWay(new BigDecimal("250000.00"))
                .sharedPriceRoundTrip(new BigDecimal("500000.00"))
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
            if (!route.isHasDepartureTimes()) continue;

            boolean isNhaTrangToDaLat = "Nha Trang".equals(route.getOriginCity()) && "Đà Lạt".equals(route.getDestinationCity());

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

    private RentalType createRentalType(String code, boolean active) {
        return rentalTypeRepository.save(RentalType.builder().code(code).active(active).build());
    }

    private void createTranslation(RentalType type, String locale, String name, String desc) {
        rentalTypeTranslationRepository.save(RentalTypeTranslation.builder()
                .rentalType(type).locale(locale).name(name).description(desc).build());
    }
}
