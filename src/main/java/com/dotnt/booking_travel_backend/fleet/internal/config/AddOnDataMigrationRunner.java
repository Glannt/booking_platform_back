package com.dotnt.booking_travel_backend.fleet.internal.config;

import com.dotnt.booking_travel_backend.fleet.internal.entity.*;
import com.dotnt.booking_travel_backend.fleet.internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddOnDataMigrationRunner implements ApplicationRunner {

    private final AddOnCategoryRepository addOnCategoryRepository;
    private final AddOnRepository addOnRepository;
    private final AmenityCategoryRepository amenityCategoryRepository;
    private final AmenityRepository amenityRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            log.info("Checking Add-on & Amenity seed and migration...");
            seedAddOnCategoriesAndAddOns();
            seedAmenityCategoriesAndAmenities();
            migrateExistingVehiclesAndCategories();
            log.info("Add-on & Amenity migration check completed successfully.");
        } catch (Exception e) {
            log.error("Error during Add-on & Amenity migration: {}", e.getMessage(), e);
        }
    }

    private void seedAddOnCategoriesAndAddOns() {
        log.info("Ensuring default AddOnCategories and AddOns with translations...");

        // 1. SEAT Category
        AddOnCategory seatCat = getOrCreateAddOnCategory("SEAT", "users", 1, true,
                "Số chỗ ngồi", "Seat Capacity", "좌석 수", "Количество мест",
                "Số lượng ghế ngồi của xe", "Number of seats in the vehicle", "차량 좌석 수", "Количество посадочных мест");

        createAddOn(seatCat, "SEAT_4", "4", "users", 1, "4 chỗ", "4 seats", "4인승", "4 места");
        createAddOn(seatCat, "SEAT_5", "5", "users", 2, "5 chỗ", "5 seats", "5인승", "5 мест");
        createAddOn(seatCat, "SEAT_7", "7", "users", 3, "7 chỗ", "7 seats", "7인승", "7 мест");
        createAddOn(seatCat, "SEAT_16", "16", "users", 4, "16 chỗ", "16 seats", "16인승", "16 мест");

        // 2. FUEL Category
        AddOnCategory fuelCat = getOrCreateAddOnCategory("FUEL", "fuel", 2, true,
                "Nhiên liệu", "Fuel Type", "연료 종류", "Тип топлива",
                "Loại nhiên liệu tiêu thụ của xe", "Vehicle fuel consumption type", "차량 연료 소비 유형", "Тип потребляемого топлива");

        createAddOn(fuelCat, "FUEL_PETROL", "petrol", "fuel", 1, "Xăng", "Gasoline", "휘발유", "Бензин");
        createAddOn(fuelCat, "FUEL_DIESEL", "diesel", "fuel", 2, "Dầu Diesel", "Diesel", "디젤", "Дизель");
        createAddOn(fuelCat, "FUEL_ELECTRIC", "electric", "zap", 3, "Điện", "Electric", "전기", "Электро");
        createAddOn(fuelCat, "FUEL_HYBRID", "hybrid", "battery-charging", 4, "Hybrid", "Hybrid", "하이브리드", "Гибрид");

        // 3. TRANSMISSION Category
        AddOnCategory transCat = getOrCreateAddOnCategory("TRANSMISSION", "cog", 3, false,
                "Hộp số", "Transmission", "변속기", "Трансмиссия",
                "Loại hộp số truyền động", "Transmission gear type", "변속기 유형", "Тип коробки передач");

        createAddOn(transCat, "TRANS_AUTO", "auto", "cog", 1, "Tự động", "Automatic", "자동", "Автомат");
        createAddOn(transCat, "TRANS_MANUAL", "manual", "cog", 2, "Số sàn", "Manual", "수동", "Механика");

        // 4. LUGGAGE Category
        AddOnCategory lugCat = getOrCreateAddOnCategory("LUGGAGE", "briefcase", 4, false,
                "Hành lý", "Luggage", "수하물", "Багаж",
                "Sức chứa hành lý", "Luggage capacity", "수하물 용량", "Вместимость багажа");

        createAddOn(lugCat, "LUGGAGE_2", "2", "briefcase", 1, "2 vali", "2 bags", "가방 2개", "2 чемодана");
        createAddOn(lugCat, "LUGGAGE_4", "4", "briefcase", 2, "4 vali", "4 bags", "가방 4개", "4 чемодана");
        createAddOn(lugCat, "LUGGAGE_6", "6", "briefcase", 3, "6 vali", "6 bags", "가방 6개", "6 чемоданов");

        // 5. YEAR Category
        AddOnCategory yearCat = getOrCreateAddOnCategory("YEAR", "calendar", 5, false,
                "Năm sản xuất", "Model Year", "연식", "Год выпуска",
                "Năm sản xuất của xe", "Manufacturing year of vehicle", "차량 제조 연도", "Год выпуска автомобиля");

        createAddOn(yearCat, "YEAR_2023", "2023", "calendar", 1, "2023", "2023", "2023년", "2023 г.");
        createAddOn(yearCat, "YEAR_2024", "2024", "calendar", 2, "2024", "2024", "2024년", "2024 г.");
        createAddOn(yearCat, "YEAR_2025", "2025", "calendar", 3, "2025", "2025", "2025년", "2025 г.");
    }

    private void seedAmenityCategoriesAndAmenities() {
        log.info("Ensuring default AmenityCategories and all 18 Amenities with translations...");

        // 1. TECH Category
        AmenityCategory techCat = getOrCreateAmenityCategory("TECH", "cpu", 1,
                "Công nghệ & Đa phương tiện", "Technology & Media", "기술 및 미디어", "Технологии и мультимедиа",
                "Trang bị công nghệ trên xe", "In-car technology equipment", "차량 내 기술 장비", "Бортовые технологии");

        createAmenity(techCat, "GPS", "navigation", 1, "Định vị GPS", "GPS Navigation", "GPS 내비게이션", "GPS-навигация");
        createAmenity(techCat, "BLUETOOTH", "bluetooth", 2, "Kết nối Bluetooth", "Bluetooth Audio", "블루투스", "Bluetooth");
        createAmenity(techCat, "APPLE_CARPLAY", "smartphone", 3, "Apple CarPlay / Android Auto", "Apple CarPlay / Android Auto", "애플 카플레이 / 안드로이드 오토", "Apple CarPlay / Android Auto");
        createAmenity(techCat, "USB_CHARGER", "zap", 4, "Cổng sạc USB", "USB Charging", "USB 충전 포트", "USB-зарядка");
        createAmenity(techCat, "ETC", "tag", 5, "Thu phí tự động ETC", "ETC Toll Tag", "ETC 자동 톨게이트", "Автооплата проезда ETC");

        // 2. SAFETY Category
        AmenityCategory safetyCat = getOrCreateAmenityCategory("SAFETY", "shield-check", 2,
                "An toàn & Hỗ trợ lái", "Safety & Driver Assist", "안전 및 운전 보조", "Безопасность и ассистенты",
                "Hệ thống an toàn", "Vehicle safety systems", "차량 안전 시스템", "Системы безопасности");

        createAmenity(safetyCat, "BACKUP_CAMERA", "camera", 1, "Camera lùi", "Rearview Camera", "후방 카메라", "Камера заднего вида");
        createAmenity(safetyCat, "DASH_CAM", "video", 2, "Camera hành trình", "Dashcam", "블랙박스", "Видеорегистратор");
        createAmenity(safetyCat, "AIRBAG_7", "shield", 3, "7 túi khí an toàn", "7 Safety Airbags", "7 에어백", "7 подушек безопасности");
        createAmenity(safetyCat, "AIRBAG_6", "shield", 4, "6 túi khí an toàn", "6 Safety Airbags", "6 에어백", "6 подушек безопасности");
        createAmenity(safetyCat, "AIRBAG_4", "shield", 5, "4 túi khí an toàn", "4 Safety Airbags", "4 에어백", "4 подушки безопасности");
        createAmenity(safetyCat, "AIRBAG_2", "shield", 6, "2 túi khí an toàn", "2 Safety Airbags", "2 에어백", "2 подушки безопасности");
        createAmenity(safetyCat, "CRUISE_CONTROL", "gauge", 7, "Ga tự động (Cruise Control)", "Cruise Control", "크루즈 컨트롤", "Круиз-контроль");
        createAmenity(safetyCat, "PARKING_SENSOR", "radar", 8, "Cảm biến lùi / va chạm", "Parking Sensors", "주차 감지 센서", "Датчики парковки");
        createAmenity(safetyCat, "TIRE_PRESSURE", "disc", 9, "Cảm biến áp suất lốp", "Tire Pressure Monitor", "타이어 공기압 센서", "Контроль давления в шинах");
        createAmenity(safetyCat, "SPARE_TIRE", "wrench", 10, "Lốp dự phòng & bộ dụng cụ", "Spare Tire & Tools", "스페어 타이어 및 공구", "Запасное колесо и инструменты");
        createAmenity(safetyCat, "CHILD_SEAT", "baby", 11, "Móc khóa ghế trẻ em Isofix", "Child Safety Seat Isofix", "어린이 카시트 Isofix", "Крепление Isofix");

        // 3. COMFORT Category
        AmenityCategory comfortCat = getOrCreateAmenityCategory("COMFORT", "sparkles", 3,
                "Tiện nghi nội thất", "Comfort & Convenience", "실내 편의 장치", "Комфорт салона",
                "Tiện nghi bên trong xe", "In-cabin comfort items", "차량 내부 편의 사양", "Элементы комфорта");

        createAmenity(comfortCat, "LEATHER_SEATS", "armchair", 1, "Ghế da cao cấp", "Leather Seats", "가죽 시트", "Кожаные сиденья");
        createAmenity(comfortCat, "SUNROOF", "sun", 2, "Cửa sổ trời", "Sunroof", "선루프", "Люк на крыше");
    }

    private AddOnCategory getOrCreateAddOnCategory(String code, String icon, int order, boolean filterable,
                                                   String viName, String enName, String koName, String ruName,
                                                   String viDesc, String enDesc, String koDesc, String ruDesc) {
        Optional<AddOnCategory> opt = addOnCategoryRepository.findByCode(code);
        if (opt.isPresent()) {
            AddOnCategory cat = opt.get();
            if (cat.getTranslations() == null || cat.getTranslations().isEmpty()) {
                cat.addTranslation(AddOnCategoryTranslation.builder().locale("vi").name(viName).description(viDesc).build());
                cat.addTranslation(AddOnCategoryTranslation.builder().locale("en").name(enName).description(enDesc).build());
                cat.addTranslation(AddOnCategoryTranslation.builder().locale("ko").name(koName).description(koDesc).build());
                cat.addTranslation(AddOnCategoryTranslation.builder().locale("ru").name(ruName).description(ruDesc).build());
                return addOnCategoryRepository.save(cat);
            }
            return cat;
        }
        AddOnCategory newCat = AddOnCategory.builder()
                .code(code)
                .icon(icon)
                .displayOrder(order)
                .active(true)
                .isFilterable(filterable)
                .build();
        newCat.addTranslation(AddOnCategoryTranslation.builder().locale("vi").name(viName).description(viDesc).build());
        newCat.addTranslation(AddOnCategoryTranslation.builder().locale("en").name(enName).description(enDesc).build());
        newCat.addTranslation(AddOnCategoryTranslation.builder().locale("ko").name(koName).description(koDesc).build());
        newCat.addTranslation(AddOnCategoryTranslation.builder().locale("ru").name(ruName).description(ruDesc).build());
        return addOnCategoryRepository.save(newCat);
    }

    private AmenityCategory getOrCreateAmenityCategory(String code, String icon, int order,
                                                       String viName, String enName, String koName, String ruName,
                                                       String viDesc, String enDesc, String koDesc, String ruDesc) {
        Optional<AmenityCategory> opt = amenityCategoryRepository.findByCode(code);
        if (opt.isPresent()) {
            AmenityCategory cat = opt.get();
            if (cat.getTranslations() == null || cat.getTranslations().isEmpty()) {
                cat.addTranslation(AmenityCategoryTranslation.builder().locale("vi").name(viName).description(viDesc).build());
                cat.addTranslation(AmenityCategoryTranslation.builder().locale("en").name(enName).description(enDesc).build());
                cat.addTranslation(AmenityCategoryTranslation.builder().locale("ko").name(koName).description(koDesc).build());
                cat.addTranslation(AmenityCategoryTranslation.builder().locale("ru").name(ruName).description(ruDesc).build());
                return amenityCategoryRepository.save(cat);
            }
            return cat;
        }
        AmenityCategory newCat = AmenityCategory.builder()
                .code(code)
                .icon(icon)
                .displayOrder(order)
                .active(true)
                .build();
        newCat.addTranslation(AmenityCategoryTranslation.builder().locale("vi").name(viName).description(viDesc).build());
        newCat.addTranslation(AmenityCategoryTranslation.builder().locale("en").name(enName).description(enDesc).build());
        newCat.addTranslation(AmenityCategoryTranslation.builder().locale("ko").name(koName).description(koDesc).build());
        newCat.addTranslation(AmenityCategoryTranslation.builder().locale("ru").name(ruName).description(ruDesc).build());
        return amenityCategoryRepository.save(newCat);
    }

    private void createAddOn(AddOnCategory category, String code, String value, String icon, int order,
                             String viName, String enName, String koName, String ruName) {
        Optional<AddOn> existing = addOnRepository.findByCode(code);
        if (existing.isPresent()) {
            AddOn addOn = existing.get();
            if (addOn.getTranslations() == null || addOn.getTranslations().isEmpty()) {
                addOn.addTranslation(AddOnTranslation.builder().locale("vi").name(viName).build());
                addOn.addTranslation(AddOnTranslation.builder().locale("en").name(enName).build());
                addOn.addTranslation(AddOnTranslation.builder().locale("ko").name(koName).build());
                addOn.addTranslation(AddOnTranslation.builder().locale("ru").name(ruName).build());
                addOnRepository.save(addOn);
            }
            return;
        }

        AddOn addOn = AddOn.builder()
                .category(category)
                .code(code)
                .value(value)
                .icon(icon)
                .displayOrder(order)
                .active(true)
                .build();

        addOn.addTranslation(AddOnTranslation.builder().locale("vi").name(viName).build());
        addOn.addTranslation(AddOnTranslation.builder().locale("en").name(enName).build());
        addOn.addTranslation(AddOnTranslation.builder().locale("ko").name(koName).build());
        addOn.addTranslation(AddOnTranslation.builder().locale("ru").name(ruName).build());
        addOnRepository.save(addOn);
    }

    private void createAmenity(AmenityCategory category, String code, String icon, int order,
                               String viName, String enName, String koName, String ruName) {
        Optional<Amenity> existing = amenityRepository.findByCode(code);
        if (existing.isPresent()) {
            Amenity amenity = existing.get();
            if (amenity.getTranslations() == null || amenity.getTranslations().isEmpty()) {
                amenity.addTranslation(AmenityTranslation.builder().locale("vi").name(viName).build());
                amenity.addTranslation(AmenityTranslation.builder().locale("en").name(enName).build());
                amenity.addTranslation(AmenityTranslation.builder().locale("ko").name(koName).build());
                amenity.addTranslation(AmenityTranslation.builder().locale("ru").name(ruName).build());
                amenityRepository.save(amenity);
            }
            return;
        }

        Amenity amenity = Amenity.builder()
                .category(category)
                .code(code)
                .icon(icon)
                .displayOrder(order)
                .active(true)
                .build();

        amenity.addTranslation(AmenityTranslation.builder().locale("vi").name(viName).build());
        amenity.addTranslation(AmenityTranslation.builder().locale("en").name(enName).build());
        amenity.addTranslation(AmenityTranslation.builder().locale("ko").name(koName).build());
        amenity.addTranslation(AmenityTranslation.builder().locale("ru").name(ruName).build());
        amenityRepository.save(amenity);
    }

    private void migrateExistingVehiclesAndCategories() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Amenity> allAmenities = amenityRepository.findAll();

        for (Vehicle v : vehicles) {
            boolean modified = false;

            // 1. Link Seat AddOn from category minSeat/maxSeat
            if (v.getCategory() != null && v.getAddOns().stream().noneMatch(a -> "SEAT".equals(a.getCategory().getCode()))) {
                int seats = v.getCategory().getMaxSeat() != null ? v.getCategory().getMaxSeat() : 4;
                String seatCode = "SEAT_" + seats;
                AddOn seatAddOn = addOnRepository.findByCode(seatCode).orElse(null);
                if (seatAddOn != null) {
                    v.addAddOn(seatAddOn);
                    modified = true;
                }
            }

            // 2. Link Fuel AddOn from vehicle fuelType
            if (v.getFuelType() != null && v.getAddOns().stream().noneMatch(a -> "FUEL".equals(a.getCategory().getCode()))) {
                String ft = v.getFuelType().toLowerCase();
                String fuelCode = ft.contains("diesel") || ft.contains("dầu") ? "FUEL_DIESEL"
                        : ft.contains("elec") || ft.contains("điện") ? "FUEL_ELECTRIC"
                        : ft.contains("hyb") ? "FUEL_HYBRID" : "FUEL_PETROL";
                AddOn fuelAddOn = addOnRepository.findByCode(fuelCode).orElse(null);
                if (fuelAddOn != null) {
                    v.addAddOn(fuelAddOn);
                    modified = true;
                }
            }

            // 3. Link Default Transmission
            if (v.getAddOns().stream().noneMatch(a -> "TRANSMISSION".equals(a.getCategory().getCode()))) {
                addOnRepository.findByCode("TRANS_AUTO").ifPresent(v::addAddOn);
                modified = true;
            }

            // 4. Link default amenities if vehicle has none or few
            if (v.getAmenities().size() < 6 && !allAmenities.isEmpty()) {
                for (Amenity am : allAmenities) {
                    if (List.of("GPS", "BACKUP_CAMERA", "BLUETOOTH", "AIRBAG_7", "APPLE_CARPLAY", "USB_CHARGER", "DASH_CAM", "ETC", "CRUISE_CONTROL").contains(am.getCode())) {
                        if (!v.getAmenities().contains(am)) {
                            v.addAmenity(am);
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                vehicleRepository.save(v);
            }
        }

        // Also ensure VehicleCategory has corresponding Seat AddOns
        List<VehicleCategory> categories = vehicleCategoryRepository.findAll();
        for (VehicleCategory cat : categories) {
            int seats = cat.getMaxSeat() != null ? cat.getMaxSeat() : 4;
            String seatCode = "SEAT_" + seats;
            addOnRepository.findByCode(seatCode).ifPresent(seatAddOn -> {
                if (!cat.getAddOns().contains(seatAddOn)) {
                    cat.addAddOn(seatAddOn);
                    vehicleCategoryRepository.save(cat);
                }
            });
        }
    }
}
