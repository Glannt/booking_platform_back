package com.dotnt.booking_travel_backend.sharedroute.internal.repository;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedRouteRepository extends JpaRepository<SharedRoute, Long> {

    List<SharedRoute> findByActiveTrue();

    Optional<SharedRoute> findBySlug(String slug);

    @Query("SELECT r FROM SharedRoute r LEFT JOIN FETCH r.directions d WHERE r.id = :id")
    Optional<SharedRoute> findByIdWithDirections(@Param("id") Long id);

    @Query("SELECT r FROM SharedRoute r LEFT JOIN FETCH r.directions d WHERE r.slug = :slug")
    Optional<SharedRoute> findBySlugWithDirections(@Param("slug") String slug);

    Optional<SharedRoute> findByLinkedIntercityRouteIdAndActiveTrue(Long linkedIntercityRouteId);

    Optional<SharedRoute> findByLinkedIntercityRouteId(Long linkedIntercityRouteId);
}
