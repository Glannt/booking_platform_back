package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
}

