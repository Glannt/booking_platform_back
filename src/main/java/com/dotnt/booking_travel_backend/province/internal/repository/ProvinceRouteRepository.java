package com.dotnt.booking_travel_backend.province.internal.repository;

import com.dotnt.booking_travel_backend.province.internal.entity.ProvinceRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRouteRepository extends JpaRepository<ProvinceRoute, Long> {
}

