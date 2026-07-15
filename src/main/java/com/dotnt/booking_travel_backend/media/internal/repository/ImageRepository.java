package com.dotnt.booking_travel_backend.media.internal.repository;

import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
