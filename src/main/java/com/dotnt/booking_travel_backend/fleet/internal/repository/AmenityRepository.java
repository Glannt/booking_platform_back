package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.Amenity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Optional<Amenity> findByCode(String code);
    List<Amenity> findByCategoryId(Long categoryId);
    List<Amenity> findByCategoryCode(String categoryCode);
    List<Amenity> findByActiveTrueOrderByDisplayOrderAsc();
    Page<Amenity> findByCategoryId(Long categoryId, Pageable pageable);
}
