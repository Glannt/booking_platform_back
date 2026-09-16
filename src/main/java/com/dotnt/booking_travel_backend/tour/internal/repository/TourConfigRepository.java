package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourConfigRepository extends JpaRepository<TourConfig, Long> {
    Optional<TourConfig> findFirstByOrderByIdAsc();
}
