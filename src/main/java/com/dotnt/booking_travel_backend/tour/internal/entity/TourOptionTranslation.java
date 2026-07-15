package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_option_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_option_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"tourOption"})
@ToString(exclude = {"tourOption"})
@Builder
public class TourOptionTranslation extends MasterEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_option_id", nullable = false)
    private TourOption tourOption;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "content", nullable = false, length = 500)
    private String content;
}
