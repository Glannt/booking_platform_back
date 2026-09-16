package com.dotnt.booking_travel_backend.selfdrive.internal.config;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.*;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(21)
public class SelfDriveDataInitializer implements CommandLineRunner {

    private final SelfDriveTypeRepository rentalTypeRepository;
    private final SelfDriveTypeTranslationRepository rentalTypeTranslationRepository;
    private final SelfDriveRentalRepository rentalRepository;
    private final SelfDriveRentalPriceRepository rentalPriceRepository;

    @Override
    public void run(String... args) {
        if (rentalRepository.count() == 0) {
            log.info("Seeding self-drive data...");
            seedRentalTypes();
            seedSelfDriveRentals();
            log.info("Self-drive data seeded.");
        }
    }

    private void seedRentalTypes() {
        if (rentalTypeRepository.count() > 0) return;

        SelfDriveType selfDrive = createRentalType("SELF_DRIVE", true);
        createTranslation(selfDrive, "vi", "Thuê xe tự lái", "Thuê xe không có tài xế, giao xe tận nơi");
        createTranslation(selfDrive, "en", "Self-Drive Rental", "Rent a car without driver, delivered to your location");
        createTranslation(selfDrive, "ko", "셀프드라이브 렌터카", "기사 없이 차량 대여, 위치까지 배달");
        createTranslation(selfDrive, "ru", "Аренда авто (без водителя)", "Аренда автомобиля без водителя, доставка на место");

        SelfDriveType intercity = createRentalType("INTERCITY", true);
        createTranslation(intercity, "vi", "Thuê xe đi tỉnh có tài xế", "Thuê xe có tài xế chạy chuyên tuyến các tỉnh");
        createTranslation(intercity, "en", "Intercity Chauffeured", "Hire a driver for intercity travel");
        createTranslation(intercity, "ko", "지역 간 차량 대여 (기사 포함)", "기사가 포함된 지역 간 차량 대여");
        createTranslation(intercity, "ru", "Междугородняя аренда (с водителем)", "Аренда автомобиля с водителем между городами");
    }

    private void seedSelfDriveRentals() {
        if (rentalRepository.count() > 0) return;

        SelfDriveType selfDriveType = rentalTypeRepository.findAll().stream()
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
            SelfDriveRental rental = SelfDriveRental.builder()
                    .rentalTypeId(selfDriveType.getId())
                    .vehicleCategoryId(vcIds[i])
                    .driverRequired(false)
                    .deposit(deposits[i])
                    .minimumDays(1)
                    .active(true)
                    .build();
            rental = rentalRepository.save(rental);

            rentalPriceRepository.save(SelfDriveRentalPrice.builder()
                    .rentalId(rental.getId())
                    .vehicleCategoryId(vcIds[i])
                    .currency("VND")
                    .amount(dailyRates[i])
                    .active(true)
                    .build());
        }
    }

    private SelfDriveType createRentalType(String code, boolean active) {
        SelfDriveType rt = SelfDriveType.builder()
                .code(code)
                .active(active)
                .build();
        return rentalTypeRepository.save(rt);
    }

    private void createTranslation(SelfDriveType rentalType, String locale, String name, String desc) {
        SelfDriveTypeTranslation translation = SelfDriveTypeTranslation.builder()
                .selfDriveType(rentalType)
                .locale(locale)
                .name(name)
                .description(desc)
                .build();
        rentalTypeTranslationRepository.save(translation);
    }
}
