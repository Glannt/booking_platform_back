package com.dotnt.booking_travel_backend.sections.internal.repository;

import com.dotnt.booking_travel_backend.sections.internal.entity.SectionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionTranslationRepository extends JpaRepository<SectionTranslation, Long> {

    List<SectionTranslation> findBySectionId(Long sectionId);

    Optional<SectionTranslation> findBySectionIdAndLocale(Long sectionId, String locale);

    void deleteBySectionId(Long sectionId);
}
