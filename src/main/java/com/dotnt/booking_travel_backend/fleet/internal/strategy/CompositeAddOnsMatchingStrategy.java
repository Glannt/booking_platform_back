package com.dotnt.booking_travel_backend.fleet.internal.strategy;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(30)
@RequiredArgsConstructor
public class CompositeAddOnsMatchingStrategy implements VehicleMatchingStrategy {

    private final VehicleRepository vehicleRepository;

    @Override
    public boolean supports(VehicleMatchingCriteria criteria) {
        if (criteria == null || criteria.getCategoryId() == null) return false;
        Set<Long> allIds = collectAllAddOnIds(criteria);
        return !allIds.isEmpty();
    }

    @Override
    public List<Vehicle> match(VehicleMatchingCriteria criteria) {
        Set<Long> allIds = collectAllAddOnIds(criteria);
        if (allIds.isEmpty()) {
            return vehicleRepository.findAvailableByCategoryId(criteria.getCategoryId());
        }
        return vehicleRepository.findAvailableByCategoryAndAllAddOnIds(
                criteria.getCategoryId(),
                allIds,
                allIds.size()
        );
    }

    private Set<Long> collectAllAddOnIds(VehicleMatchingCriteria criteria) {
        Set<Long> ids = new HashSet<>();
        if (criteria.getSeatAddOnId() != null) ids.add(criteria.getSeatAddOnId());
        if (criteria.getFuelAddOnId() != null) ids.add(criteria.getFuelAddOnId());
        if (criteria.getAdditionalAddOnIds() != null) {
            ids.addAll(criteria.getAdditionalAddOnIds());
        }
        return ids;
    }
}
