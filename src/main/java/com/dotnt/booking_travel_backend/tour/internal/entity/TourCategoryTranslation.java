package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_category_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_category_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"tourCategory"})
@ToString(exclude = {"tourCategory"})
@Builder
public class TourCategoryTranslation extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_category_id", nullable = false)
    private TourCategory tourCategory;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", length = 255)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;
}
