package com.dotnt.booking_travel_backend.media.internal.repository;

import com.dotnt.booking_travel_backend.media.internal.entity.ImageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageTranslationRepository extends JpaRepository<ImageTranslation, Long> {
}
