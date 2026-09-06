package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourCategory;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourCategoryRepository extends JpaRepository<TourCategory, Long> {
    @NullMarked
    Optional<TourCategory> findById(Long id);

    Optional<TourCategory> findByCode(String code);

    List<TourCategory> findAllByActiveTrueOrderBySortOrderAsc();

    Page<TourCategory> findAllByOrderBySortOrderAsc(Pageable pageable);

    Page<TourCategory> findAllByActiveTrueOrderBySortOrderAsc(Pageable pageable);
}
