package com.dotnt.booking_travel_backend.service.api;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateServiceTypeRequest {
    private String code;
    private String icon;
    private int displayOrder;
    private boolean active;
    private List<ServiceTypeTranslationRequest> translations;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceTypeTranslationRequest {
        private String locale;
        private String name;
        private String seoTitle;
        private String seoDescription;
        private String canonicalUrl;
    }
}

