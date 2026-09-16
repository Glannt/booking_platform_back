package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicyTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourPolicyTranslationRepository extends JpaRepository<TourPolicyTranslation, Long> {
    List<TourPolicyTranslation> findByPolicyId(Long policyId);

    Optional<TourPolicyTranslation> findByPolicyIdAndLocale(Long policyId, String locale);

    void deleteByPolicyId(Long policyId);
}
