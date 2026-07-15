package com.dotnt.booking_travel_backend.shared.service;

import com.dotnt.booking_travel_backend.shared.entity.IdempotencyRecord;
import com.dotnt.booking_travel_backend.shared.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public Optional<IdempotencyRecord> find(String key) {
        return repository.findByIdempotencyKey(key);
    }

    public void save(String key, int httpStatus, String responseBody) {
        repository.save(IdempotencyRecord.builder()
                .idempotencyKey(key)
                .httpStatus(httpStatus)
                .responseBody(responseBody)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        repository.deleteByCreatedAtBefore(cutoff);
        log.debug("Idempotency records older than 24h cleaned up");
    }
}
