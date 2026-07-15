package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.RentalTypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalTypeTranslationRepository extends JpaRepository<RentalTypeTranslation, Long> {
    List<RentalTypeTranslation> findByRentalTypeId(Long rentalTypeId);
}

