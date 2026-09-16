package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "price_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceType extends MasterEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "default_name", nullable = false, length = 100)
    private String defaultName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;
}
