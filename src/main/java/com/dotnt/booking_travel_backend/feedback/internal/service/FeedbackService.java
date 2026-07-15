package com.dotnt.booking_travel_backend.feedback.internal.service;

import com.dotnt.booking_travel_backend.feedback.api.FeedbackDto;

import java.util.List;

public interface FeedbackService {
    List<FeedbackDto> getPublicFeedbacks();
    List<FeedbackDto> getAllFeedbacks();
    FeedbackDto getFeedbackById(Long id);
    FeedbackDto createFeedback(FeedbackDto dto);
    FeedbackDto updateFeedback(Long id, FeedbackDto dto);
    void deleteFeedback(Long id);
}
