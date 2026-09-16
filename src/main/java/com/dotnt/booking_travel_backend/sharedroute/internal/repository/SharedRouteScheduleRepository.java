package com.dotnt.booking_travel_backend.sharedroute.internal.repository;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedRouteSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface SharedRouteScheduleRepository extends JpaRepository<SharedRouteSchedule, Long> {

    List<SharedRouteSchedule> findByDirectionIdAndActiveTrueOrderByDepartureTimeAsc(Long directionId);

    List<SharedRouteSchedule> findByDirectionIdOrderByDepartureTimeAsc(Long directionId);

    @Query("SELECT s FROM SharedRouteSchedule s JOIN s.operatingDays d WHERE s.direction.id = :directionId AND s.active = true AND d = :dayOfWeek ORDER BY s.departureTime ASC")
    List<SharedRouteSchedule> findActiveByDirectionIdAndDayOfWeek(@Param("directionId") Long directionId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
