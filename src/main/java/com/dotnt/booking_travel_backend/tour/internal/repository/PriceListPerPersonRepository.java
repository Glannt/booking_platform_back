package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.PriceListPerPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceListPerPersonRepository extends JpaRepository<PriceListPerPerson, Long> {
    List<PriceListPerPerson> findByActiveTrue();
}
