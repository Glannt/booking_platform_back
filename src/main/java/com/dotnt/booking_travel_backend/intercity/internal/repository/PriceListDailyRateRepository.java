package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.PriceListDailyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListDailyRateRepository extends JpaRepository<PriceListDailyRate, Long> {
    Optional<PriceListDailyRate> findByPriceListId(Long priceListId);
    void deleteByPriceListId(Long priceListId);
}
