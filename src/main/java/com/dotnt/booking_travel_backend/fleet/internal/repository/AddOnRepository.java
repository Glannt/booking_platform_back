package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AddOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddOnRepository extends JpaRepository<AddOn, Long> {
    Optional<AddOn> findByCode(String code);
    List<AddOn> findByCategoryId(Long categoryId);
    List<AddOn> findByCategoryCode(String categoryCode);
    List<AddOn> findByCategoryCodeAndActiveTrueOrderByDisplayOrderAsc(String categoryCode);
    List<AddOn> findByActiveTrueOrderByDisplayOrderAsc();
    Page<AddOn> findByCategoryId(Long categoryId, Pageable pageable);
}
