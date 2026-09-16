package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveTypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfDriveTypeTranslationRepository extends JpaRepository<SelfDriveTypeTranslation, Long> {
    List<SelfDriveTypeTranslation> findBySelfDriveTypeId(Long selfDriveTypeId);

    @Query("SELECT t FROM SelfDriveTypeTranslation t WHERE t.selfDriveType.id = :rentalTypeId")
    List<SelfDriveTypeTranslation> findByRentalTypeId(@Param("rentalTypeId") Long rentalTypeId);
}
