package com.dotnt.booking_travel_backend.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class for all entities in the modulith.
 * Provides common fields: id, timestamps (createdAt, updatedAt), soft delete (deletedAt), and audit tracking (createdBy).
 * Uses SINGLE_TABLE inheritance strategy for JPA.
 * Requires @EnableJpaAuditing configuration to enable automatic timestamp population.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class MasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    /**
     * Temporarily hardcoded. Will be populated from Spring Security context after user module implementation.
     */
    @Column(name = "created_by", length = 255)
    protected String createdBy = "SYSTEM";

}

