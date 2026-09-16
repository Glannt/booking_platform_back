package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicyTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfDrivePolicyTranslationRepository extends JpaRepository<SelfDrivePolicyTranslation, Long> {
    List<SelfDrivePolicyTranslation> findByPolicyId(Long policyId);

    void deleteByPolicyId(Long policyId);
}
