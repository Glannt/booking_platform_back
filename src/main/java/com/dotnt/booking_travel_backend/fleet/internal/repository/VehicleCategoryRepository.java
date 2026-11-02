package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    Optional<VehicleCategory> findById(Long id);
    Optional<VehicleCategory> findByCode(String code);
}
