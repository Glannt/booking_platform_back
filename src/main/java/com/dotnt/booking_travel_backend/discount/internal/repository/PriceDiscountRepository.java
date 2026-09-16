package com.dotnt.booking_travel_backend.discount.internal.repository;

import com.dotnt.booking_travel_backend.shared.entity.PriceDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceDiscountRepository extends JpaRepository<PriceDiscount, Long> {
    Optional<PriceDiscount> findByCodeIgnoreCaseAndActiveTrue(String code);
    Optional<PriceDiscount> findByCodeIgnoreCase(String code);

    List<PriceDiscount> findByScopeTypeAndScopeRefIdAndActiveTrue(String scopeType, String scopeRefId);
    List<PriceDiscount> findByScopeTypeAndScopeRefId(String scopeType, String scopeRefId);

    List<PriceDiscount> findByScopeTypeAndActiveTrue(String scopeType);
    List<PriceDiscount> findByScopeType(String scopeType);

    Page<PriceDiscount> findByScopeType(String scopeType, Pageable pageable);
    Page<PriceDiscount> findByScopeTypeAndScopeRefId(String scopeType, String scopeRefId, Pageable pageable);

    List<PriceDiscount> findByActiveTrue();
}
