package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.PriceListTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceListTranslationRepository extends JpaRepository<PriceListTranslation, Long> {
    List<PriceListTranslation> findByPriceListId(Long priceListId);
    void deleteByPriceListId(Long priceListId);
}
