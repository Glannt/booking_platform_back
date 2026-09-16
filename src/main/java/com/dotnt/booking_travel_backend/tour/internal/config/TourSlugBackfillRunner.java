package com.dotnt.booking_travel_backend.tour.internal.config;

import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourTranslation;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(100)
public class TourSlugBackfillRunner implements CommandLineRunner {

    private final TourRepository tourRepository;
    private final TourTranslationRepository tourTranslationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            List<Tour> tours = tourRepository.findAll();
            boolean updated = false;
            for (Tour tour : tours) {
                if (tour.getSlug() == null || tour.getSlug().trim().isEmpty()) {
                    List<TourTranslation> translations = tourTranslationRepository.findByTourId(tour.getId());
                    String candidate = translations.stream()
                            .filter(tr -> tr.getSlug() != null && !tr.getSlug().trim().isEmpty())
                            .map(TourTranslation::getSlug)
                            .findFirst()
                            .orElse(null);

                    if (candidate == null || candidate.trim().isEmpty()) {
                        candidate = translations.stream()
                                .filter(tr -> tr.getName() != null && !tr.getName().trim().isEmpty())
                                .map(tr -> slugify(tr.getName()))
                                .findFirst()
                                .orElse("tour-" + tour.getId());
                    }

                    tour.setSlug(candidate.trim());
                    tourRepository.save(tour);
                    updated = true;
                    log.info("Backfilled missing slug '{}' for Tour ID {}", tour.getSlug(), tour.getId());
                }
            }
            if (updated) {
                log.info("Tour slug backfill completed successfully.");
            }
        } catch (Exception e) {
            log.warn("Tour slug backfill skipped or failed: {}", e.getMessage());
        }
    }

    private String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "tour-" + System.currentTimeMillis();
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String noDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase()
                .replace("đ", "d")
                .replace("Đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s_]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
