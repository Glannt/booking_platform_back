package com.dotnt.booking_travel_backend.feedback.api;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private Long id;
    private Integer rating;
    private String author;
    private String avatarBg;
    private String avatarText;
    private String route;
    private Boolean showOnIndex;
    private Integer orderIndex;
    private List<FeedbackTranslationDto> translations;
}
