package com.dotnt.booking_travel_backend.tour.internal.service.impl;

import com.dotnt.booking_travel_backend.tour.api.dtos.*;
import com.dotnt.booking_travel_backend.tour.internal.entity.*;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import com.dotnt.booking_travel_backend.tour.internal.mapper.TourMapper;
import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
import com.dotnt.booking_travel_backend.media.api.MediaDto;
import org.springframework.context.ApplicationEventPublisher;
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

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private final TourPriceRepository tourPriceRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourPackageTranslationRepository tourPackageTranslationRepository;
    private final TourMapper tourMapper;
    private final ImageRepository imageRepository;

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

        Tour t = Tour.builder()
                .category(category)
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights())
                .minGuest(request.getMinGuest())
                .maxGuest(request.getMaxGuest())
                .thumbnailId(request.getThumbnailId())
                .status(tourStatus)
                .active(isTourActive)
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .build();

        // Set images from IDs
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(request.getImageIds());
            t.setImages(images);
        }

        Tour saved = tourRepository.save(t);

        // Save options
        if (request.getOptions() != null) {
            for (TourOptionDto optDto : request.getOptions()) {
                TourOption opt = TourOption.builder()
                        .tour(saved)
                        .iconUrl(optDto.getIconUrl())
                        .colorText(optDto.getColorText())
                        .colorIcon(optDto.getColorIcon())
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
                String slug = transDto.getSlug() != null && !transDto.getSlug().isBlank()
                        ? transDto.getSlug()
                        : slugify(transDto.getName());
                TourTranslation trans = TourTranslation.builder()
                        .tour(saved)
                        .locale(transDto.getLocale())
                        .name(transDto.getName())
                        .slug(slug)
                        .description(transDto.getDescription())
                        .content(transDto.getContent())
                        .build();
                tourTranslationRepository.save(trans);
            }
        }

        // Save prices
        if (request.getPrices() != null) {
            for (TourPriceDto prDto : request.getPrices()) {
                TourPrice price = TourPrice.builder()
                        .tourId(saved.getId())
                        .vehicleCategoryId(prDto.getVehicleCategoryId())
                        .currency(prDto.getCurrency() != null ? prDto.getCurrency() : "VND")
                        .amount(prDto.getAmount())
                        .active(prDto.isActive())
                        .build();
                tourPriceRepository.save(price);
            }
        }

        // Save packages
        if (request.getPackages() != null) {
            for (TourPackageDto pkgDto : request.getPackages()) {
                TourPackage pkg = TourPackage.builder()
                        .tour(saved)
                        .name(pkgDto.getName() != null ? pkgDto.getName() : "Gói Tiêu Chuẩn")
                        .pricingType(pkgDto.getPricingType() != null ? pkgDto.getPricingType() : "PER_PERSON")
                        .adultPrice(pkgDto.getAdultPrice() != null ? pkgDto.getAdultPrice() : java.math.BigDecimal.ZERO)
                        .childPrice(pkgDto.getChildPrice() != null ? pkgDto.getChildPrice() : java.math.BigDecimal.ZERO)
                        .infantPrice(pkgDto.getInfantPrice() != null ? pkgDto.getInfantPrice() : java.math.BigDecimal.ZERO)
                        .childPolicyNote(pkgDto.getChildPolicyNote())
                        .infantPolicyNote(pkgDto.getInfantPolicyNote())
                        .fixedPrice(pkgDto.getFixedPrice() != null ? pkgDto.getFixedPrice() : java.math.BigDecimal.ZERO)
                        .isContactOnly(pkgDto.isContactOnly())
                        .minGuests(pkgDto.getMinGuests() > 0 ? pkgDto.getMinGuests() : 1)
                        .maxGuests(pkgDto.getMaxGuests() > 0 ? pkgDto.getMaxGuests() : 30)
                        .highlights(pkgDto.getHighlights())
                        .inclusions(pkgDto.getInclusions())
                        .exclusions(pkgDto.getExclusions())
                        .active(pkgDto.isActive())
                        .sortOrder(pkgDto.getSortOrder())
                        .build();
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

    @Override
    @Cacheable(value = "tours", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #search + '_' + #tourCategoryId")
    public Page<TourDto> getTours(Pageable pageable, String search, Long tourCategoryId) {
        Page<Tour> page;
        if (tourCategoryId != null) {
            page = tourRepository.findByCategoryId(tourCategoryId, pageable);
        } else {
            page = tourRepository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent().stream().map(t -> getTour(t.getId())).collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    @Override
    @Cacheable(value = "tours", key = "#id")
    public TourDto getTour(Long id) {
        return tourRepository.findById(id).map(t -> {
            TourDto dto = toDto(t);
            // load days
            java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDay> days = tourDayRepository
                    .findByTourId(t.getId());
            dto.setDays(days.stream().map(d -> {
                com.dotnt.booking_travel_backend.tour.api.dtos.TourDayDto dayDto = tourMapper.toDto(d);
                // translations
                java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayTranslation> trans = tourDayTranslationRepository
                        .findByTourDayId(d.getId());
                dayDto.setTranslations(
                        trans.stream().map(tourMapper::toDto).collect(java.util.stream.Collectors.toList()));
                // activities
                java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivity> acts = tourDayActivityRepository
                        .findByTourDayId(d.getId());
                dayDto.setActivities(acts.stream().map(a -> {
                    com.dotnt.booking_travel_backend.tour.api.dtos.TourActivityDto actDto = tourMapper.toDto(a);
                    java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivityTranslation> atrans = tourDayActivityTranslationRepository
                            .findByTourDayActivityId(a.getId());
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
                    .colorIcon(o.getColorIcon())
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

            // load prices
            List<TourPriceDto> priceDtos = tourPriceRepository.findByTourId(t.getId()).stream()
                    .map(p -> TourPriceDto.builder()
                            .id(p.getId())
                            .vehicleCategoryId(p.getVehicleCategoryId())
                            .currency(p.getCurrency())
                            .amount(p.getAmount())
                            .active(p.isActive())
                            .build())
                    .collect(Collectors.toList());
            dto.setPrices(priceDtos);

            // load packages
            List<TourPackageDto> packageDtos = tourPackageRepository.findByTourId(t.getId()).stream()
                    .map(pkg -> TourPackageDto.builder()
                            .id(pkg.getId())
                            .tourId(t.getId())
                            .name(pkg.getName())
                            .pricingType(pkg.getPricingType())
                            .adultPrice(pkg.getAdultPrice())
                            .childPrice(pkg.getChildPrice())
                            .infantPrice(pkg.getInfantPrice())
                            .childPolicyNote(pkg.getChildPolicyNote())
                            .infantPolicyNote(pkg.getInfantPolicyNote())
                            .fixedPrice(pkg.getFixedPrice())
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
                            .build())
                    .collect(Collectors.toList());
            dto.setPackages(packageDtos);

            return dto;
        }).orElse(null);
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
            t.setDurationDays(tourDto.getDurationDays());
            t.setDurationNights(tourDto.getDurationNights());
            t.setMinGuest(tourDto.getMinGuest());
            t.setMaxGuest(tourDto.getMaxGuest());
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

            // Update images
            if (tourDto.getImageIds() != null) {
                List<Image> images = imageRepository.findAllById(tourDto.getImageIds());
                t.setImages(images);
            }

            Tour saved = tourRepository.save(t);

            // Clear and recreate options
            tourOptionRepository.deleteAll(saved.getOptions());
            saved.getOptions().clear();
            tourOptionRepository.flush();
            if (tourDto.getOptions() != null) {
                for (TourOptionDto optDto : tourDto.getOptions()) {
                    TourOption opt = TourOption.builder()
                            .tour(saved)
                            .iconUrl(optDto.getIconUrl())
                            .colorText(optDto.getColorText())
                            .colorIcon(optDto.getColorIcon())
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
                    saved.getOptions().add(savedOpt);
                }
            }

            // Clear and recreate translations
            tourTranslationRepository.deleteAll(saved.getTranslations());
            saved.getTranslations().clear();
            tourTranslationRepository.flush();
            if (tourDto.getTranslations() != null) {
                for (TourTranslationDto transDto : tourDto.getTranslations()) {
                    String slug = transDto.getSlug() != null && !transDto.getSlug().isBlank()
                            ? transDto.getSlug()
                            : slugify(transDto.getName());
                    TourTranslation trans = TourTranslation.builder()
                            .tour(saved)
                            .locale(transDto.getLocale())
                            .name(transDto.getName())
                            .slug(slug)
                            .description(transDto.getDescription())
                            .content(transDto.getContent())
                            .build();
                    TourTranslation savedTrans = tourTranslationRepository.save(trans);
                    saved.getTranslations().add(savedTrans);
                }
            }

            // Clear and recreate prices
            List<TourPrice> existingPrices = tourPriceRepository.findByTourId(saved.getId());
            tourPriceRepository.deleteAll(existingPrices);
            tourPriceRepository.flush();
            if (tourDto.getPrices() != null) {
                for (TourPriceDto prDto : tourDto.getPrices()) {
                    TourPrice price = TourPrice.builder()
                            .tourId(saved.getId())
                            .vehicleCategoryId(prDto.getVehicleCategoryId())
                            .currency(prDto.getCurrency() != null ? prDto.getCurrency() : "VND")
                            .amount(prDto.getAmount())
                            .active(prDto.isActive())
                            .build();
                    tourPriceRepository.save(price);
                }
            }

            // Clear and recreate packages
            List<TourPackage> existingPackages = tourPackageRepository.findByTourId(saved.getId());
            tourPackageRepository.deleteAll(existingPackages);
            tourPackageRepository.flush();
            if (tourDto.getPackages() != null) {
                for (TourPackageDto pkgDto : tourDto.getPackages()) {
                    TourPackage pkg = TourPackage.builder()
                            .tour(saved)
                            .name(pkgDto.getName() != null ? pkgDto.getName() : "Gói Tiêu Chuẩn")
                            .pricingType(pkgDto.getPricingType() != null ? pkgDto.getPricingType() : "PER_PERSON")
                            .adultPrice(pkgDto.getAdultPrice() != null ? pkgDto.getAdultPrice() : java.math.BigDecimal.ZERO)
                            .childPrice(pkgDto.getChildPrice() != null ? pkgDto.getChildPrice() : java.math.BigDecimal.ZERO)
                            .infantPrice(pkgDto.getInfantPrice() != null ? pkgDto.getInfantPrice() : java.math.BigDecimal.ZERO)
                            .childPolicyNote(pkgDto.getChildPolicyNote())
                            .infantPolicyNote(pkgDto.getInfantPolicyNote())
                            .fixedPrice(pkgDto.getFixedPrice() != null ? pkgDto.getFixedPrice() : java.math.BigDecimal.ZERO)
                            .isContactOnly(pkgDto.isContactOnly())
                            .minGuests(pkgDto.getMinGuests() > 0 ? pkgDto.getMinGuests() : 1)
                            .maxGuests(pkgDto.getMaxGuests() > 0 ? pkgDto.getMaxGuests() : 30)
                            .highlights(pkgDto.getHighlights())
                            .inclusions(pkgDto.getInclusions())
                            .exclusions(pkgDto.getExclusions())
                            .active(pkgDto.isActive())
                            .sortOrder(pkgDto.getSortOrder())
                            .build();
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

            // Clear and recreate days
            List<TourDay> existingDays = tourDayRepository.findByTourId(saved.getId());
            for (TourDay day : existingDays) {
                List<TourDayTranslation> dayTrans = tourDayTranslationRepository.findByTourDayId(day.getId());
                tourDayTranslationRepository.deleteAll(dayTrans);
                tourDayTranslationRepository.flush();

                List<TourDayActivity> activities = tourDayActivityRepository.findByTourDayId(day.getId());
                for (TourDayActivity act : activities) {
                    List<TourDayActivityTranslation> actTrans = tourDayActivityTranslationRepository
                            .findByTourDayActivityId(act.getId());
                    tourDayActivityTranslationRepository.deleteAll(actTrans);
                    tourDayActivityTranslationRepository.flush();
                }
                tourDayActivityRepository.deleteAll(activities);
                tourDayActivityRepository.flush();
            }
            tourDayRepository.deleteAll(existingDays);
            saved.getDays().clear();
            tourDayRepository.flush();

            if (tourDto.getDays() != null && !tourDto.getDays().isEmpty()) {
                List<TourDay> tourDays = tourDto.getDays().stream().map(td -> TourDay.builder()
                        .tour(saved)
                        .dayNumber(td.getDayNumber())
                        .build())
                        .toList();
                tourDayRepository.saveAllAndFlush(tourDays);

                for (int i = 0; i < tourDto.getDays().size(); i++) {
                    TourDayDto tdDto = tourDto.getDays().get(i);
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
        return tourTranslationRepository.findBySlug(slug)
                .map(tr -> getTour(tr.getTour().getId()))
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(slug);
                        return getTour(id);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
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

    private TourDto toDto(Tour t) {
        List<Long> imageIds = t.getImages() != null
                ? t.getImages().stream().map(Image::getId).toList()
                : List.of();
        List<MediaDto> images = t.getImages() != null
                ? t.getImages().stream().map(img -> MediaDto.builder()
                        .id(img.getId())
                        .fileName(img.getFileName())
                        .filePath(img.getFilePath())
                        .fileSize(img.getFileSize())
                        .mimeType(img.getMimeType())
                        .width(img.getWidth())
                        .height(img.getHeight())
                        .build()).toList()
                : List.of();
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
                .inclusions(t.getInclusions())
                .exclusions(t.getExclusions())
                .build();
    }
}
