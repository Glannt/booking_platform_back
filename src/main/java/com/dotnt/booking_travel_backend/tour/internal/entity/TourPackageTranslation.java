package com.dotnt.booking_travel_backend.tour.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_package_translations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "tourPackage")
@ToString(exclude = "tourPackage")
@Builder
public class TourPackageTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_package_id", nullable = false)
    private TourPackage tourPackage;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Column(name = "inclusions", columnDefinition = "TEXT")
    private String inclusions;

    @Column(name = "exclusions", columnDefinition = "TEXT")
    private String exclusions;

    @Column(name = "child_policy_note")
    private String childPolicyNote;

    @Column(name = "infant_policy_note")
    private String infantPolicyNote;
}
