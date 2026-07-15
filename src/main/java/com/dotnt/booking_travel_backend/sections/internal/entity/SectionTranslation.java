package com.dotnt.booking_travel_backend.sections.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "section_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
public class SectionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "subtitle", length = 500)
    private String subtitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "btn_text", length = 100)
    private String btnText;

    @Column(name = "btn_link", length = 500)
    private String btnLink;
}
