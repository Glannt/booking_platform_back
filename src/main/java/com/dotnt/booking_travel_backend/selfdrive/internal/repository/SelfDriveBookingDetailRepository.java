package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SelfDriveBookingDetailRepository extends JpaRepository<SelfDriveBookingDetail, Long> {

    @Query("SELECT d FROM SelfDriveBookingDetail d WHERE d.vehicleCategoryId = :categoryId AND d.startDate <= :endDate AND d.endDate >= :startDate AND (d.status IS NULL OR LOWER(d.status) <> 'cancelled')")
    List<SelfDriveBookingDetail> findActiveOverlappingDetails(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
