package com.dotnt.booking_travel_backend.surcharge.internal.repository;

import com.dotnt.booking_travel_backend.surcharge.internal.domain.SurchargeRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurchargeRuleRepository extends JpaRepository<SurchargeRule, Long> {

    List<SurchargeRule> findByActiveTrue();

    List<SurchargeRule> findByCodeAndActiveTrue(String code);

    @Query("SELECT r FROM SurchargeRule r WHERE " +
           "(CAST(:code AS string) IS NULL OR :code = '' OR LOWER(r.code) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')) OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%'))) AND " +
           "(:active IS NULL OR r.active = :active)")
    Page<SurchargeRule> findWithFilters(@Param("code") String code,
                                        @Param("active") Boolean active,
                                        Pageable pageable);
}
