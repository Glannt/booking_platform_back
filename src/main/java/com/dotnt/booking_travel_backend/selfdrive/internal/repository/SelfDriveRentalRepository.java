package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfDriveRentalRepository extends JpaRepository<SelfDriveRental, Long> {
    List<SelfDriveRental> findByVehicleCategoryIdAndActiveTrue(Long vehicleCategoryId);
}
