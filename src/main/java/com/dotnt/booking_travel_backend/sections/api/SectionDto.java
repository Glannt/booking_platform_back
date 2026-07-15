package com.dotnt.booking_travel_backend.sections.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDto {
    private Long id;
    private String sectionName;
    private String sectionType;
    private String pagePath;
    private int orderIndex;
    private boolean active;
    private String iconUrl;
    private List<String> imageUrls;
    private String linkUrl;
    private String textColor;
    private String bgColor;
    private String cssClass;
    private String contentData;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<SectionTranslationDto> translations;
}
