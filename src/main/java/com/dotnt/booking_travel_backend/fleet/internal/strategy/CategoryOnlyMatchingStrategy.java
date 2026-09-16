package com.dotnt.booking_travel_backend.fleet.internal.strategy;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(50) // Fallback after specific criteria
@RequiredArgsConstructor
public class CategoryOnlyMatchingStrategy implements VehicleMatchingStrategy {

    private final VehicleRepository vehicleRepository;

    @Override
    public boolean supports(VehicleMatchingCriteria criteria) {
        if (criteria == null || criteria.getCategoryId() == null) return false;
        boolean noSeat = criteria.getSeatAddOnId() == null;
        boolean noFuel = criteria.getFuelAddOnId() == null;
        boolean noAdditional = criteria.getAdditionalAddOnIds() == null || criteria.getAdditionalAddOnIds().isEmpty();
        return noSeat && noFuel && noAdditional;
    }

    @Override
    public List<Vehicle> match(VehicleMatchingCriteria criteria) {
        return vehicleRepository.findAvailableByCategoryId(criteria.getCategoryId());
    }
}
