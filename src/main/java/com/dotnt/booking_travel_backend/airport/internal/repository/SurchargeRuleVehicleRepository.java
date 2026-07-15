package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.SurchargeRuleVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurchargeRuleVehicleRepository extends JpaRepository<SurchargeRuleVehicle, Long> {
    List<SurchargeRuleVehicle> findBySurchargeRuleId(Long surchargeRuleId);
    void deleteBySurchargeRuleId(Long surchargeRuleId);
}
