package com.dotnt.booking_travel_backend.feedback.internal.service;

import com.dotnt.booking_travel_backend.feedback.api.FeedbackDto;
import com.dotnt.booking_travel_backend.feedback.api.FeedbackTranslationDto;
import com.dotnt.booking_travel_backend.feedback.internal.entity.Feedback;
import com.dotnt.booking_travel_backend.feedback.internal.entity.FeedbackTranslation;
import com.dotnt.booking_travel_backend.feedback.internal.repository.FeedbackRepository;
import com.dotnt.booking_travel_backend.feedback.internal.repository.FeedbackTranslationRepository;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackTranslationRepository feedbackTranslationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getPublicFeedbacks() {
        return feedbackRepository.findByShowOnIndexTrueOrderByOrderIndexAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDto getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public FeedbackDto createFeedback(FeedbackDto dto) {
        Feedback feedback = Feedback.builder()
                .rating(dto.getRating() != null ? dto.getRating() : 5)
                .author(dto.getAuthor())
                .avatarBg(dto.getAvatarBg())
                .avatarText(dto.getAvatarText())
                .route(dto.getRoute())
                .showOnIndex(dto.getShowOnIndex() != null ? dto.getShowOnIndex() : false)
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .translations(new ArrayList<>())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            List<FeedbackTranslation> translations = dto.getTranslations().stream()
                    .map(t -> FeedbackTranslation.builder()
                            .feedback(saved)
                            .locale(t.getLocale())
                            .title(t.getTitle())
                            .description(t.getDescription())
                            .build())
                    .toList();
            feedbackTranslationRepository.saveAll(translations);
            saved.setTranslations(new ArrayList<>(translations));
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public FeedbackDto updateFeedback(Long id, FeedbackDto dto) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + id));

        feedback.setRating(dto.getRating() != null ? dto.getRating() : 5);
        feedback.setAuthor(dto.getAuthor());
        feedback.setAvatarBg(dto.getAvatarBg());
        feedback.setAvatarText(dto.getAvatarText());
        feedback.setRoute(dto.getRoute());
        feedback.setShowOnIndex(dto.getShowOnIndex() != null ? dto.getShowOnIndex() : false);
        feedback.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);

        if (dto.getTranslations() != null) {
            final Feedback finalFeedback = feedback;
            EntityMergeUtils.mergeCollection(
                    feedback.getTranslations(),
                    dto.getTranslations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (existing, tDto) -> {
                        existing.setTitle(tDto.getTitle());
                        existing.setDescription(tDto.getDescription());
                    },
                    tDto -> FeedbackTranslation.builder()
                            .feedback(finalFeedback)
                            .locale(tDto.getLocale().toLowerCase())
                            .title(tDto.getTitle())
                            .description(tDto.getDescription())
                            .build()
            );
        }

        Feedback saved = feedbackRepository.save(feedback);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + id));
        feedbackRepository.delete(feedback);
    }

    private FeedbackDto mapToDto(Feedback entity) {
        List<FeedbackTranslationDto> translations = entity.getTranslations().stream()
                .map(t -> FeedbackTranslationDto.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .build())
                .toList();

        return FeedbackDto.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .author(entity.getAuthor())
                .avatarBg(entity.getAvatarBg())
                .avatarText(entity.getAvatarText())
                .route(entity.getRoute())
                .showOnIndex(entity.getShowOnIndex())
                .orderIndex(entity.getOrderIndex())
                .translations(translations)
                .build();
    }
}
