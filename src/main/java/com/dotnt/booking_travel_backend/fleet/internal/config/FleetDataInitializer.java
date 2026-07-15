package com.dotnt.booking_travel_backend.fleet.internal.config;

import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class FleetDataInitializer implements CommandLineRunner {

    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (vehicleCategoryRepository.count() == 0) {
            log.info("Seeding vehicle categories...");

            VehicleCategory cat4 = VehicleCategory.builder()
                    .code("4-seater")
                    .minSeat(4)
                    .maxSeat(5)
                    .minLuggage(1)
                    .maxLuggage(2)
                    .unitLuggage("kiện")
                    .active(true)
                    .isDefault(true)
                    .translations(new ArrayList<>())
                    .build();
            cat4.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat4)
                    .locale("vi")
                    .name("Xe 4 chỗ")
                    .slug("xe-4-cho")
                    .description("Dòng xe sedan phổ thông thoải mái (Vios, Accent...)")
                    .build());
            cat4.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat4)
                    .locale("en")
                    .name("4-Seater Sedan")
                    .slug("4-seater-sedan")
                    .description("Comfortable standard sedan (Vios, Accent...)")
                    .build());

            // 2. 7-Seater
            VehicleCategory cat7 = VehicleCategory.builder()
                    .code("7-seater")
                    .minSeat(6)
                    .maxSeat(7)
                    .minLuggage(2)
                    .maxLuggage(4)
                    .unitLuggage("kiện")
                    .active(true)
                    .isDefault(false)
                    .translations(new ArrayList<>())
                    .build();
            cat7.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat7)
                    .locale("vi")
                    .name("Xe 7 chỗ")
                    .slug("xe-7-cho")
                    .description("Dòng xe SUV rộng rãi phù hợp gia đình (Innova, Xpander...)")
                    .build());
            cat7.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat7)
                    .locale("en")
                    .name("7-Seater SUV")
                    .slug("7-seater-suv")
                    .description("Spacious SUV suitable for family (Innova, Xpander...)")
                    .build());

            // 3. 16-Seater
            VehicleCategory cat16 = VehicleCategory.builder()
                    .code("16-seater")
                    .minSeat(15)
                    .maxSeat(16)
                    .minLuggage(4)
                    .maxLuggage(8)
                    .unitLuggage("kiện")
                    .active(true)
                    .isDefault(false)
                    .translations(new ArrayList<>())
                    .build();
            cat16.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat16)
                    .locale("vi")
                    .name("Xe 16 chỗ")
                    .slug("xe-16-cho")
                    .description("Xe Ford Transit hoặc tương đương, phù hợp đi đoàn đông")
                    .build());
            cat16.getTranslations().add(VehicleCategoryTranslation.builder()
                    .vehicleCategory(cat16)
                    .locale("en")
                    .name("16-Seater Van")
                    .slug("16-seater-van")
                    .description("Ford Transit or equivalent, suitable for group tours")
                    .build());

            vehicleCategoryRepository.saveAll(List.of(cat4, cat7, cat16));
            log.info("Vehicle categories seeded successfully!");
        }

        if (vehicleRepository.count() == 0) {
            log.info("Seeding vehicles...");
            VehicleCategory cat4 = vehicleCategoryRepository.findAll().stream()
                    .filter(c -> c.getCode().equals("4-seater")).findFirst().orElse(null);
            VehicleCategory cat7 = vehicleCategoryRepository.findAll().stream()
                    .filter(c -> c.getCode().equals("7-seater")).findFirst().orElse(null);
            VehicleCategory cat16 = vehicleCategoryRepository.findAll().stream()
                    .filter(c -> c.getCode().equals("16-seater")).findFirst().orElse(null);

            if (cat4 != null) {
                vehicleRepository.save(Vehicle.builder()
                        .brand("Toyota")
                        .model("Vios")
                        .year(2023)
                        .fuelType("gasoline")
                        .active(true)
                        .luggage(2)
                        .luggageUnit("kiện")
                        .category(cat4)
                        .build());
            }

            if (cat7 != null) {
                vehicleRepository.save(Vehicle.builder()
                        .brand("Mitsubishi")
                        .model("Xpander")
                        .year(2023)
                        .fuelType("gasoline")
                        .active(true)
                        .luggage(4)
                        .luggageUnit("kiện")
                        .category(cat7)
                        .build());
            }

            if (cat16 != null) {
                vehicleRepository.save(Vehicle.builder()
                        .brand("Ford")
                        .model("Transit")
                        .year(2022)
                        .fuelType("diesel")
                        .active(true)
                        .luggage(8)
                        .luggageUnit("kiện")
                        .category(cat16)
                        .build());
            }
            log.info("Vehicles seeded successfully!");
        }
    }
}
