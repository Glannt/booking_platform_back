package com.dotnt.booking_travel_backend.location.internal.repository;

import com.dotnt.booking_travel_backend.location.internal.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByActiveTrue();

    @Query("""
            SELECT DISTINCT l FROM Location l
            JOIN l.translations t
            WHERE l.active = true
              AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(t.address) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY l.id
            """)
    List<Location> searchAnyLocale(@Param("query") String query);
}
