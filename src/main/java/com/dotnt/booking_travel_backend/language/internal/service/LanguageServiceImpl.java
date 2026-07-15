package com.dotnt.booking_travel_backend.language.internal.service;

import com.dotnt.booking_travel_backend.language.api.CreateLanguageRequest;
import com.dotnt.booking_travel_backend.language.api.LanguageDto;
import com.dotnt.booking_travel_backend.language.internal.entity.Language;
import com.dotnt.booking_travel_backend.language.internal.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    @Transactional
    public LanguageDto createLanguage(CreateLanguageRequest request) {
        if (languageRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Language code already exists: " + request.getCode());
        }

        // If this is set as default, unset previous default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            languageRepository.findAll().forEach(l -> {
                if (Boolean.TRUE.equals(l.getIsDefault())) {
                    l.setIsDefault(false);
                    languageRepository.save(l);
                }
            });
        }

        Language language = Language.builder()
                .code(request.getCode())
                .name(request.getName())
                .flagIcon(request.getFlagIcon())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();

        Language saved = languageRepository.save(language);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageDto> getActiveLanguages() {
        return languageRepository.findAllByStatusOrderByOrderIndexAsc("active").stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageDto> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    private LanguageDto mapToDto(Language language) {
        return LanguageDto.builder()
                .id(language.getId())
                .code(language.getCode())
                .name(language.getName())
                .flagIcon(language.getFlagIcon())
                .isDefault(language.getIsDefault())
                .status(language.getStatus())
                .orderIndex(language.getOrderIndex())
                .build();
    }
}
