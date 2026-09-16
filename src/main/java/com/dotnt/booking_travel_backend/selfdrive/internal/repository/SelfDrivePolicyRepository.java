package com.dotnt.booking_travel_backend.selfdrive.internal.repository;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface SelfDrivePolicyRepository extends JpaRepository<SelfDrivePolicy, Long> {
    List<SelfDrivePolicy> findByActiveTrueOrderBySortOrderAscIdAsc();

    Page<SelfDrivePolicy> findAllByOrderBySortOrderAscIdAsc(Pageable pageable);

    Optional<SelfDrivePolicy> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    SelfDrivePolicy findSelfDrivePolicyBySortOrder(int sortOrder);

    @Query("""
    SELECT MAX(sp.sortOrder) FROM SelfDrivePolicy sp
    """
    )
    Integer findMaxSortOrder();
}
