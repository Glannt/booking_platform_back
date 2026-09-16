package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRentalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfDriveRentalPriceRepository extends JpaRepository<SelfDriveRentalPrice, Long> {
    List<SelfDriveRentalPrice> findByRentalId(Long rentalId);
}
