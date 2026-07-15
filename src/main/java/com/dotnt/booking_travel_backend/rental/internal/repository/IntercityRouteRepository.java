package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercityRouteRepository extends JpaRepository<IntercityRoute, Long> {
    List<IntercityRoute> findByActiveTrue();
}
