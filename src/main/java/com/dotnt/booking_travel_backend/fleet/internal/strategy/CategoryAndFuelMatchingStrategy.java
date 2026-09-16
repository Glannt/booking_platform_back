package com.dotnt.booking_travel_backend.fleet.internal.strategy;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(20)
@RequiredArgsConstructor
public class CategoryAndFuelMatchingStrategy implements VehicleMatchingStrategy {

    private final VehicleRepository vehicleRepository;

    @Override
    public boolean supports(VehicleMatchingCriteria criteria) {
        if (criteria == null || criteria.getCategoryId() == null) return false;
        boolean noSeat = criteria.getSeatAddOnId() == null;
        boolean hasFuel = criteria.getFuelAddOnId() != null;
        boolean noAdditional = criteria.getAdditionalAddOnIds() == null || criteria.getAdditionalAddOnIds().isEmpty();
        return noSeat && hasFuel && noAdditional;
    }

    @Override
    public List<Vehicle> match(VehicleMatchingCriteria criteria) {
        return vehicleRepository.findAvailableByCategoryAndFuelAddOn(criteria.getCategoryId(), criteria.getFuelAddOnId());
    }
}
