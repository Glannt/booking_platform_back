package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "airport_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"airport_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class AirportTranslation extends MasterEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
