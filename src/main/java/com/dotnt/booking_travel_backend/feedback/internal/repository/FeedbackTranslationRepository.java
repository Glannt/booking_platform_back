package com.dotnt.booking_travel_backend.feedback.internal.repository;

import com.dotnt.booking_travel_backend.feedback.internal.entity.FeedbackTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackTranslationRepository extends JpaRepository<FeedbackTranslation, Long> {
}
