package com.dotnt.booking_travel_backend.promotion.internal.repository;

import com.dotnt.booking_travel_backend.promotion.internal.entity.PromotionScope;
import com.dotnt.booking_travel_backend.shared.enums.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionScopeRepository extends JpaRepository<PromotionScope, Long> {

    List<PromotionScope> findByPromotionId(Long promotionId);

    List<PromotionScope> findByScopeType(ScopeType scopeType);

    List<PromotionScope> findByScopeTypeAndScopeRefId(ScopeType scopeType, String scopeRefId);

    void deleteByPromotionId(Long promotionId);
}
