package com.dotnt.booking_travel_backend.price.internal.repository;

import com.dotnt.booking_travel_backend.price.internal.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByBusinessTypeAndBusinessIdAndVehicleCategoryId(String businessType, Long businessId, Long vehicleCategoryId);
    List<Price> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
}

