package com.dotnt.booking_travel_backend.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(nullable = false)
    private Integer httpStatus;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
