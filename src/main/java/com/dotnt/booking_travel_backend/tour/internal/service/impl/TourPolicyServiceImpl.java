package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourPolicyDto;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourPolicyTranslationDto;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicy;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicyTranslation;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPolicyRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPolicyTranslationRepository;
import com.dotnt.booking_travel_backend.tour.internal.service.TourPolicyService;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourPolicyServiceImpl implements TourPolicyService {

    private final TourPolicyRepository policyRepository;
    private final TourPolicyTranslationRepository translationRepository;

    @Override
    @Cacheable(value = "tourPolicies", key = "'public_active'")
    public List<TourPolicyDto> getActivePolicies() {
        return policyRepository.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TourPolicyDto> list(Pageable pageable) {
        Page<TourPolicy> page = policyRepository.findAllByOrderBySortOrderAscIdAsc(pageable);
        List<TourPolicyDto> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public TourPolicyDto get(Long id) {
        return policyRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourPolicies", allEntries = true)
    public TourPolicyDto create(TourPolicyDto dto) {
        if (policyRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Mã chính sách đã tồn tại: " + dto.getCode());
        }

        TourPolicy policy = TourPolicy.builder()
                .code(dto.getCode())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder())
                .active(dto.isActive())
                .build();

        TourPolicy saved = policyRepository.save(policy);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            List<TourPolicyTranslation> translations = dto.getTranslations().stream()
                    .map(tr -> TourPolicyTranslation.builder()
                            .policy(saved)
                            .locale(tr.getLocale())
                            .name(tr.getName())
                            .description(tr.getDescription())
                            .build())
                    .collect(Collectors.toList());
            translationRepository.saveAll(translations);
        }

        return get(saved.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourPolicies", allEntries = true)
    public TourPolicyDto update(Long id, TourPolicyDto dto) {
        return policyRepository.findById(id).map(policy -> {
            if (policyRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                throw new IllegalArgumentException("Mã chính sách đã tồn tại: " + dto.getCode());
            }

            policy.setCode(dto.getCode());
            policy.setIcon(dto.getIcon());
            policy.setSortOrder(dto.getSortOrder());
            policy.setActive(dto.isActive());

            if (dto.getTranslations() != null) {
                final TourPolicy finalPolicy = policy;
                EntityMergeUtils.mergeCollection(
                        policy.getTranslations(),
                        dto.getTranslations(),
                        tr -> tr.getLocale() != null ? tr.getLocale().toLowerCase() : null,
                        trDto -> trDto.getLocale() != null ? trDto.getLocale().toLowerCase() : null,
                        (existing, trDto) -> {
                            existing.setName(trDto.getName());
                            existing.setDescription(trDto.getDescription());
                        },
                        trDto -> TourPolicyTranslation.builder()
                                .policy(finalPolicy)
                                .locale(trDto.getLocale().toLowerCase())
                                .name(trDto.getName())
                                .description(trDto.getDescription())
                                .build()
                );
            }

            TourPolicy saved = policyRepository.save(policy);
            return get(saved.getId());
        }).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chính sách với id: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourPolicies", allEntries = true)
    public void delete(Long id) {
        translationRepository.deleteByPolicyId(id);
        policyRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tourPolicies", allEntries = true)
    public TourPolicyDto toggleActive(Long id) {
        return policyRepository.findById(id).map(policy -> {
            policy.setActive(!policy.isActive());
            policyRepository.save(policy);
            return get(id);
        }).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chính sách với id: " + id));
    }

    private TourPolicyDto toDto(TourPolicy entity) {
        List<TourPolicyTranslationDto> translationDtos = translationRepository.findByPolicyId(entity.getId()).stream()
                .map(t -> TourPolicyTranslationDto.builder()
                        .id(t.getId())
                        .policyId(entity.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toList());

        return TourPolicyDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .icon(entity.getIcon())
                .sortOrder(entity.getSortOrder())
                .active(entity.isActive())
                .translations(translationDtos)
                .build();
    }
}
