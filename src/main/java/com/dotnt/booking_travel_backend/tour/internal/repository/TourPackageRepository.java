package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackage, Long> {
    List<TourPackage> findByTourId(Long tourId);
    List<TourPackage> findByTourIdAndActiveTrueOrderBySortOrderAsc(Long tourId);
}
