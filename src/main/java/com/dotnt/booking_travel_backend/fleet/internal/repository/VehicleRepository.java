package com.dotnt.booking_travel_backend.fleet.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Vehicle extends JpaRepository<com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle, Long> {
}
