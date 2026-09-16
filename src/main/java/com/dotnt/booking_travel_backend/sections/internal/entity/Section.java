package com.dotnt.booking_travel_backend.sections.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class Section extends MasterEntity {

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    @Column(name = "section_type", nullable = false, length = 50)
    private String sectionType;

    @Column(name = "page_path", nullable = false, length = 255)
    private String pagePath;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @Column(name = "icon_url", columnDefinition = "TEXT")
    private String iconUrl;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "text_color", length = 20)
    private String textColor;

    @Column(name = "bg_color", length = 20)
    private String bgColor;

    @Column(name = "css_class", length = 255)
    private String cssClass;

    @Column(name = "content_data", columnDefinition = "TEXT")
    private String contentData;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SectionTranslation> translations = new ArrayList<>();
}
