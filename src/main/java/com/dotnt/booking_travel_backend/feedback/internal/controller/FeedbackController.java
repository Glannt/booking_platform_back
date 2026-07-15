package com.dotnt.booking_travel_backend.feedback.internal.controller;

import com.dotnt.booking_travel_backend.feedback.api.FeedbackDto;
import com.dotnt.booking_travel_backend.feedback.internal.service.FeedbackService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // --- Public API ---
    @GetMapping("/api/v1/feedbacks")
    @SuccessResponse("Feedbacks retrieved successfully!")
    public ResponseEntity<List<FeedbackDto>> getPublicFeedbacks() {
        return ResponseEntity.ok(feedbackService.getPublicFeedbacks());
    }

    // --- Admin API ---
    @GetMapping("/api/v1/admin/feedbacks")
    @SuccessResponse("All feedbacks retrieved successfully!")
    public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/api/v1/admin/feedbacks/{id}")
    @SuccessResponse("Feedback retrieved successfully!")
    public ResponseEntity<FeedbackDto> getFeedbackById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }

    @PreAuthorize("hasAuthority('FEEDBACK_UPDATE')")
    @PostMapping("/api/v1/admin/feedbacks")
    @SuccessResponse("Feedback created successfully!")
    public ResponseEntity<FeedbackDto> createFeedback(@RequestBody FeedbackDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.createFeedback(dto));
    }

    @PreAuthorize("hasAuthority('FEEDBACK_UPDATE')")
    @PutMapping("/api/v1/admin/feedbacks/{id}")
    @SuccessResponse("Feedback updated successfully!")
    public ResponseEntity<FeedbackDto> updateFeedback(@PathVariable Long id, @RequestBody FeedbackDto dto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, dto));
    }

    @PreAuthorize("hasAuthority('FEEDBACK_DELETE')")
    @DeleteMapping("/api/v1/admin/feedbacks/{id}")
    @SuccessResponse("Feedback deleted successfully!")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok().build();
    }
}
