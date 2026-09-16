package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfDriveTypeRepository extends JpaRepository<SelfDriveType, Long> {
}
