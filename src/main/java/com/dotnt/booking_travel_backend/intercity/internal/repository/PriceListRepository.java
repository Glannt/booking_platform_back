package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    List<PriceList> findByScopeTypeAndScopeRefId(String scopeType, String scopeRefId);

    List<PriceList> findByScopeTypeAndScopeRefIdAndStatus(String scopeType, String scopeRefId, String status);

    List<PriceList> findByScopeTypeAndScopeRefIdAndVehicleCategoryId(String scopeType, String scopeRefId, Long vehicleCategoryId);

    Optional<PriceList> findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(
            String scopeType, String scopeRefId, Long vehicleCategoryId, String priceTypeCode);

    Optional<PriceList> findFirstByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCodeAndStatus(
            String scopeType, String scopeRefId, Long vehicleCategoryId, String priceTypeCode, String status);

    List<PriceList> findByScopeType(String scopeType);

    void deleteByScopeTypeAndScopeRefId(String scopeType, String scopeRefId);
}
