package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.PriceListRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRouteRepository extends JpaRepository<PriceListRoute, Long> {
    Optional<PriceListRoute> findByPriceListId(Long priceListId);
    List<PriceListRoute> findByRouteId(Long routeId);
    void deleteByPriceListId(Long priceListId);
}
