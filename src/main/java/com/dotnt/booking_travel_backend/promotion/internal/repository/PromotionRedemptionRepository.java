package com.dotnt.booking_travel_backend.promotion.internal.repository;

import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionRedemption;
import com.dotnt.booking_travel_backend.shared.enums.RedemptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Long> {

    long countByPromotionIdAndCustomerIdAndStatus(Long promotionId, Long customerId, RedemptionStatus status);

    Optional<PromotionRedemption> findByOrderIdAndPromotionId(Long orderId, Long promotionId);

    List<PromotionRedemption> findByOrderId(Long orderId);

    List<PromotionRedemption> findByPromotionIdOrderByRedeemedAtDesc(Long promotionId);

    Page<PromotionRedemption> findByPromotionIdOrderByRedeemedAtDesc(Long promotionId, Pageable pageable);

    Page<PromotionRedemption> findAllByOrderByRedeemedAtDesc(Pageable pageable);
}
