package com.dotnt.booking_travel_backend.promotion.internal.repository;

import com.dotnt.booking_travel_backend.promotion.internal.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    Optional<Promotion> findByCodeIgnoreCaseAndActiveTrue(String code);

    Optional<Promotion> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Modifying
    @Query("UPDATE Promotion p SET p.usageCount = p.usageCount + 1 WHERE p.id = :id AND (p.maxUsageTotal IS NULL OR p.usageCount < p.maxUsageTotal)")
    int incrementUsageCountAtomic(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Promotion p SET p.usageCount = CASE WHEN p.usageCount > 0 THEN p.usageCount - 1 ELSE 0 END WHERE p.id = :id")
    int decrementUsageCountAtomic(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Promotion p LEFT JOIN FETCH p.scopes WHERE p.active = true " +
           "AND (p.validFrom <= :now) AND (p.validTo IS NULL OR p.validTo > :now) " +
           "AND (p.maxUsageTotal IS NULL OR p.usageCount < p.maxUsageTotal)")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT p FROM Promotion p LEFT JOIN FETCH p.scopes WHERE p.id = :id")
    Optional<Promotion> findByIdWithScopes(@Param("id") Long id);

    @Query("SELECT p FROM Promotion p ORDER BY p.createdAt DESC")
    Page<Promotion> findAllOrderByCreatedAtDesc(Pageable pageable);
}
