package com.dotnt.booking_travel_backend.promotion.internal.repository;

import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionGrant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionGrantRepository extends JpaRepository<PromotionGrant, Long> {

    Optional<PromotionGrant> findByUniqueCodeIgnoreCase(String uniqueCode);

    Optional<PromotionGrant> findByPromotionIdAndCustomerId(Long promotionId, Long customerId);

    @Query("SELECT g FROM PromotionGrant g JOIN FETCH g.promotion p " +
           "WHERE g.customerId = :customerId AND g.used = false " +
           "AND p.active = true " +
           "AND (g.expiresAt IS NULL OR g.expiresAt > :now) " +
           "AND (p.validTo IS NULL OR p.validTo > :now)")
    List<PromotionGrant> findAvailableGrantsForCustomer(@Param("customerId") Long customerId, @Param("now") LocalDateTime now);

    List<PromotionGrant> findByCustomerIdOrderByGrantedAtDesc(Long customerId);

    List<PromotionGrant> findByPromotionIdOrderByGrantedAtDesc(Long promotionId);

    Page<PromotionGrant> findByPromotionIdOrderByGrantedAtDesc(Long promotionId, Pageable pageable);
}
