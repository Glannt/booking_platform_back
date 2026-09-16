package com.dotnt.booking_travel_backend.fleet.internal.strategy;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleMatchingContext {

    private final List<VehicleMatchingStrategy> strategies;

    public List<Vehicle> executeMatching(VehicleMatchingCriteria criteria) {
        if (criteria == null || criteria.getCategoryId() == null) {
            log.warn("Vehicle matching criteria missing categoryId");
            return Collections.emptyList();
        }

        for (VehicleMatchingStrategy strategy : strategies) {
            if (strategy.supports(criteria)) {
                log.info("Matching vehicle using strategy: {}", strategy.getClass().getSimpleName());
                return strategy.match(criteria);
            }
        }

        log.warn("No suitable strategy found for criteria: {}", criteria);
        return Collections.emptyList();
    }
}
