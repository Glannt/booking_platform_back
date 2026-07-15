package com.dotnt.booking_travel_backend.service.api;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTypeTranslationDto {
    private Long id;
    private String locale;
    private String name;
    private String seoTitle;
    private String seoDescription;
    private String canonicalUrl;
}

