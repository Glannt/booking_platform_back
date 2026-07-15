package com.dotnt.booking_travel_backend.feedback.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackTranslationDto {
    private Long id;
    private String locale;
    private String title;
    private String description;
}
