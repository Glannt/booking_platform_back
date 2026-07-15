package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.SurchargeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurchargeRuleRepository extends JpaRepository<SurchargeRule, Long> {
    List<SurchargeRule> findByCountryAndActiveTrue(String country);
}
