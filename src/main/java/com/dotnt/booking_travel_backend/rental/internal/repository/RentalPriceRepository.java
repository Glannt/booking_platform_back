package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.RentalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalPriceRepository extends JpaRepository<RentalPrice, Long> {
    List<RentalPrice> findByRentalId(Long rentalId);
}

