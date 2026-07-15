package com.dotnt.booking_travel_backend.feedback.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedbacks")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
@ToString(exclude = {"translations"})
@Builder
public class Feedback extends MasterEntity {

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Integer rating = 5;

    @Column(name = "author", nullable = false, length = 255)
    private String author;

    @Column(name = "avatar_bg", length = 255)
    private String avatarBg;

    @Column(name = "avatar_text", length = 10)
    private String avatarText;

    @Column(name = "route", length = 255)
    private String route;

    @Column(name = "show_on_index", nullable = false)
    @Builder.Default
    private Boolean showOnIndex = false;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedbackTranslation> translations = new ArrayList<>();

}
