package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AddOnCategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddOnCategoryTranslationRepository extends JpaRepository<AddOnCategoryTranslation, Long> {
    Optional<AddOnCategoryTranslation> findByAddonCategoryIdAndLocale(Long addonCategoryId, String locale);
    List<AddOnCategoryTranslation> findByAddonCategoryId(Long addonCategoryId);
}
