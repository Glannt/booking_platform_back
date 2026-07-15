package com.dotnt.booking_travel_backend.sections.api;

import jakarta.validation.constraints.NotBlank;
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
public class CreateSectionRequest {

    @NotBlank(message = "Section name is required")
    private String sectionName;

    @NotBlank(message = "Section type is required")
    private String sectionType;

    @NotBlank(message = "Page path is required")
    private String pagePath;

    private int orderIndex;
    private boolean active;
    private String iconUrl;
    private List<Long> imageIds;
    private String linkUrl;
    private String textColor;
    private String bgColor;
    private String cssClass;
    private String contentData;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<TranslationRequest> translations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslationRequest {
        @NotBlank(message = "Locale is required")
        private String locale;
        private String title;
        private String subtitle;
        private String description;
        private String btnText;
        private String btnLink;
    }
}
