package com.dotnt.booking_travel_backend.selfdrive.internal.service.impl;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDrivePolicyDto;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDrivePolicyTranslationDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicy;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicyTranslation;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDrivePolicyRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDrivePolicyTranslationRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.ISelfDrivePolicyService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelfDrivePolicyServiceImpl implements ISelfDrivePolicyService {

    private final SelfDrivePolicyRepository policyRepository;
    private final SelfDrivePolicyTranslationRepository translationRepository;

    @Override
    @Cacheable(value = "selfDrivePolicies", key = "'public_active'")
    public List<SelfDrivePolicyDto> getActivePolicies() {
        return policyRepository.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SelfDrivePolicyDto> list(Pageable pageable) {
        Page<SelfDrivePolicy> page = policyRepository.findAllByOrderBySortOrderAscIdAsc(pageable);
        List<SelfDrivePolicyDto> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public SelfDrivePolicyDto get(Long id) {
        return policyRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "selfDrivePolicies", allEntries = true)
    public SelfDrivePolicyDto create(SelfDrivePolicyDto dto) {
        if (policyRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Mã chính sách đã tồn tại: " + dto.getCode());
        }
         SelfDrivePolicy existSortOrderSelfDrivePolicy = policyRepository.findSelfDrivePolicyBySortOrder(dto.getSortOrder());
        if (existSortOrderSelfDrivePolicy != null) {
             Integer maxSortOrder = policyRepository.findMaxSortOrder();
            existSortOrderSelfDrivePolicy.setSortOrder(maxSortOrder + 1);
            policyRepository.save(existSortOrderSelfDrivePolicy);
        }

        SelfDrivePolicy policy = SelfDrivePolicy.builder()
                .code(dto.getCode())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder())
                .active(dto.isActive())
                .build();

        SelfDrivePolicy saved = policyRepository.save(policy);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            List<SelfDrivePolicyTranslation> translations = dto.getTranslations().stream()
                    .map(tr -> SelfDrivePolicyTranslation.builder()
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
    @CacheEvict(value = "selfDrivePolicies", allEntries = true)
    public SelfDrivePolicyDto update(Long id, SelfDrivePolicyDto dto) {
        return policyRepository.findById(id).map(policy -> {
            if (policyRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                throw new IllegalArgumentException("Mã chính sách đã tồn tại: " + dto.getCode());
            }
            SelfDrivePolicy existSortOrderSelfDrivePolicy = policyRepository.findSelfDrivePolicyBySortOrder(dto.getSortOrder());
            if (existSortOrderSelfDrivePolicy != null && !existSortOrderSelfDrivePolicy.getId().equals(id)) {
                existSortOrderSelfDrivePolicy.setSortOrder(policy.getSortOrder());
                policyRepository.save(existSortOrderSelfDrivePolicy);
            }

            policy.setCode(dto.getCode());
            policy.setIcon(dto.getIcon());
            policy.setSortOrder(dto.getSortOrder());
            policy.setActive(dto.isActive());

            if (dto.getTranslations() != null) {
                final SelfDrivePolicy finalPolicy = policy;
                EntityMergeUtils.mergeCollection(
                        policy.getTranslations(),
                        dto.getTranslations(),
                        tr -> tr.getLocale() != null ? tr.getLocale().toLowerCase() : null,
                        trDto -> trDto.getLocale() != null ? trDto.getLocale().toLowerCase() : null,
                        (existing, trDto) -> {
                            existing.setName(trDto.getName());
                            existing.setDescription(trDto.getDescription());
                        },
                        trDto -> SelfDrivePolicyTranslation.builder()
                                .policy(finalPolicy)
                                .locale(trDto.getLocale().toLowerCase())
                                .name(trDto.getName())
                                .description(trDto.getDescription())
                                .build()
                );
            }

            SelfDrivePolicy saved = policyRepository.save(policy);
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "selfDrivePolicies", allEntries = true)
    public void delete(Long id) {
        translationRepository.findByPolicyId(id).forEach(translationRepository::delete);
        policyRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "selfDrivePolicies", allEntries = true)
    public SelfDrivePolicyDto toggleActive(Long id) {
        return policyRepository.findById(id).map(policy -> {
            policy.setActive(!policy.isActive());
            SelfDrivePolicy saved = policyRepository.save(policy);
            return toDto(saved);
        }).orElse(null);
    }

    private SelfDrivePolicyDto toDto(SelfDrivePolicy policy) {
        List<SelfDrivePolicyTranslationDto> translations = translationRepository.findByPolicyId(policy.getId()).stream()
                .map(tr -> SelfDrivePolicyTranslationDto.builder()
                        .id(tr.getId())
                        .policyId(tr.getPolicy().getId())
                        .locale(tr.getLocale())
                        .name(tr.getName())
                        .description(tr.getDescription())
                        .build())
                .collect(Collectors.toList());

        return SelfDrivePolicyDto.builder()
                .id(policy.getId())
                .code(policy.getCode())
                .icon(policy.getIcon())
                .sortOrder(policy.getSortOrder())
                .active(policy.isActive())
                .translations(translations)
                .build();
    }
}
