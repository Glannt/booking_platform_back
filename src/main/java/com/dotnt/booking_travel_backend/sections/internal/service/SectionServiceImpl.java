package com.dotnt.booking_travel_backend.sections.internal.service;

import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
import com.dotnt.booking_travel_backend.media.internal.service.MediaService;
import com.dotnt.booking_travel_backend.sections.api.CreateSectionRequest;
import com.dotnt.booking_travel_backend.sections.api.SectionDto;
import com.dotnt.booking_travel_backend.sections.api.SectionTranslationDto;
import com.dotnt.booking_travel_backend.sections.internal.entity.Section;
import com.dotnt.booking_travel_backend.sections.internal.entity.SectionTranslation;
import com.dotnt.booking_travel_backend.sections.internal.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final ImageRepository imageRepository;
    private final MediaService mediaService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sections", key = "#pagePath + '_' + #locale")
    public List<SectionDto> getActiveByPagePath(String pagePath, String locale) {
        List<Section> sections = sectionRepository.findActiveByPagePath(pagePath, LocalDateTime.now());
        return sections.stream().map(s -> mapToDto(s, locale)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sections", key = "#pagePath + '_all'")
    public List<SectionDto> getAllByPagePath(String pagePath) {
        List<Section> sections = sectionRepository.findByPagePathOrderByOrderIndexAsc(pagePath);
        return sections.stream().map(s -> mapToDto(s, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sections", key = "#id")
    public SectionDto getById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + id));
        return mapToDto(section, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sections", allEntries = true)
    public SectionDto create(CreateSectionRequest request) {
        Section section = Section.builder()
                .sectionName(request.getSectionName())
                .sectionType(request.getSectionType())
                .pagePath(request.getPagePath())
                .orderIndex(request.getOrderIndex())
                .active(request.isActive())
                .iconUrl(request.getIconUrl())
                .linkUrl(request.getLinkUrl())
                .textColor(request.getTextColor())
                .bgColor(request.getBgColor())
                .cssClass(request.getCssClass())
                .contentData(request.getContentData())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .translations(new ArrayList<>())
                .build();

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(request.getImageIds());
            String imagesJson = images.stream().map(Image::getId).map(String::valueOf)
                    .reduce((a, b) -> a + "," + b).orElse("");
            section.setImages(imagesJson);
        }

        if (request.getTranslations() != null) {
            for (CreateSectionRequest.TranslationRequest tr : request.getTranslations()) {
                SectionTranslation translation = SectionTranslation.builder()
                        .section(section)
                        .locale(tr.getLocale())
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .btnText(tr.getBtnText())
                        .btnLink(tr.getBtnLink())
                        .build();
                section.getTranslations().add(translation);
            }
        }

        Section saved = sectionRepository.save(section);
        log.info("Created section: {} ({})", saved.getSectionName(), saved.getId());
        return mapToDto(saved, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sections", allEntries = true)
    public SectionDto update(Long id, CreateSectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + id));

        section.setSectionName(request.getSectionName());
        section.setSectionType(request.getSectionType());
        section.setPagePath(request.getPagePath());
        section.setOrderIndex(request.getOrderIndex());
        section.setActive(request.isActive());
        section.setIconUrl(request.getIconUrl());
        section.setLinkUrl(request.getLinkUrl());
        section.setTextColor(request.getTextColor());
        section.setBgColor(request.getBgColor());
        section.setCssClass(request.getCssClass());
        section.setContentData(request.getContentData());
        section.setStartDate(request.getStartDate());
        section.setEndDate(request.getEndDate());

        if (request.getImageIds() != null) {
            List<Long> oldIds = parseImageIds(section.getImages());
            String imagesJson = request.getImageIds().stream().map(String::valueOf)
                    .reduce((a, b) -> a + "," + b).orElse("");
            section.setImages(imagesJson);
            List<Long> newIds = request.getImageIds();
            oldIds.stream().filter(oldId -> !newIds.contains(oldId)).forEach(mediaService::deleteImage);
        }

        section.getTranslations().clear();
        sectionRepository.saveAndFlush(section);
        if (request.getTranslations() != null) {
            for (CreateSectionRequest.TranslationRequest tr : request.getTranslations()) {
                SectionTranslation translation = SectionTranslation.builder()
                        .section(section)
                        .locale(tr.getLocale())
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .btnText(tr.getBtnText())
                        .btnLink(tr.getBtnLink())
                        .build();
                section.getTranslations().add(translation);
            }
        }

        Section saved = sectionRepository.save(section);
        log.info("Updated section: {} ({})", saved.getSectionName(), saved.getId());
        return mapToDto(saved, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sections", allEntries = true)
    public void delete(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + id));
        cleanupSectionImages(section);
        sectionRepository.delete(section);
        log.info("Deleted section: {} ({})", section.getSectionName(), id);
    }

    private List<Long> parseImageIds(String imagesStr) {
        if (imagesStr == null || imagesStr.isBlank()) return List.of();
        return java.util.Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> { try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; } })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private void cleanupSectionImages(Section section) {
        parseImageIds(section.getImages()).forEach(id -> {
            try { mediaService.deleteImage(id); } catch (Exception e) {
                log.warn("Failed to delete image {} during section cleanup: {}", id, e.getMessage());
            }
        });
    }

    private SectionDto mapToDto(Section section, String locale) {
        List<String> imageUrls = new ArrayList<>();
        if (section.getImages() != null && !section.getImages().isEmpty()) {
            imageUrls = java.util.Arrays.stream(section.getImages().split(","))
                    .filter(s -> !s.trim().isEmpty())
                    .map(s -> {
                        try {
                            Long imageId = Long.parseLong(s.trim());
                            return imageRepository.findById(imageId)
                                    .map(Image::getFilePath)
                                    .orElse(null);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(s -> s != null)
                    .toList();
        }

        List<SectionTranslationDto> translations = section.getTranslations().stream()
                .map(t -> SectionTranslationDto.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .title(t.getTitle())
                        .subtitle(t.getSubtitle())
                        .description(t.getDescription())
                        .btnText(t.getBtnText())
                        .btnLink(t.getBtnLink())
                        .build())
                .toList();

        return SectionDto.builder()
                .id(section.getId())
                .sectionName(section.getSectionName())
                .sectionType(section.getSectionType())
                .pagePath(section.getPagePath())
                .orderIndex(section.getOrderIndex())
                .active(section.isActive())
                .iconUrl(section.getIconUrl())
                .imageUrls(imageUrls)
                .linkUrl(section.getLinkUrl())
                .textColor(section.getTextColor())
                .bgColor(section.getBgColor())
                .cssClass(section.getCssClass())
                .contentData(section.getContentData())
                .startDate(section.getStartDate())
                .endDate(section.getEndDate())
                .translations(translations)
                .build();
    }
}
