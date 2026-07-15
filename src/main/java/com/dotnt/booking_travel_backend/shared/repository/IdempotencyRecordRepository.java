package com.dotnt.booking_travel_backend.shared.repository;

import com.dotnt.booking_travel_backend.shared.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
