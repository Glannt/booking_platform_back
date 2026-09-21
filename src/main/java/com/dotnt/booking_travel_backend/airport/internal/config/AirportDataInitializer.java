package com.dotnt.booking_travel_backend.airport.internal.config;

import com.dotnt.booking_travel_backend.airport.internal.entity.*;
import com.dotnt.booking_travel_backend.airport.internal.repository.*;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(15)
public class AirportDataInitializer implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final AirportRouteRepository airportRouteRepository;
    private final AirportTransferPriceRepository airportTransferPriceRepository;
    private final FleetFacade fleetFacade;

    @Override
    public void run(String... args) throws Exception {
        if (airportRepository.count() > 0) {
            log.info("Airports already seeded, skipping.");
            return;
        }

        log.info("Seeding airport transfer data...");

        Airport cxr = airportRepository.save(Airport.builder()
                .code("CXR")
                .name("Cam Ranh International Airport")
                .location("Cam Ranh, Khanh Hoa, Vietnam")
                .latitude(12.0104)
                .longitude(109.2171)
                .active(true)
                .translations(new ArrayList<>())
                .build());

        cxr.getTranslations().add(AirportTranslation.builder()
                .airport(cxr).locale("vi")
                .name("Sân bay Quốc tế Cam Ranh")
                .description("Sân bay Quốc tế Cam Ranh (CXR) phục vụ tỉnh Khánh Hòa và khu vực Nam Trung Bộ Việt Nam. Cách trung tâm thành phố Nha Trang khoảng 35km.")
                .build());
        cxr.getTranslations().add(AirportTranslation.builder()
                .airport(cxr).locale("en")
                .name("Cam Ranh International Airport")
                .description("Cam Ranh International Airport (CXR) serves Khanh Hoa province and South Central Coast region of Vietnam. Located approximately 35km from Nha Trang city center.")
                .build());
        cxr.getTranslations().add(AirportTranslation.builder()
                .airport(cxr).locale("ko")
                .name("깜란 국제공항")
                .description("깜란 국제공항(CXR)은 끼엔호아 성과 남중부 베트남 지역을 서비스합니다. 닫랑 시내에서 약 35km 떨어져 있습니다.")
                .build());
        cxr.getTranslations().add(AirportTranslation.builder()
                .airport(cxr).locale("ru")
                .name("Международный аэропорт Камрань")
                .description("Международный аэропорт Камрань (CXR) обслуживает провинцию Ханьхоа и регион Юго-Центрального Вьетнама. Расположен примерно в 35 км от центра Нячанга.")
                .build());
        airportRepository.save(cxr);

        Long cat4Id = fleetFacade.findCategoryIdByCode("4-seater");
        Long cat7Id = fleetFacade.findCategoryIdByCode("7-seater");
        Long cat16Id = fleetFacade.findCategoryIdByCode("16-seater");

        AirportRoute route1 = airportRouteRepository.save(AirportRoute.builder()
                .code("CXR-NT-CENTER")
                .airport(cxr)
                .name("Sân bay Cam Ranh ⇌ Nha Trang (Trung tâm)")
                .distanceKm(new BigDecimal("35.0"))
                .active(true)
                .build());

        AirportRoute route2 = airportRouteRepository.save(AirportRoute.builder()
                .code("CXR-NT-DUONG")
                .airport(cxr)
                .name("Sân bay Cam Ranh ⇌ Đường Đệ / Vĩnh Hòa")
                .distanceKm(new BigDecimal("42.0"))
                .active(true)
                .build());

        AirportRoute route3 = airportRouteRepository.save(AirportRoute.builder()
                .code("CXR-NT-SOUTH")
                .airport(cxr)
                .name("Sân bay Cam Ranh ⇌ Bãi Dài / Nam Cam Ranh")
                .distanceKm(new BigDecimal("15.0"))
                .active(true)
                .build());

        List<AirportRoute> routes = List.of(route1, route2, route3);

        BigDecimal baseKm = new BigDecimal("35");
        BigDecimal defaultZoneBufferKm = new BigDecimal("2.00");
        BigDecimal defaultFeeFixed = new BigDecimal("100000");
        for (AirportRoute route : routes) {
            if (cat4Id != null) {
                airportTransferPriceRepository.save(AirportTransferPrice.builder()
                        .airportRouteId(route.getId())
                        .vehicleCategoryId(cat4Id)
                        .baseKm(baseKm)
                        .zoneBufferKm(defaultZoneBufferKm)
                        .baseAmount(new BigDecimal("300000"))
                        .perKmAmount(new BigDecimal("10000"))
                        .fixedOvercharge(false)
                        .feeOverchargeFixed(defaultFeeFixed)
                        .currency("VND")
                        .active(true)
                        .build());
            }
            if (cat7Id != null) {
                airportTransferPriceRepository.save(AirportTransferPrice.builder()
                        .airportRouteId(route.getId())
                        .vehicleCategoryId(cat7Id)
                        .baseKm(baseKm)
                        .zoneBufferKm(defaultZoneBufferKm)
                        .baseAmount(new BigDecimal("450000"))
                        .perKmAmount(new BigDecimal("15000"))
                        .fixedOvercharge(false)
                        .feeOverchargeFixed(defaultFeeFixed)
                        .currency("VND")
                        .active(true)
                        .build());
            }
            if (cat16Id != null) {
                airportTransferPriceRepository.save(AirportTransferPrice.builder()
                        .airportRouteId(route.getId())
                        .vehicleCategoryId(cat16Id)
                        .baseKm(baseKm)
                        .zoneBufferKm(defaultZoneBufferKm)
                        .baseAmount(new BigDecimal("700000"))
                        .perKmAmount(new BigDecimal("20000"))
                        .fixedOvercharge(false)
                        .feeOverchargeFixed(defaultFeeFixed)
                        .currency("VND")
                        .active(true)
                        .build());
            }
        }

        log.info("Airport transfer data seeded successfully!");
    }
}
