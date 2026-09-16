package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourPolicyRepository extends JpaRepository<TourPolicy, Long> {
    List<TourPolicy> findByActiveTrueOrderBySortOrderAscIdAsc();

    Page<TourPolicy> findAllByOrderBySortOrderAscIdAsc(Pageable pageable);

    Optional<TourPolicy> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT MAX(tp.sortOrder) FROM TourPolicy tp")
    Integer findMaxSortOrder();
}
