package com.dotnt.booking_travel_backend.sharedroute.internal.config;

import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.*;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(25)
public class SharedRouteDataInitializer implements CommandLineRunner {

    private final SharedOfficeRepository officeRepository;
    private final SharedRouteRepository routeRepository;
    private final SharedRouteDirectionRepository directionRepository;
    private final SharedRouteScheduleRepository scheduleRepository;
    private final ModuleEventPublisher moduleEventPublisher;

    @Override
    @Transactional
    public void run(String... args) {
        if (officeRepository.count() == 0) {
            log.info("Seeding shared route offices...");
            seedOffices();
        }

        if (routeRepository.count() == 0) {
            log.info("Seeding shared routes, directions, and schedules...");
            seedSharedRoutes();
            log.info("Shared routes data seeded successfully.");
        }
    }

    private void seedOffices() {
        officeRepository.save(SharedOffice.builder()
                .name("Văn phòng Nha Trang")
                .address("126 Hoàng Hoa Thám, Lộc Thọ, Nha Trang, Khánh Hòa")
                .city("Nha Trang")
                .latitude(new BigDecimal("12.2405000"))
                .longitude(new BigDecimal("109.1950000"))
                .active(true)
                .build());

        officeRepository.save(SharedOffice.builder()
                .name("Văn phòng Đà Lạt")
                .address("26 Nguyễn Thị Minh Khai, Phường 1, TP. Đà Lạt, Lâm Đồng")
                .city("Đà Lạt")
                .latitude(new BigDecimal("11.9424000"))
                .longitude(new BigDecimal("108.4377000"))
                .active(true)
                .build());

        officeRepository.save(SharedOffice.builder()
                .name("Văn phòng Mũi Né")
                .address("139 Nguyễn Đình Chiểu, Hàm Tiến, TP. Phan Thiết, Bình Thuận")
                .city("Mũi Né")
                .latitude(new BigDecimal("10.9575000"))
                .longitude(new BigDecimal("108.2162000"))
                .active(true)
                .build());

        officeRepository.save(SharedOffice.builder()
                .name("Văn phòng Buôn Ma Thuột")
                .address("45 Lê Duẩn, Tân Thành, TP. Buôn Ma Thuột, Đắk Lắk")
                .city("Buôn Ma Thuột")
                .latitude(new BigDecimal("12.6800000"))
                .longitude(new BigDecimal("108.0380000"))
                .active(true)
                .build());
    }

    private void seedSharedRoutes() {
        Map<String, SharedOffice> officesByCity = new HashMap<>();
        for (SharedOffice office : officeRepository.findByActiveTrue()) {
            officesByCity.put(office.getCity(), office);
        }

        SharedOffice nhaTrangOffice = officesByCity.get("Nha Trang");
        SharedOffice daLatOffice = officesByCity.get("Đà Lạt");
        SharedOffice muiNeOffice = officesByCity.get("Mũi Né");
        SharedOffice bmtOffice = officesByCity.get("Buôn Ma Thuột");

        // 1. Nha Trang ⇌ Đà Lạt
        Long ntdlIntercityId = null;
        try {
            ntdlIntercityId = moduleEventPublisher.publishRequest(
                    "intercity.intercityRoute.findIdByCities", new String[]{"Nha Trang", "Đà Lạt"}, Long.class);
        } catch (Exception e) {
            log.warn("Could not find intercity route Nha Trang - Đà Lạt: {}", e.getMessage());
        }

        SharedRoute r1 = routeRepository.save(SharedRoute.builder()
                .name("Nha Trang ⇌ Đà Lạt")
                .vehicleName("Dcar Limousine 9 chỗ")
                .slug("xe-ghep-nha-trang-da-lat")
                .linkedIntercityRouteId(ntdlIntercityId)
                .active(true)
                .build());

        createDirectionWithSchedules(
                r1,
                DirectionType.OUTBOUND,
                "Nha Trang", "Đà Lạt",
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                new BigDecimal("11.9404190"), new BigDecimal("108.4583130"),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                daLatOffice != null ? List.of(daLatOffice) : List.of(),
                new BigDecimal("250000.00"),
                new BigDecimal("135.0"),
                new BigDecimal("3.5"),
                List.of("07:00", "09:00", "11:00", "13:00", "15:00", "17:00")
        );

        createDirectionWithSchedules(
                r1,
                DirectionType.RETURN,
                "Đà Lạt", "Nha Trang",
                new BigDecimal("11.9404190"), new BigDecimal("108.4583130"),
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                daLatOffice != null ? List.of(daLatOffice) : List.of(),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                new BigDecimal("250000.00"),
                new BigDecimal("135.0"),
                new BigDecimal("3.5"),
                List.of("07:00", "09:00", "11:00", "13:00", "15:00", "17:00")
        );

        // 2. Nha Trang ⇌ Mũi Né
        Long ntmnIntercityId = null;
        try {
            ntmnIntercityId = moduleEventPublisher.publishRequest(
                    "intercity.intercityRoute.findIdByCities", new String[]{"Nha Trang", "Mũi Né"}, Long.class);
        } catch (Exception e) {
            log.warn("Could not find intercity route Nha Trang - Mũi Né: {}", e.getMessage());
        }

        SharedRoute r2 = routeRepository.save(SharedRoute.builder()
                .name("Nha Trang ⇌ Mũi Né")
                .vehicleName("Dcar Limousine 9 chỗ")
                .slug("xe-ghep-nha-trang-mui-ne")
                .linkedIntercityRouteId(ntmnIntercityId)
                .active(true)
                .build());

        createDirectionWithSchedules(
                r2,
                DirectionType.OUTBOUND,
                "Nha Trang", "Mũi Né",
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                new BigDecimal("10.9333000"), new BigDecimal("108.2833000"),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                muiNeOffice != null ? List.of(muiNeOffice) : List.of(),
                new BigDecimal("280000.00"),
                new BigDecimal("155.0"),
                new BigDecimal("4.0"),
                List.of("08:00", "13:00", "16:00")
        );

        createDirectionWithSchedules(
                r2,
                DirectionType.RETURN,
                "Mũi Né", "Nha Trang",
                new BigDecimal("10.9333000"), new BigDecimal("108.2833000"),
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                muiNeOffice != null ? List.of(muiNeOffice) : List.of(),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                new BigDecimal("280000.00"),
                new BigDecimal("155.0"),
                new BigDecimal("4.0"),
                List.of("08:00", "13:00", "16:00")
        );

        // 3. Nha Trang ⇌ Buôn Ma Thuột
        Long ntbmtIntercityId = null;
        try {
            ntbmtIntercityId = moduleEventPublisher.publishRequest(
                    "intercity.intercityRoute.findIdByCities", new String[]{"Nha Trang", "Buôn Ma Thuột"}, Long.class);
        } catch (Exception e) {
            log.warn("Could not find intercity route Nha Trang - Buôn Ma Thuột: {}", e.getMessage());
        }

        SharedRoute r3 = routeRepository.save(SharedRoute.builder()
                .name("Nha Trang ⇌ Buôn Ma Thuột")
                .vehicleName("Dcar Limousine 9 chỗ")
                .slug("xe-ghep-nha-trang-buon-ma-thuot")
                .linkedIntercityRouteId(ntbmtIntercityId)
                .active(true)
                .build());

        createDirectionWithSchedules(
                r3,
                DirectionType.OUTBOUND,
                "Nha Trang", "Buôn Ma Thuột",
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                new BigDecimal("12.6833000"), new BigDecimal("108.0500000"),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                bmtOffice != null ? List.of(bmtOffice) : List.of(),
                new BigDecimal("300000.00"),
                new BigDecimal("260.0"),
                new BigDecimal("5.5"),
                List.of("07:30", "13:30")
        );

        createDirectionWithSchedules(
                r3,
                DirectionType.RETURN,
                "Buôn Ma Thuột", "Nha Trang",
                new BigDecimal("12.6833000"), new BigDecimal("108.0500000"),
                new BigDecimal("12.2450700"), new BigDecimal("109.1943000"),
                bmtOffice != null ? List.of(bmtOffice) : List.of(),
                nhaTrangOffice != null ? List.of(nhaTrangOffice) : List.of(),
                new BigDecimal("300000.00"),
                new BigDecimal("260.0"),
                new BigDecimal("5.5"),
                List.of("07:30", "13:30")
        );
    }

    private void createDirectionWithSchedules(
            SharedRoute route,
            DirectionType directionType,
            String originCity,
            String destCity,
            BigDecimal originLat,
            BigDecimal originLng,
            BigDecimal destLat,
            BigDecimal destLng,
            List<SharedOffice> originOffices,
            List<SharedOffice> destOffices,
            BigDecimal price,
            BigDecimal distanceKm,
            BigDecimal estimatedHours,
            List<String> times
    ) {
        SharedRouteDirection direction = SharedRouteDirection.builder()
                .route(route)
                .directionType(directionType)
                .originCity(originCity)
                .destinationCity(destCity)
                .originLat(originLat)
                .originLng(originLng)
                .destinationLat(destLat)
                .destinationLng(destLng)
                .originOffices(new ArrayList<>(originOffices))
                .destinationOffices(new ArrayList<>(destOffices))
                .price(price)
                .distanceKm(distanceKm)
                .estimatedHours(estimatedHours)
                .active(true)
                .build();
        direction = directionRepository.save(direction);

        Set<DayOfWeek> allDays = EnumSet.allOf(DayOfWeek.class);

        for (String timeStr : times) {
            scheduleRepository.save(SharedRouteSchedule.builder()
                    .direction(direction)
                    .departureTime(LocalTime.parse(timeStr))
                    .daysOfWeek("DAILY")
                    .operatingDays(new HashSet<>(allDays))
                    .active(true)
                    .note("Xe Limousine chạy hàng ngày")
                    .build());
        }
    }
}
