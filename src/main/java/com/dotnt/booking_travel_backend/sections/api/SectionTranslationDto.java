package com.dotnt.booking_travel_backend.sections.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionTranslationDto {
    private Long id;
    private String locale;
    private String title;
    private String subtitle;
    private String description;
    private String btnText;
    private String btnLink;
}
