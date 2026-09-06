package com.dotnt.booking_travel_backend.fleet.api;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FleetFacade {
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository vehicleRepository;

    public Integer getCategoryTotalVehicles(Long id) {
        return vehicleCategoryRepository.findById(id).map(c -> {
            if (c.getTotalVehicles() != null && c.getTotalVehicles() > 0) {
                return c.getTotalVehicles();
            }
            return vehicleRepository.findByCategoryIdAndCanRentTrueAndActiveTrue(id).size();
        }).orElse(0);
    }

    public Integer getCategoryAvailableVehicles(Long id) {
        return vehicleCategoryRepository.findById(id).map(c -> {
            if (c.getAvailableVehicles() != null) {
                return c.getAvailableVehicles();
            }
            return getCategoryTotalVehicles(id);
        }).orElse(0);
    }

    public int countRentableVehiclesByCategory(Long categoryId) {
        return vehicleRepository.findByCategoryIdAndCanRentTrueAndActiveTrue(categoryId).size();
    }

    public Integer getVehicleCategoryMaxSeat(Long id) {
        return vehicleCategoryRepository.findById(id)
                .map(VehicleCategory::getMaxSeat)
                .orElse(null);
    }

    public Integer getVehicleCategoryLuggageWeight(Long id) {
        return vehicleCategoryRepository.findById(id)
                .map(VehicleCategory::getLuggageWeight)
                .orElse(null);
    }

    public String getVehicleCategoryName(Long id, String locale) {
        VehicleCategory category = vehicleCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return "Xe " + id + " chỗ";
        }
        if (category.getTranslations() != null) {
            String targetLocale = (locale == null || locale.isBlank()) ? "vi" : locale;
            return category.getTranslations().stream()
                    .filter(t -> targetLocale.equalsIgnoreCase(t.getLocale()))
                    .map(VehicleCategoryTranslation::getName)
                    .findFirst()
                    .orElseGet(() -> category.getTranslations().stream()
                            .filter(t -> "vi".equalsIgnoreCase(t.getLocale()))
                            .map(VehicleCategoryTranslation::getName)
                            .findFirst()
                            .orElse(category.getCode() != null ? category.getCode() : "Xe " + id + " chỗ"));
        }
        return category.getCode() != null ? category.getCode() : "Xe " + id + " chỗ";
    }
}
