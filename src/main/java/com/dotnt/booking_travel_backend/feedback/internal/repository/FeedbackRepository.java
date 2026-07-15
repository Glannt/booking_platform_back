package com.dotnt.booking_travel_backend.feedback.internal.repository;

import com.dotnt.booking_travel_backend.feedback.internal.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByShowOnIndexTrueOrderByOrderIndexAsc();
    List<Feedback> findAllByOrderByOrderIndexAsc();
}
