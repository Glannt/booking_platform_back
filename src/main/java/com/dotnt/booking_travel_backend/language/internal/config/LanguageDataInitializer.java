package com.dotnt.booking_travel_backend.language.internal.config;

import com.dotnt.booking_travel_backend.language.api.CreateLanguageRequest;
import com.dotnt.booking_travel_backend.language.internal.repository.LanguageRepository;
import com.dotnt.booking_travel_backend.language.internal.service.LanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LanguageDataInitializer implements CommandLineRunner {

    private final LanguageRepository languageRepository;
    private final LanguageService languageService;

    @Override
    public void run(String... args) throws Exception {
        if (languageRepository.count() > 0) {
            log.info("Languages already initialized. Skipping.");
            return;
        }

        log.info("Seeding initial language data...");

        languageService.createLanguage(CreateLanguageRequest.builder()
                .code("vi")
                .name("Tiếng Việt")
                .flagIcon("🇻🇳")
                .isDefault(true)
                .status("active")
                .orderIndex(1)
                .build());

        languageService.createLanguage(CreateLanguageRequest.builder()
                .code("en")
                .name("English")
                .flagIcon("🇬🇧")
                .isDefault(false)
                .status("active")
                .orderIndex(2)
                .build());

        languageService.createLanguage(CreateLanguageRequest.builder()
                .code("ko")
                .name("한국어")
                .flagIcon("🇰🇷")
                .isDefault(false)
                .status("active")
                .orderIndex(3)
                .build());

        languageService.createLanguage(CreateLanguageRequest.builder()
                .code("ru")
                .name("Русский")
                .flagIcon("🇷🇺")
                .isDefault(false)
                .status("active")
                .orderIndex(4)
                .build());

        log.info("Language data seeding completed successfully.");
    }
}
