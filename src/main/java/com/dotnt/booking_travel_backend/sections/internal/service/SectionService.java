package com.dotnt.booking_travel_backend.sections.internal.service;

import com.dotnt.booking_travel_backend.sections.api.CreateSectionRequest;
import com.dotnt.booking_travel_backend.sections.api.SectionDto;

import java.util.List;

public interface SectionService {
    List<SectionDto> getActiveByPagePath(String pagePath, String locale);
    List<SectionDto> getAllByPagePath(String pagePath);
    SectionDto getById(Long id);
    SectionDto create(CreateSectionRequest request);
    SectionDto update(Long id, CreateSectionRequest request);
    void delete(Long id);
}
