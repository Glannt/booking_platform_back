package com.dotnt.booking_travel_backend.fleet.internal.strategy;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleMatchingCriteria;
import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;

import java.util.List;

public interface VehicleMatchingStrategy {
    boolean supports(VehicleMatchingCriteria criteria);
    List<Vehicle> match(VehicleMatchingCriteria criteria);
}
