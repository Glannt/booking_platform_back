package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AddOnCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddOnCategoryRepository extends JpaRepository<AddOnCategory, Long> {
    Optional<AddOnCategory> findByCode(String code);
    List<AddOnCategory> findByActiveTrueOrderByDisplayOrderAsc();
    List<AddOnCategory> findByActiveTrueAndIsFilterableTrueOrderByDisplayOrderAsc();
    Page<AddOnCategory> findByCodeContainingIgnoreCase(String code, Pageable pageable);
}
