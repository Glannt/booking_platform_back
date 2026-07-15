package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRouteTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercityRouteTranslationRepository extends JpaRepository<IntercityRouteTranslation, Long> {
    List<IntercityRouteTranslation> findByRouteId(Long routeId);
}
