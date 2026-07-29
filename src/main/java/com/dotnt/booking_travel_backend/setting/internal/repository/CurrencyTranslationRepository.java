package com.dotnt.booking_travel_backend.setting.internal.repository;

import com.dotnt.booking_travel_backend.setting.internal.entity.CurrencyTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyTranslationRepository extends JpaRepository<CurrencyTranslation, Long> {
    List<CurrencyTranslation> findByCurrencyId(Long currencyId);
}
