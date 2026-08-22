package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRouteTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntercityRouteTranslationRepository extends JpaRepository<IntercityRouteTranslation, Long> {
    List<IntercityRouteTranslation> findByRouteId(Long routeId);
    Optional<IntercityRouteTranslation> findBySlug(String slug);
    Optional<IntercityRouteTranslation> findByLocaleAndSlug(String locale, String slug);
}
