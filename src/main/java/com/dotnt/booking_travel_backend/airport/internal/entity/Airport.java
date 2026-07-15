package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "airports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport extends MasterEntity {
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @OneToMany(mappedBy = "airport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AirportTranslation> translations = new ArrayList<>();
}
