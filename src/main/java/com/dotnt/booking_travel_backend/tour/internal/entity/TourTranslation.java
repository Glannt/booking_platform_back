package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tour_id", "locale"}),
        @UniqueConstraint(columnNames = {"locale", "slug"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"tour"})
@ToString(exclude = {"tour"})
@Builder
public class TourTranslation extends MasterEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", length = 255)
    private String slug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
