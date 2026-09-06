package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourPackageTranslationRepository extends JpaRepository<TourPackageTranslation, Long> {
    List<TourPackageTranslation> findByTourPackageId(Long tourPackageId);
}
