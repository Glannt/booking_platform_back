package com.dotnt.booking_travel_backend.language.internal.repository;

import com.dotnt.booking_travel_backend.language.internal.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);
    List<Language> findAllByStatusOrderByOrderIndexAsc(String status);
}
