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
                    .totalVehicles(3)
                    .availableVehicles(3)
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
                    .totalVehicles(3)
                    .availableVehicles(3)
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
                    .totalVehicles(2)
                    .availableVehicles(2)
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
        } else {
            // Update existing categories if totalVehicles is null
            List<VehicleCategory> existingCats = vehicleCategoryRepository.findAll();
            for (VehicleCategory cat : existingCats) {
                boolean changed = false;
                if (cat.getTotalVehicles() == null) {
                    if ("4-seater".equals(cat.getCode())) { cat.setTotalVehicles(3); cat.setAvailableVehicles(3); }
                    else if ("7-seater".equals(cat.getCode())) { cat.setTotalVehicles(3); cat.setAvailableVehicles(3); }
                    else if ("16-seater".equals(cat.getCode())) { cat.setTotalVehicles(2); cat.setAvailableVehicles(2); }
                    else { cat.setTotalVehicles(1); cat.setAvailableVehicles(1); }
                    changed = true;
                }
                if (changed) {
                    vehicleCategoryRepository.save(cat);
                }
            }
        }

        seedSampleVehicles();
    }

    private void seedSampleVehicles() {
        VehicleCategory cat4 = vehicleCategoryRepository.findAll().stream()
                .filter(c -> c.getCode().equals("4-seater")).findFirst().orElse(null);
        VehicleCategory cat7 = vehicleCategoryRepository.findAll().stream()
                .filter(c -> c.getCode().equals("7-seater")).findFirst().orElse(null);
        VehicleCategory cat16 = vehicleCategoryRepository.findAll().stream()
                .filter(c -> c.getCode().equals("16-seater")).findFirst().orElse(null);

        // Category 4-seater (3 vehicles)
        if (cat4 != null) {
            seedOrUpdateVehicle("Toyota", "Vios", "79A-123.45", 2023, "gasoline", 2, "kiện", cat4);
            seedOrUpdateVehicle("Hyundai", "Accent", "79A-234.56", 2023, "gasoline", 2, "kiện", cat4);
            seedOrUpdateVehicle("Kia", "K3", "79A-345.67", 2024, "gasoline", 2, "kiện", cat4);
        }

        // Category 7-seater (3 vehicles)
        if (cat7 != null) {
            seedOrUpdateVehicle("Mitsubishi", "Xpander", "79A-456.78", 2023, "gasoline", 4, "kiện", cat7);
            seedOrUpdateVehicle("Toyota", "Veloz Cross", "79A-567.89", 2024, "gasoline", 4, "kiện", cat7);
            seedOrUpdateVehicle("Toyota", "Fortuner", "79A-678.90", 2023, "diesel", 4, "kiện", cat7);
        }

        // Category 16-seater (2 vehicles)
        if (cat16 != null) {
            seedOrUpdateVehicle("Ford", "Transit", "79B-789.01", 2022, "diesel", 8, "kiện", cat16);
            seedOrUpdateVehicle("Hyundai", "Solati", "79B-890.12", 2023, "diesel", 8, "kiện", cat16);
        }
        log.info("Sample vehicles checked and synchronized!");
    }

    private void seedOrUpdateVehicle(String brand, String model, String licensePlate, int year,
                                     String fuelType, int luggage, String luggageUnit, VehicleCategory category) {
        List<Vehicle> existing = vehicleRepository.findAll().stream()
                .filter(v -> (v.getLicensePlate() != null && v.getLicensePlate().equalsIgnoreCase(licensePlate))
                        || (v.getBrand().equalsIgnoreCase(brand) && v.getModel().equalsIgnoreCase(model) && v.getCategory().getId().equals(category.getId())))
                .toList();

        if (existing.isEmpty()) {
            vehicleRepository.save(Vehicle.builder()
                    .brand(brand)
                    .model(model)
                    .licensePlate(licensePlate)
                    .year(year)
                    .fuelType(fuelType)
                    .active(true)
                    .canRent(true)
                    .luggage(luggage)
                    .luggageUnit(luggageUnit)
                    .category(category)
                    .build());
            log.info("Seeded new vehicle: {} {} ({})", brand, model, licensePlate);
        } else {
            Vehicle v = existing.get(0);
            boolean updated = false;
            if (v.getLicensePlate() == null || !v.getLicensePlate().equals(licensePlate)) {
                v.setLicensePlate(licensePlate);
                updated = true;
            }
            if (!v.isCanRent()) {
                v.setCanRent(true);
                updated = true;
            }
            if (updated) {
                vehicleRepository.save(v);
                log.info("Updated vehicle plate: {} {} ({})", brand, model, licensePlate);
            }
        }
    }
}
