package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntercityRouteRepository extends JpaRepository<IntercityRoute, Long> {
    List<IntercityRoute> findByActiveTrue();
    Optional<IntercityRoute> findBySlug(String slug);
}
