package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRentalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfDriveRentalPriceRepository extends JpaRepository<SelfDriveRentalPrice, Long> {
    List<SelfDriveRentalPrice> findByRentalId(Long rentalId);

    /** Ordered so a category backed by more than one rental still resolves to the same row every time. */
    List<SelfDriveRentalPrice> findByVehicleCategoryIdAndActiveTrueOrderById(Long vehicleCategoryId);
}
