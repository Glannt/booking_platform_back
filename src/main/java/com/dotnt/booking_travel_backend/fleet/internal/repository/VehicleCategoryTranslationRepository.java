package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.api.dtos.VehicleCategoryDTO;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleCategoryTranslationRepository extends JpaRepository<VehicleCategoryTranslation, Long> {
    @Query(value = """
        SELECT vc 
        FROM VehicleCategory vc
        WHERE vc.active = :active
          AND vc.id IN (
              SELECT t.vehicleCategory.id 
              FROM VehicleCategoryTranslation t 
              WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
          )
          OR (:search IS NULL OR :search = '' 
              OR LOWER(vc.code) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
            countQuery = """
        SELECT COUNT(vc) 
        FROM VehicleCategory vc
        WHERE vc.active = :active
          AND vc.id IN (
              SELECT t.vehicleCategory.id 
              FROM VehicleCategoryTranslation t 
              WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
          )
          OR (:search IS NULL OR :search = '' 
              OR LOWER(vc.code) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<VehicleCategory> search(
            @Param("search") String search,
            @Param("active") boolean active,
            Pageable pageable
    );

    java.util.Optional<VehicleCategoryTranslation> findBySlug(String slug);
}
