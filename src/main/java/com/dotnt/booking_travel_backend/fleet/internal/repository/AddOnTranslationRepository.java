package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AddOnTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddOnTranslationRepository extends JpaRepository<AddOnTranslation, Long> {
    Optional<AddOnTranslation> findByAddOnIdAndLocale(Long addOnId, String locale);
    List<AddOnTranslation> findByAddOnId(Long addOnId);
}
