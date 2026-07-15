package com.dotnt.booking_travel_backend.fleet.internal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle, Long> {
	Page<com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle>
			findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model, Pageable pageable);
}
