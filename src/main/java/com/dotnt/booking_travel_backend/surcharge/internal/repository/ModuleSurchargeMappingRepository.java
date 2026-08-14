package com.dotnt.booking_travel_backend.surcharge.internal.repository;

import com.dotnt.booking_travel_backend.surcharge.internal.domain.ModuleSurchargeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleSurchargeMappingRepository extends JpaRepository<ModuleSurchargeMapping, Long> {

    @Query("SELECT m FROM ModuleSurchargeMapping m " +
           "JOIN FETCH m.surchargeRule r " +
           "WHERE m.moduleCode = :moduleCode AND m.active = true AND r.active = true " +
           "ORDER BY m.applyOrder ASC, r.priority DESC")
    List<ModuleSurchargeMapping> findActiveMappingsWithRule(@Param("moduleCode") String moduleCode);

    @Query("SELECT m FROM ModuleSurchargeMapping m " +
           "JOIN FETCH m.surchargeRule r " +
           "WHERE (:moduleCode IS NULL OR m.moduleCode = :moduleCode) " +
           "ORDER BY m.moduleCode ASC, m.applyOrder ASC")
    List<ModuleSurchargeMapping> findAllWithRule(@Param("moduleCode") String moduleCode);

    Optional<ModuleSurchargeMapping> findByModuleCodeAndSurchargeRuleId(String moduleCode, Long surchargeRuleId);

    List<ModuleSurchargeMapping> findBySurchargeRuleId(Long surchargeRuleId);

    void deleteByModuleCodeAndSurchargeRuleId(String moduleCode, Long surchargeRuleId);
}
