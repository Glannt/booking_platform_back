package com.dotnt.booking_travel_backend.sections.internal.repository;

import com.dotnt.booking_travel_backend.sections.internal.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByActiveTrueOrderByOrderIndexAsc();

    @Query("SELECT s FROM Section s WHERE s.active = true AND s.pagePath = :pagePath AND (s.startDate IS NULL OR s.startDate <= :now) AND (s.endDate IS NULL OR s.endDate >= :now) ORDER BY s.orderIndex ASC")
    List<Section> findActiveByPagePath(@Param("pagePath") String pagePath, @Param("now") LocalDateTime now);

    List<Section> findByPagePathOrderByOrderIndexAsc(String pagePath);

    boolean existsBySectionType(String sectionType);
}
