package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AmenityCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityCategoryRepository extends JpaRepository<AmenityCategory, Long> {
    Optional<AmenityCategory> findByCode(String code);
    List<AmenityCategory> findByActiveTrueOrderByDisplayOrderAsc();
    Page<AmenityCategory> findByCodeContainingIgnoreCase(String code, Pageable pageable);
}
