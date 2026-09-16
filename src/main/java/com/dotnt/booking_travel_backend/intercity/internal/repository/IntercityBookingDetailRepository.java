package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntercityBookingDetailRepository extends JpaRepository<IntercityBookingDetail, Long> {
}
