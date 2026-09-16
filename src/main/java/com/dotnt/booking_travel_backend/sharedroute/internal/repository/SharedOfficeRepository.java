package com.dotnt.booking_travel_backend.sharedroute.internal.repository;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedOfficeRepository extends JpaRepository<SharedOffice, Long> {
    List<SharedOffice> findByActiveTrue();
    List<SharedOffice> findByCityAndActiveTrue(String city);
}
