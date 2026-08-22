package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "intercity_route_translations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"route_id", "locale"}),
                @UniqueConstraint(columnNames = {"locale", "slug"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRouteTranslation extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private IntercityRoute route;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", length = 255)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;
}
