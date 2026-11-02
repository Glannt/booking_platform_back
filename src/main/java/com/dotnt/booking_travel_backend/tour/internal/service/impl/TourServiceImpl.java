package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.*;
import com.dotnt.booking_travel_backend.tour.internal.entity.*;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import com.dotnt.booking_travel_backend.tour.internal.mapper.TourMapper;
import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import org.springframework.context.ApplicationEventPublisher;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import com.dotnt.booking_travel_backend.tour.internal.service.ITourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils.mergeCollection;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourServiceImpl implements ITourService {

    private final TourRepository tourRepository;
    private final TourCategoryRepository tourCategoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TourDayRepository tourDayRepository;
    private final TourDayActivityRepository tourDayActivityRepository;
    private final TourDayTranslationRepository tourDayTranslationRepository;
    private final TourDayActivityTranslationRepository tourDayActivityTranslationRepository;
    private final TourOptionRepository tourOptionRepository;
    private final TourOptionTranslationRepository tourOptionTranslationRepository;
    private final TourTranslationRepository tourTranslationRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourPackageTranslationRepository tourPackageTranslationRepository;
    private final TourMapper tourMapper;
    private final ModuleEventPublisher moduleEventPublisher;
    private final com.dotnt.booking_travel_backend.tour.internal.service.TourConfigService tourConfigService;

    @Override
    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public TourDto createTour(CreateTourRequest request) {
        TourCategory category = null;
        if (request.getTourCategoryId() != null) {
            category = tourCategoryRepository.findById(request.getTourCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Tour category not found"));
        }
        String tourStatus = request.getStatus() != null && !request.getStatus().isBlank()
                ? request.getStatus().toUpperCase()
                : (request.isActive() ? "ACTIVE" : "INACTIVE");
        boolean isTourActive = "ACTIVE".equalsIgnoreCase(tourStatus);

        String rawSlug = request.getSlug();
        if (rawSlug == null || rawSlug.isBlank()) {
            if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
                String candidateName = request.getTranslations().stream()
                        .filter(tr -> "vi".equalsIgnoreCase(tr.getLocale()) && tr.getName() != null && !tr.getName().isBlank())
                        .map(TourTranslationDto::getName)
                        .findFirst()
                        .orElse(request.getTranslations().stream()
                                .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                                .map(TourTranslationDto::getName)
                                .findFirst()
                                .orElse(null));
                rawSlug = slugify(candidateName);
            }
        } else {
            rawSlug = slugify(rawSlug);
        }
        if (rawSlug == null || rawSlug.isBlank()) {
            rawSlug = "tour-" + System.currentTimeMillis();
        }

        String finalSlug = rawSlug;
        int counter = 1;
        while (tourRepository.existsBySlug(finalSlug)) {
            finalSlug = rawSlug + "-" + counter++;
        }

        Tour t = Tour.builder()
                .category(category)
                .durationDays(request.getDurationDays() != null ? request.getDurationDays() : 1)
                .durationNights(request.getDurationNights() != null ? request.getDurationNights() : 0)
                .minGuest(request.getMinGuest() != null ? request.getMinGuest() : 1)
                .maxGuest(request.getMaxGuest() != null ? request.getMaxGuest() : 20)
                .thumbnailId(request.getThumbnailId())
                .status(tourStatus)
                .active(isTourActive)
                .isRecommended(request.getIsRecommended() != null ? request.getIsRecommended() : false)
                .slug(finalSlug)
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .components(request.getComponents())
                .build();

        // Set images from IDs
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            t.setImageIds(new ArrayList<>(request.getImageIds()));
        }

        Tour saved = tourRepository.save(t);

        // Save options
        if (request.getOptions() != null) {
            for (TourOptionDto optDto : request.getOptions()) {
                TourOption opt = TourOption.builder()
                        .tour(saved)
                        .iconUrl(optDto.getIconUrl())
                        .colorText(optDto.getColorText())
                        .build();
                TourOption savedOpt = tourOptionRepository.save(opt);
                if (optDto.getTranslations() != null) {
                    for (TourOptionTranslationDto transDto : optDto.getTranslations()) {
                        TourOptionTranslation trans = TourOptionTranslation.builder()
                                .tourOption(savedOpt)
                                .locale(transDto.getLocale())
                                .content(transDto.getContent())
                                .build();
                        tourOptionTranslationRepository.save(trans);
                    }
                }
            }
        }

        // Save translations
        if (request.getTranslations() != null) {
            for (TourTranslationDto transDto : request.getTranslations()) {
                TourTranslation trans = TourTranslation.builder()
                        .tour(saved)
                        .locale(transDto.getLocale())
                        .name(transDto.getName())
                        .slug(finalSlug)
                        .description(transDto.getDescription())
                        .content(transDto.getContent())
                        .build();
                tourTranslationRepository.save(trans);
            }
        }

        // Save packages
        if (request.getPackages() != null) {
            for (TourPackageDto pkgDto : request.getPackages()) {
                List<PriceListPerPerson> prices = new ArrayList<>();
                if (pkgDto.getPrices() != null) {
                    for (PriceListPerPersonDto prDto : pkgDto.getPrices()) {
                        // A price the admin never entered does not become a row. Writing 0 here
                        // created an active 0đ entry out of an empty box, which is a price nobody
                        // set. Free-by-policy infant and child stay expressible — they are entered
                        // as 0, which is a figure, not a blank.
                        if (prDto.getPricePerPerson() == null) continue;
                        prices.add(PriceListPerPerson.builder()
                                .passengerType(prDto.getPassengerType() != null ? prDto.getPassengerType() : "ADULT")
                                .pricePerPerson(prDto.getPricePerPerson())
                                .minPax(prDto.getMinPax() != null ? prDto.getMinPax() : 1)
                                .maxPax(prDto.getMaxPax())
                                .active(prDto.getActive() != null ? prDto.getActive() : true)
                                .applyAgeCondition(Boolean.TRUE.equals(prDto.getApplyAgeCondition()))
                                .minAge(prDto.getMinAge())
                                .maxAge(prDto.getMaxAge())
                                .applyHeightCondition(Boolean.TRUE.equals(prDto.getApplyHeightCondition()))
                                .minHeight(prDto.getMinHeight())
                                .maxHeight(prDto.getMaxHeight())
                                .build());
                    }
                }
                TourPackage pkg = TourPackage.builder()
                        .tour(saved)
                        .name(pkgDto.getName() != null ? pkgDto.getName() : "Gói Tiêu Chuẩn")
                        .prices(prices)
                        .depositPolicies(DepositPolicies.from(pkgDto.getDepositType(), pkgDto.getDepositRate(), pkgDto.getDepositAmount()))
                        .childPolicyNote(pkgDto.getChildPolicyNote())
                        .infantPolicyNote(pkgDto.getInfantPolicyNote())
                        .isContactOnly(pkgDto.isContactOnly())
                        .minGuests(pkgDto.getMinGuests() != null && pkgDto.getMinGuests() > 0 ? pkgDto.getMinGuests() : 1)
                        .maxGuests(pkgDto.getMaxGuests() != null && pkgDto.getMaxGuests() > 0 ? pkgDto.getMaxGuests() : 30)
                        .highlights(pkgDto.getHighlights())
                        .inclusions(pkgDto.getInclusions())
                        .exclusions(pkgDto.getExclusions())
                        .active(pkgDto.isActive())
                        .sortOrder(pkgDto.getSortOrder() != null ? pkgDto.getSortOrder() : 0)
                        .build();
                assertPackagePublishable(pkg);
                TourPackage savedPkg = tourPackageRepository.save(pkg);
                if (pkgDto.getTranslations() != null) {
                    for (TourPackageTranslationDto ptrDto : pkgDto.getTranslations()) {
                        TourPackageTranslation ptr = TourPackageTranslation.builder()
                                .tourPackage(savedPkg)
                                .locale(ptrDto.getLocale())
                                .name(ptrDto.getName())
                                .highlights(ptrDto.getHighlights())
                                .inclusions(ptrDto.getInclusions())
                                .exclusions(ptrDto.getExclusions())
                                .childPolicyNote(ptrDto.getChildPolicyNote())
                                .infantPolicyNote(ptrDto.getInfantPolicyNote())
                                .build();
                        tourPackageTranslationRepository.save(ptr);
                    }
                }
            }
        }

        // Save days
        if (request.getTourDays() != null && !request.getTourDays().isEmpty()) {
            List<TourDay> tourDays = request.getTourDays().stream().map(td -> TourDay.builder()
                    .tour(saved)
                    .dayNumber(td.getDayNumber())
                    .build())
                    .toList();
            tourDayRepository.saveAllAndFlush(tourDays);

            // For simplicity and to avoid indexing problems:
            for (int i = 0; i < request.getTourDays().size(); i++) {
                TourDayDto tdDto = request.getTourDays().get(i);
                TourDay dayEntity = tourDays.get(i);

                if (tdDto.getTranslations() != null) {
                    List<TourDayTranslation> dayTrans = tdDto.getTranslations().stream()
                            .map(tr -> TourDayTranslation.builder()
                                    .tourDay(dayEntity)
                                    .locale(tr.getLocale())
                                    .name(tr.getName())
                                    .description(tr.getDescription())
                                    .build())
                            .toList();
                    tourDayTranslationRepository.saveAllAndFlush(dayTrans);
                }

                if (tdDto.getActivities() != null) {
                    List<TourDayActivity> activities = tdDto.getActivities().stream()
                            .map(act -> TourDayActivity.builder()
                                    .tourDay(dayEntity)
                                    .startTime(act.getStartTime())
                                    .endTime(act.getEndTime())
                                    .location(act.getLocation())
                                    .build())
                            .toList();
                    tourDayActivityRepository.saveAllAndFlush(activities);

                    for (int j = 0; j < tdDto.getActivities().size(); j++) {
                        TourActivityDto actDto = tdDto.getActivities().get(j);
                        TourDayActivity actEntity = activities.get(j);
                        if (actDto.getTranslations() != null) {
                            List<TourDayActivityTranslation> actTrans = actDto.getTranslations().stream()
                                    .map(tr -> TourDayActivityTranslation.builder()
                                            .tourDayActivity(actEntity)
                                            .locale(tr.getLocale())
                                            .name(tr.getName())
                                            .description(tr.getDescription())
                                            .build())
                                    .toList();
                            tourDayActivityTranslationRepository.saveAllAndFlush(actTrans);
                        }
                    }
                }
            }
        }

        return getTour(saved.getId());
    }

    private java.util.Map<Long, TourBookingStat> getTopRecommendedTourStats() {
        try {
            com.dotnt.booking_travel_backend.tour.api.dtos.TourConfigDto config = tourConfigService.getConfig();
            if (config == null || !config.isAutoRecommendEnabled() || config.getRecommendedLimit() <= 0) {
                return java.util.Collections.emptyMap();
            }

            java.util.List<Object[]> rows = tourRepository.findTourBookingStats();
            java.util.Map<Long, TourBookingStat> statsMap = new java.util.LinkedHashMap<>();
            int count = 0;
            for (Object[] row : rows) {
                if (row == null || row.length < 3 || row[0] == null) continue;
                Long tourId = ((Number) row[0]).longValue();
                Long bookingsCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                java.math.BigDecimal revenue = row[2] instanceof java.math.BigDecimal
                        ? (java.math.BigDecimal) row[2]
                        : (row[2] != null ? java.math.BigDecimal.valueOf(((Number) row[2]).doubleValue()) : java.math.BigDecimal.ZERO);
                statsMap.put(tourId, new TourBookingStat(bookingsCount, revenue));
                count++;
                if (count >= config.getRecommendedLimit()) {
                    break;
                }
            }
            return statsMap;
        } catch (Exception e) {
            log.warn("Failed to compute tour recommendation stats: {}", e.getMessage());
            return java.util.Collections.emptyMap();
        }
    }

    private static class TourBookingStat {
        final Long count;
        final java.math.BigDecimal revenue;
        TourBookingStat(Long count, java.math.BigDecimal revenue) {
            this.count = count;
            this.revenue = revenue;
        }
    }

    @Override
    @Cacheable(value = "tours", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #search + '_' + #tourCategoryId")
    public Page<TourDto> getTours(Pageable pageable, String search, Long tourCategoryId) {
        Page<Tour> page;
        if (tourCategoryId != null) {
            page = tourRepository.findByCategoryId(tourCategoryId, pageable);
        } else {
            page = tourRepository.findAll(pageable);
        }

        java.util.Map<Long, TourBookingStat> topStats = getTopRecommendedTourStats();
        List<TourDto> list = page.getContent().stream().map(t -> {
            TourDto dto = getTour(t.getId());
            if (dto != null && topStats.containsKey(t.getId())) {
                dto.setIsRecommended(true);
                dto.setSuccessfulBookingsCount(topStats.get(t.getId()).count);
                dto.setTotalRevenue(topStats.get(t.getId()).revenue);
            }
            return dto;
        }).sorted((a, b) -> {
            boolean recA = a != null && Boolean.TRUE.equals(a.getIsRecommended());
            boolean recB = b != null && Boolean.TRUE.equals(b.getIsRecommended());
            if (recA && !recB) return -1;
            if (!recA && recB) return 1;
            return 0;
        }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    @Cacheable(value = "tours", key = "#id")
    public TourDto getTour(Long id) {
        return tourRepository.findById(id).map(t -> {
            TourDto dto = toDto(t);
            // load days
            java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDay> days = (t.getDays() != null && !t.getDays().isEmpty())
                    ? t.getDays()
                    : tourDayRepository.findByTourId(t.getId());
            dto.setDays(days.stream().map(d -> {
                com.dotnt.booking_travel_backend.tour.api.dtos.TourDayDto dayDto = tourMapper.toDto(d);
                // translations
                java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayTranslation> trans = (d.getTranslations() != null && !d.getTranslations().isEmpty())
                        ? d.getTranslations()
                        : (d.getId() != null ? tourDayTranslationRepository.findByTourDayId(d.getId()) : java.util.Collections.emptyList());
                dayDto.setTranslations(
                        trans.stream().map(tourMapper::toDto).collect(java.util.stream.Collectors.toList()));
                // activities
                java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivity> acts = (d.getActivities() != null && !d.getActivities().isEmpty())
                        ? d.getActivities()
                        : (d.getId() != null ? tourDayActivityRepository.findByTourDayId(d.getId()) : java.util.Collections.emptyList());
                dayDto.setActivities(acts.stream().map(a -> {
                    com.dotnt.booking_travel_backend.tour.api.dtos.TourActivityDto actDto = tourMapper.toDto(a);
                    java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivityTranslation> atrans = (a.getTranslations() != null && !a.getTranslations().isEmpty())
                            ? a.getTranslations()
                            : (a.getId() != null ? tourDayActivityTranslationRepository.findByTourDayActivityId(a.getId()) : java.util.Collections.emptyList());
                    actDto.setTranslations(
                            atrans.stream().map(tourMapper::toDto).collect(java.util.stream.Collectors.toList()));
                    return actDto;
                }).collect(java.util.stream.Collectors.toList()));
                return dayDto;
            }).collect(java.util.stream.Collectors.toList()));

            // load options
            List<TourOptionDto> optionDtos = t.getOptions().stream().map(o -> TourOptionDto.builder()
                    .id(o.getId())
                    .iconUrl(o.getIconUrl())
                    .colorText(o.getColorText())
                    .translations(o.getTranslations().stream().map(tr -> TourOptionTranslationDto.builder()
                            .id(tr.getId())
                            .locale(tr.getLocale())
                            .content(tr.getContent())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
            dto.setOptions(optionDtos);

            // load translations
            List<TourTranslationDto> translationDtos = t.getTranslations().stream()
                    .map(tr -> TourTranslationDto.builder()
                            .id(tr.getId())
                            .locale(tr.getLocale())
                            .name(tr.getName())
                            .slug(tr.getSlug())
                            .description(tr.getDescription())
                            .content(tr.getContent())
                            .build())
                    .collect(Collectors.toList());
            dto.setTranslations(translationDtos);
            dto.setPrices(new ArrayList<>());

            // load packages
            List<TourPackage> pkgs = (t.getPackages() != null && !t.getPackages().isEmpty())
                    ? t.getPackages()
                    : tourPackageRepository.findByTourId(t.getId());
            List<TourPackageDto> packageDtos = pkgs.stream()
                    .map(pkg -> {
                        BigDecimal adultPrice = null;
                        BigDecimal childPrice = null;
                        BigDecimal infantPrice = null;
                        if (pkg.getPrices() != null) {
                            for (PriceListPerPerson pr : pkg.getPrices()) {
                                if ("ADULT".equalsIgnoreCase(pr.getPassengerType())) {
                                    adultPrice = pr.getPricePerPerson();
                                } else if ("CHILD".equalsIgnoreCase(pr.getPassengerType())) {
                                    childPrice = pr.getPricePerPerson();
                                } else if ("INFANT".equalsIgnoreCase(pr.getPassengerType())) {
                                    infantPrice = pr.getPricePerPerson();
                                }
                            }
                        }
                        String pricingType = pkg.isContactOnly() ? "PRIVATE_CHARTER" : "PER_PERSON";

                        return TourPackageDto.builder()
                                .id(pkg.getId())
                                .tourId(t.getId())
                                .name(pkg.getName())
                                .depositType(pkg.getDepositPolicies() != null ? pkg.getDepositPolicies().getType() : null)
                                .depositRate(pkg.getDepositPolicies() != null ? pkg.getDepositPolicies().getRate() : null)
                                .depositAmount(pkg.getDepositPolicies() != null ? pkg.getDepositPolicies().getAmount() : null)
                                .pricingType(pricingType)
                                .adultPrice(adultPrice)
                                .childPrice(childPrice)
                                .infantPrice(infantPrice)
                                .prices(pkg.getPrices() != null ? pkg.getPrices().stream().map(pr -> PriceListPerPersonDto.builder()
                                        .id(pr.getId())
                                        .passengerType(pr.getPassengerType())
                                        .pricePerPerson(pr.getPricePerPerson())
                                        .minPax(pr.getMinPax())
                                        .maxPax(pr.getMaxPax())
                                        .active(pr.isActive())
                                        .applyAgeCondition(pr.isApplyAgeCondition())
                                        .minAge(pr.getMinAge())
                                        .maxAge(pr.getMaxAge())
                                        .applyHeightCondition(pr.isApplyHeightCondition())
                                        .minHeight(pr.getMinHeight())
                                        .maxHeight(pr.getMaxHeight())
                                        .build()).collect(Collectors.toList()) : new ArrayList<>())
                                .childPolicyNote(pkg.getChildPolicyNote())
                                .infantPolicyNote(pkg.getInfantPolicyNote())
                                .isContactOnly(pkg.isContactOnly())
                                .minGuests(pkg.getMinGuests())
                                .maxGuests(pkg.getMaxGuests())
                                .highlights(pkg.getHighlights())
                                .inclusions(pkg.getInclusions())
                                .exclusions(pkg.getExclusions())
                                .active(pkg.isActive())
                                .sortOrder(pkg.getSortOrder())
                                .translations(pkg.getTranslations().stream().map(ptr -> TourPackageTranslationDto.builder()
                                        .id(ptr.getId())
                                        .locale(ptr.getLocale())
                                        .name(ptr.getName())
                                        .highlights(ptr.getHighlights())
                                        .inclusions(ptr.getInclusions())
                                        .exclusions(ptr.getExclusions())
                                        .childPolicyNote(ptr.getChildPolicyNote())
                                        .infantPolicyNote(ptr.getInfantPolicyNote())
                                        .build()).collect(Collectors.toList()))
                                .build();
                    })
                    .collect(Collectors.toList());
            dto.setPackages(packageDtos);

            java.util.Map<Long, TourBookingStat> topStats = getTopRecommendedTourStats();
            if (topStats.containsKey(t.getId())) {
                dto.setIsRecommended(true);
                dto.setSuccessfulBookingsCount(topStats.get(t.getId()).count);
                dto.setTotalRevenue(topStats.get(t.getId()).revenue);
            }

            return dto;
        }).orElse(null);
    }

    /**
     * A package that is switched on has to be buyable. Without a price the customer would be
     * quoted 0đ, which is the exact hole this closes — an empty price box plus an un-ticked
     * contact-only flag reads as an accident, so it is rejected rather than published.
     */
    private void assertPackagePublishable(TourPackage pkg) {
        if (pkg.isActive() && !pkg.isPublishable()) {
            throw new IllegalArgumentException(
                    "Gói \"" + pkg.getName() + "\" đang bật nhưng chưa có giá người lớn. "
                            + "Hãy nhập giá người lớn, hoặc bật \"Chỉ liên hệ\" nếu muốn khách báo giá.");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public TourDto updateTour(Long id, TourDto tourDto) {
        return tourRepository.findById(id).map(t -> {
            if (tourDto.getTourCategoryId() != null) {
                TourCategory category = tourCategoryRepository.findById(tourDto.getTourCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Tour category not found"));
                t.setCategory(category);
            } else {
                t.setCategory(null);
            }
            if (tourDto.getDurationDays() != null) t.setDurationDays(tourDto.getDurationDays());
            if (tourDto.getDurationNights() != null) t.setDurationNights(tourDto.getDurationNights());
            if (tourDto.getMinGuest() != null) t.setMinGuest(tourDto.getMinGuest());
            if (tourDto.getMaxGuest() != null) t.setMaxGuest(tourDto.getMaxGuest());
            t.setThumbnailId(tourDto.getThumbnailId());
            if (tourDto.getStatus() != null && !tourDto.getStatus().isBlank()) {
                t.setStatus(tourDto.getStatus().toUpperCase());
                t.setActive("ACTIVE".equalsIgnoreCase(t.getStatus()));
            } else {
                t.setActive(tourDto.isActive());
                t.setStatus(tourDto.isActive() ? "ACTIVE" : "INACTIVE");
            }
            if (tourDto.getInclusions() != null) {
                t.setInclusions(tourDto.getInclusions());
            }
            if (tourDto.getExclusions() != null) {
                t.setExclusions(tourDto.getExclusions());
            }
            if (tourDto.getComponents() != null) {
                t.setComponents(tourDto.getComponents());
            }
            if(tourDto.getIsRecommended() != null){
                t.setIsRecommended(tourDto.getIsRecommended());
            }
            if (tourDto.getSlug() != null && !tourDto.getSlug().isBlank()) {
                String rawSlug = slugify(tourDto.getSlug());
                String finalSlug = rawSlug;
                int counter = 1;
                while (tourRepository.existsBySlugAndIdNot(finalSlug, id)) {
                    finalSlug = rawSlug + "-" + counter++;
                }
                t.setSlug(finalSlug);
            } else if (t.getSlug() == null || t.getSlug().isBlank()) {
                String candidateName = null;
                if (tourDto.getTranslations() != null && !tourDto.getTranslations().isEmpty()) {
                    candidateName = tourDto.getTranslations().stream()
                            .filter(tr -> "vi".equalsIgnoreCase(tr.getLocale()) && tr.getName() != null && !tr.getName().isBlank())
                            .map(TourTranslationDto::getName)
                            .findFirst()
                            .orElse(tourDto.getTranslations().stream()
                                    .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                                    .map(TourTranslationDto::getName)
                                    .findFirst()
                                    .orElse(null));
                }
                String rawSlug = slugify(candidateName);
                String finalSlug = rawSlug;
                int counter = 1;
                while (tourRepository.existsBySlugAndIdNot(finalSlug, id)) {
                    finalSlug = rawSlug + "-" + counter++;
                }
                t.setSlug(finalSlug);
            }

            // Update images
            if (tourDto.getImageIds() != null) {
                t.setImageIds(new ArrayList<>(tourDto.getImageIds()));
            }

            // 1. Merge translations (key = locale)
            mergeCollection(
                    t.getTranslations(),
                    tourDto.getTranslations() != null ? tourDto.getTranslations() : List.of(),
                    TourTranslation::getLocale,
                    TourTranslationDto::getLocale,
                    (entity, dto) -> {
                        entity.setName(dto.getName());
                        entity.setSlug(t.getSlug());
                        entity.setDescription(dto.getDescription());
                        entity.setContent(dto.getContent());
                    },
                    dto -> TourTranslation.builder()
                            .tour(t)
                            .locale(dto.getLocale())
                            .name(dto.getName())
                            .slug(t.getSlug())
                            .description(dto.getDescription())
                            .content(dto.getContent())
                            .build()
            );

            // 2. Merge options (key = id != null ? id : (long) -System.identityHashCode(dto))
            mergeCollection(
                    t.getOptions(),
                    tourDto.getOptions() != null ? tourDto.getOptions() : List.of(),
                    TourOption::getId,
                    optDto -> optDto.getId() != null ? optDto.getId() : (long) -System.identityHashCode(optDto),
                    (opt, optDto) -> {
                        opt.setIconUrl(optDto.getIconUrl());
                        opt.setColorText(optDto.getColorText());

                        mergeCollection(
                                opt.getTranslations(),
                                optDto.getTranslations() != null ? optDto.getTranslations() : List.of(),
                                TourOptionTranslation::getLocale,
                                TourOptionTranslationDto::getLocale,
                                (tr, trDto) -> tr.setContent(trDto.getContent()),
                                trDto -> TourOptionTranslation.builder()
                                        .tourOption(opt)
                                        .locale(trDto.getLocale())
                                        .content(trDto.getContent())
                                        .build()
                        );
                    },
                    optDto -> {
                        TourOption opt = TourOption.builder()
                                .tour(t)
                                .iconUrl(optDto.getIconUrl())
                                .colorText(optDto.getColorText())
                                .build();
                        if (optDto.getTranslations() != null) {
                            for (TourOptionTranslationDto trDto : optDto.getTranslations()) {
                                opt.getTranslations().add(TourOptionTranslation.builder()
                                        .tourOption(opt)
                                        .locale(trDto.getLocale())
                                        .content(trDto.getContent())
                                        .build());
                            }
                        }
                        return opt;
                    }
            );

            // 3. Merge packages (key = id != null ? id : (long) -System.identityHashCode(dto))
            mergeCollection(
                    t.getPackages(),
                    tourDto.getPackages() != null ? tourDto.getPackages() : List.of(),
                    TourPackage::getId,
                    pkgDto -> pkgDto.getId() != null ? pkgDto.getId() : (long) -System.identityHashCode(pkgDto),
                    (pkg, pkgDto) -> {
                        String pkgName = pkgDto.getName();
                        if ((pkgName == null || pkgName.isBlank()) && pkgDto.getTranslations() != null) {
                            pkgName = pkgDto.getTranslations().stream()
                                    .filter(tr -> "vi".equalsIgnoreCase(tr.getLocale()) && tr.getName() != null && !tr.getName().isBlank())
                                    .map(TourPackageTranslationDto::getName)
                                    .findFirst()
                                    .orElse(pkgDto.getTranslations().stream()
                                            .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                                            .map(TourPackageTranslationDto::getName)
                                            .findFirst()
                                            .orElse(pkg.getName()));
                        }
                        if (pkgName != null && !pkgName.isBlank()) {
                            pkg.setName(pkgName);
                        }
                        pkg.setChildPolicyNote(pkgDto.getChildPolicyNote());
                        pkg.setInfantPolicyNote(pkgDto.getInfantPolicyNote());
                        pkg.setDepositPolicies(DepositPolicies.merge(pkg.getDepositPolicies(),
                                pkgDto.getDepositType(), pkgDto.getDepositRate(), pkgDto.getDepositAmount()));
                        pkg.setContactOnly(pkgDto.isContactOnly());
                        pkg.setMinGuests(pkgDto.getMinGuests() != null && pkgDto.getMinGuests() > 0 ? pkgDto.getMinGuests() : 1);
                        pkg.setMaxGuests(pkgDto.getMaxGuests() != null && pkgDto.getMaxGuests() > 0 ? pkgDto.getMaxGuests() : 30);
                        pkg.setHighlights(pkgDto.getHighlights());
                        pkg.setInclusions(pkgDto.getInclusions());
                        pkg.setExclusions(pkgDto.getExclusions());
                        pkg.setActive(pkgDto.isActive());
                        pkg.setSortOrder(pkgDto.getSortOrder() != null ? pkgDto.getSortOrder() : 0);

                        // Merge prices (key: passengerType)
                        if (pkgDto.getPrices() != null) {
                            mergeCollection(
                                    pkg.getPrices(),
                                    pkgDto.getPrices(),
                                    pr -> pr.getPassengerType() != null ? pr.getPassengerType().toUpperCase() : "",
                                    prDto -> prDto.getPassengerType() != null ? prDto.getPassengerType().toUpperCase() : "",
                                    (pr, prDto) -> {
                                        pr.setPricePerPerson(prDto.getPricePerPerson() != null ? prDto.getPricePerPerson() : BigDecimal.ZERO);
                                        pr.setMinPax(prDto.getMinPax() != null ? prDto.getMinPax() : 1);
                                        pr.setMaxPax(prDto.getMaxPax());
                                        pr.setActive(prDto.getActive() != null ? prDto.getActive() : true);
                                        pr.setApplyAgeCondition(Boolean.TRUE.equals(prDto.getApplyAgeCondition()));
                                        pr.setMinAge(prDto.getMinAge());
                                        pr.setMaxAge(prDto.getMaxAge());
                                        pr.setApplyHeightCondition(Boolean.TRUE.equals(prDto.getApplyHeightCondition()));
                                        pr.setMinHeight(prDto.getMinHeight());
                                        pr.setMaxHeight(prDto.getMaxHeight());
                                    },
                                    prDto -> PriceListPerPerson.builder()
                                            .passengerType(prDto.getPassengerType() != null ? prDto.getPassengerType().toUpperCase() : "ADULT")
                                            .pricePerPerson(prDto.getPricePerPerson() != null ? prDto.getPricePerPerson() : BigDecimal.ZERO)
                                            .minPax(prDto.getMinPax() != null ? prDto.getMinPax() : 1)
                                            .maxPax(prDto.getMaxPax())
                                            .active(prDto.getActive() != null ? prDto.getActive() : true)
                                            .applyAgeCondition(Boolean.TRUE.equals(prDto.getApplyAgeCondition()))
                                            .minAge(prDto.getMinAge())
                                            .maxAge(prDto.getMaxAge())
                                            .applyHeightCondition(Boolean.TRUE.equals(prDto.getApplyHeightCondition()))
                                            .minHeight(prDto.getMinHeight())
                                            .maxHeight(prDto.getMaxHeight())
                                            .build()
                            );
                        }

                        // Merge package translations (key: locale)
                        if (pkgDto.getTranslations() != null) {
                            mergeCollection(
                                    pkg.getTranslations(),
                                    pkgDto.getTranslations(),
                                    TourPackageTranslation::getLocale,
                                    TourPackageTranslationDto::getLocale,
                                    (ptr, ptrDto) -> {
                                        ptr.setName(ptrDto.getName());
                                        ptr.setHighlights(ptrDto.getHighlights());
                                        ptr.setInclusions(ptrDto.getInclusions());
                                        ptr.setExclusions(ptrDto.getExclusions());
                                        ptr.setChildPolicyNote(ptrDto.getChildPolicyNote());
                                        ptr.setInfantPolicyNote(ptrDto.getInfantPolicyNote());
                                    },
                                    ptrDto -> TourPackageTranslation.builder()
                                            .tourPackage(pkg)
                                            .locale(ptrDto.getLocale())
                                            .name(ptrDto.getName())
                                            .highlights(ptrDto.getHighlights())
                                            .inclusions(ptrDto.getInclusions())
                                            .exclusions(ptrDto.getExclusions())
                                            .childPolicyNote(ptrDto.getChildPolicyNote())
                                            .infantPolicyNote(ptrDto.getInfantPolicyNote())
                                            .build()
                            );
                        }
                    },
                    pkgDto -> {
                        String pkgName = pkgDto.getName();
                        if ((pkgName == null || pkgName.isBlank()) && pkgDto.getTranslations() != null) {
                            pkgName = pkgDto.getTranslations().stream()
                                    .filter(tr -> "vi".equalsIgnoreCase(tr.getLocale()) && tr.getName() != null && !tr.getName().isBlank())
                                    .map(TourPackageTranslationDto::getName)
                                    .findFirst()
                                    .orElse(pkgDto.getTranslations().stream()
                                            .filter(tr -> tr.getName() != null && !tr.getName().isBlank())
                                            .map(TourPackageTranslationDto::getName)
                                            .findFirst()
                                            .orElse("Gói Tiêu Chuẩn"));
                        }
                        List<PriceListPerPerson> prices = new ArrayList<>();
                        if (pkgDto.getPrices() != null) {
                            for (PriceListPerPersonDto prDto : pkgDto.getPrices()) {
                                prices.add(PriceListPerPerson.builder()
                                        .passengerType(prDto.getPassengerType() != null ? prDto.getPassengerType() : "ADULT")
                                        .pricePerPerson(prDto.getPricePerPerson() != null ? prDto.getPricePerPerson() : BigDecimal.ZERO)
                                        .minPax(prDto.getMinPax() != null ? prDto.getMinPax() : 1)
                                        .maxPax(prDto.getMaxPax())
                                        .active(prDto.getActive() != null ? prDto.getActive() : true)
                                        .applyAgeCondition(Boolean.TRUE.equals(prDto.getApplyAgeCondition()))
                                        .minAge(prDto.getMinAge())
                                        .maxAge(prDto.getMaxAge())
                                        .applyHeightCondition(Boolean.TRUE.equals(prDto.getApplyHeightCondition()))
                                        .minHeight(prDto.getMinHeight())
                                        .maxHeight(prDto.getMaxHeight())
                                        .build());
                            }
                        }
                        TourPackage newPkg = TourPackage.builder()
                                .tour(t)
                                .name(pkgName != null ? pkgName : "Gói Tiêu Chuẩn")
                                .prices(prices)
                                .childPolicyNote(pkgDto.getChildPolicyNote())
                                .infantPolicyNote(pkgDto.getInfantPolicyNote())
                                .isContactOnly(pkgDto.isContactOnly())
                                .minGuests(pkgDto.getMinGuests() != null && pkgDto.getMinGuests() > 0 ? pkgDto.getMinGuests() : 1)
                                .maxGuests(pkgDto.getMaxGuests() != null && pkgDto.getMaxGuests() > 0 ? pkgDto.getMaxGuests() : 30)
                                .highlights(pkgDto.getHighlights())
                                .inclusions(pkgDto.getInclusions())
                                .exclusions(pkgDto.getExclusions())
                                .active(pkgDto.isActive())
                                .sortOrder(pkgDto.getSortOrder() != null ? pkgDto.getSortOrder() : 0)
                                .build();
                        if (pkgDto.getTranslations() != null) {
                            for (TourPackageTranslationDto ptrDto : pkgDto.getTranslations()) {
                                newPkg.getTranslations().add(TourPackageTranslation.builder()
                                        .tourPackage(newPkg)
                                        .locale(ptrDto.getLocale())
                                        .name(ptrDto.getName())
                                        .highlights(ptrDto.getHighlights())
                                        .inclusions(ptrDto.getInclusions())
                                        .exclusions(ptrDto.getExclusions())
                                        .childPolicyNote(ptrDto.getChildPolicyNote())
                                        .infantPolicyNote(ptrDto.getInfantPolicyNote())
                                        .build());
                            }
                        }
                        return newPkg;
                    }
            );

            // Checked after the merge so it sees both the packages that were updated in place and
            // the ones just created, and reads the prices as they now stand rather than as sent.
            t.getPackages().forEach(this::assertPackagePublishable);

            // 4. Merge days & activities (key = dayNumber cho day, key = id cho activity)
            mergeCollection(
                    t.getDays(),
                    tourDto.getDays() != null ? tourDto.getDays() : List.of(),
                    TourDay::getDayNumber,
                    TourDayDto::getDayNumber,
                    (day, dayDto) -> {
                        // Merge day translations (key: locale)
                        mergeCollection(
                                day.getTranslations(),
                                dayDto.getTranslations() != null ? dayDto.getTranslations() : List.of(),
                                TourDayTranslation::getLocale,
                                TourDayTranslationDto::getLocale,
                                (dtr, dtrDto) -> {
                                    dtr.setName(dtrDto.getName());
                                    dtr.setDescription(dtrDto.getDescription());
                                },
                                dtrDto -> TourDayTranslation.builder()
                                        .tourDay(day)
                                        .locale(dtrDto.getLocale())
                                        .name(dtrDto.getName())
                                        .description(dtrDto.getDescription())
                                        .build()
                        );

                        // Merge day activities (key: id != null ? id : -System.identityHashCode(dto))
                        mergeCollection(
                                day.getActivities(),
                                dayDto.getActivities() != null ? dayDto.getActivities() : List.of(),
                                TourDayActivity::getId,
                                actDto -> actDto.getId() != null ? actDto.getId() : (long) -System.identityHashCode(actDto),
                                (act, actDto) -> {
                                    act.setStartTime(actDto.getStartTime());
                                    act.setEndTime(actDto.getEndTime());
                                    act.setLocation(actDto.getLocation());

                                    // Merge activity translations (key: locale)
                                    mergeCollection(
                                            act.getTranslations(),
                                            actDto.getTranslations() != null ? actDto.getTranslations() : List.of(),
                                            TourDayActivityTranslation::getLocale,
                                            TourActivityTranslationDto::getLocale,
                                            (atr, atrDto) -> {
                                                atr.setName(atrDto.getName());
                                                atr.setDescription(atrDto.getDescription());
                                            },
                                            atrDto -> TourDayActivityTranslation.builder()
                                                    .tourDayActivity(act)
                                                    .locale(atrDto.getLocale())
                                                    .name(atrDto.getName())
                                                    .description(atrDto.getDescription())
                                                    .build()
                                    );
                                },
                                actDto -> {
                                    TourDayActivity newAct = TourDayActivity.builder()
                                            .tourDay(day)
                                            .startTime(actDto.getStartTime())
                                            .endTime(actDto.getEndTime())
                                            .location(actDto.getLocation())
                                            .build();
                                    if (actDto.getTranslations() != null) {
                                        for (TourActivityTranslationDto atrDto : actDto.getTranslations()) {
                                            newAct.getTranslations().add(TourDayActivityTranslation.builder()
                                                    .tourDayActivity(newAct)
                                                    .locale(atrDto.getLocale())
                                                    .name(atrDto.getName())
                                                    .description(atrDto.getDescription())
                                                    .build());
                                        }
                                    }
                                    return newAct;
                                }
                        );
                    },
                    dayDto -> {
                        TourDay newDay = TourDay.builder()
                                .tour(t)
                                .dayNumber(dayDto.getDayNumber())
                                .build();
                        if (dayDto.getTranslations() != null) {
                            for (TourDayTranslationDto dtrDto : dayDto.getTranslations()) {
                                newDay.getTranslations().add(TourDayTranslation.builder()
                                        .tourDay(newDay)
                                        .locale(dtrDto.getLocale())
                                        .name(dtrDto.getName())
                                        .description(dtrDto.getDescription())
                                        .build());
                            }
                        }
                        if (dayDto.getActivities() != null) {
                            for (TourActivityDto actDto : dayDto.getActivities()) {
                                TourDayActivity newAct = TourDayActivity.builder()
                                        .tourDay(newDay)
                                        .startTime(actDto.getStartTime())
                                        .endTime(actDto.getEndTime())
                                        .location(actDto.getLocation())
                                        .build();
                                if (actDto.getTranslations() != null) {
                                    for (TourActivityTranslationDto atrDto : actDto.getTranslations()) {
                                        newAct.getTranslations().add(TourDayActivityTranslation.builder()
                                                .tourDayActivity(newAct)
                                                .locale(atrDto.getLocale())
                                                .name(atrDto.getName())
                                                .description(atrDto.getDescription())
                                                .build());
                                    }
                                }
                                newDay.getActivities().add(newAct);
                            }
                        }
                        return newDay;
                    }
            );

            Tour saved = tourRepository.save(t);
            return getTour(saved.getId());
        }).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "tours", key = "'slug_' + #slug")
    public TourDto getTourBySlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return tourRepository.findBySlug(slug)
                .map(t -> getTour(t.getId()))
                .orElseGet(() -> tourTranslationRepository.findBySlug(slug)
                        .map(tr -> getTour(tr.getTour().getId()))
                        .orElseGet(() -> {
                            try {
                                if (slug.matches(".*-(\\d+)$")) {
                                    String idPart = slug.substring(slug.lastIndexOf('-') + 1);
                                    return getTour(Long.parseLong(idPart));
                                }
                                Long id = Long.parseLong(slug);
                                return getTour(id);
                            } catch (Exception e) {
                                return null;
                            }
                        }));
    }

    private String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "tour-" + System.currentTimeMillis();
        }
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        String noDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase()
                .replace("đ", "d")
                .replace("Đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @SuppressWarnings("unchecked")
    private TourDto toDto(Tour t) {
        List<Long> imageIds = t.getImageIds() != null
                ? t.getImageIds()
                : List.of();
        List<MediaDto> images = List.of();
        if (!imageIds.isEmpty()) {
            try {
                List<MediaDto> fetched = moduleEventPublisher.publishRequest(
                        "media.image.findAllByIds", imageIds, List.class);
                if (fetched != null) images = fetched;
            } catch (Exception e) {
                log.warn("Failed to fetch media for tour {}: {}", t.getId(), e.getMessage());
            }
        }
        TourCategoryDTO categoryDTO = null;
        if (t.getCategory() != null) {
            categoryDTO = TourCategoryDTO.builder()
                    .id(t.getCategory().getId())
                    .code(t.getCategory().getCode())
                    .icon(t.getCategory().getIcon())
                    .sortOrder(t.getCategory().getSortOrder())
                    .active(t.getCategory().isActive())
                    .isActive(t.getCategory().isActive())
                    .title(t.getCategory().getTitle())
                    .description(t.getCategory().getDescription())
                    .translations(t.getCategory().getTranslations() != null ? t.getCategory().getTranslations().stream()
                            .map(tr -> TourCategoryTranslationDTO.builder()
                                    .id(tr.getId())
                                    .locale(tr.getLocale())
                                    .name(tr.getName())
                                    .slug(tr.getSlug())
                                    .description(tr.getDescription())
                                    .build())
                            .toList() : List.of())
                    .build();
        }

        return TourDto.builder()
                .id(t.getId())
                .tourCategoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .tourCategory(categoryDTO)
                .durationDays(t.getDurationDays())
                .durationNights(t.getDurationNights())
                .minGuest(t.getMinGuest())
                .maxGuest(t.getMaxGuest())
                .thumbnailId(t.getThumbnailId())
                .imageIds(imageIds)
                .images(images)
                .active(t.isActive())
                .status(t.getStatus() != null ? t.getStatus() : (t.isActive() ? "ACTIVE" : "INACTIVE"))
                .slug(t.getSlug())
                .inclusions(t.getInclusions())
                .exclusions(t.getExclusions())
                .components(t.getComponents())
                .build();
    }
}
