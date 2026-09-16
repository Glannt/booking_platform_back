package com.dotnt.booking_travel_backend.promotion.internal.repository;

import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionConditionRepository extends JpaRepository<PromotionCondition, Long> {

    List<PromotionCondition> findByPromotionId(Long promotionId);

    void deleteByPromotionId(Long promotionId);
}
