package com.dotnt.booking_travel_backend.location.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class LocationTranslation extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
