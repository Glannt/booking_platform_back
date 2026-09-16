package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceTypeRepository extends JpaRepository<PriceType, Long> {
    Optional<PriceType> findByCode(String code);
    boolean existsByCode(String code);
}
