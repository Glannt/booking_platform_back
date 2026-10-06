package com.dotnt.booking_travel_backend.tour;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourRequest;
import com.dotnt.booking_travel_backend.tour.api.dtos.PriceListPerPersonDto;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourDto;
import com.dotnt.booking_travel_backend.tour.api.dtos.TourPackageDto;
import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import com.dotnt.booking_travel_backend.tour.internal.mapper.TourMapper;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import com.dotnt.booking_travel_backend.tour.internal.service.TourConfigService;
import com.dotnt.booking_travel_backend.tour.internal.service.impl.TourServiceImpl;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * T2.5: a tour cannot reach `active` unless it can actually be sold.
 *
 * <p>Both entry points are covered because the rejection is not symmetric by construction — the
 * create path validates a package it just built, while the update path validates a package that
 * already exists and has just been mutated in place. An admin who writes a price-less tour as a
 * draft and switches it on next week has to be stopped by the second one, and a rule checked only
 * on creation would let them straight through.
 */
@ExtendWith(MockitoExtension.class)
class TourPublishableRuleTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourCategoryRepository tourCategoryRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private TourDayRepository tourDayRepository;
    @Mock
    private TourDayActivityRepository tourDayActivityRepository;
    @Mock
    private TourDayTranslationRepository tourDayTranslationRepository;
    @Mock
    private TourDayActivityTranslationRepository tourDayActivityTranslationRepository;
    @Mock
    private TourOptionRepository tourOptionRepository;
    @Mock
    private TourOptionTranslationRepository tourOptionTranslationRepository;
    @Mock
    private TourTranslationRepository tourTranslationRepository;
    @Mock
    private TourPackageRepository tourPackageRepository;
    @Mock
    private TourPackageTranslationRepository tourPackageTranslationRepository;
    @Mock
    private TourMapper tourMapper;
    @Mock
    private ModuleEventPublisher moduleEventPublisher;
    @Mock
    private TourConfigService tourConfigService;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour existingTour;

    @BeforeEach
    void setUp() {
        existingTour = Tour.builder()
                .slug("tour-missing-price")
                .active(false)
                .status("INACTIVE")
                .build();
        existingTour.setId(7L);

        // Shared plumbing so each test only states its own subject. Lenient because the create-path
        // stubs go unused in the update tests and vice versa.
        lenient().when(tourMapper.toDto(any(Tour.class))).thenReturn(new TourDto());
        lenient().when(tourRepository.existsBySlug(any())).thenReturn(false);
        lenient().when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> {
            Tour t = inv.getArgument(0);
            if (t.getId() == null) t.setId(7L);
            return t;
        });
        lenient().when(tourPackageRepository.save(any(TourPackage.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    /** active, no price anywhere, no contact-only flag — the case that used to publish for free. */
    private TourPackageDto unpricedPackage(boolean active) {
        return unpricedPackage(active, false);
    }

    private TourPackageDto unpricedPackage(boolean active, boolean contactOnly) {
        return TourPackageDto.builder()
                .id(10L)
                .name("Gói thiếu giá")
                .active(active)
                .isContactOnly(contactOnly)
                .prices(List.of())
                .build();
    }

    private CreateTourRequest.CreateTourRequestBuilder createRequest(TourPackageDto pkg) {
        return CreateTourRequest.builder()
                .status("ACTIVE")
                .packages(List.of(pkg));
    }

    @Test
    @DisplayName("Create: active with no price and no contact-only flag is rejected by name")
    void testCreateRejectsAnActivePackageWithNoPrice() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tourService.createTour(createRequest(unpricedPackage(true)).build()));

        assertTrue(ex.getMessage().contains("Gói thiếu giá"),
                "the admin needs to know which package is at fault; got: " + ex.getMessage());
        verify(tourPackageRepository, never()).save(any(TourPackage.class));
    }

    @Test
    @DisplayName("Create: an explicit 0 price is a missing price, not a bargain")
    void testCreateRejectsAnActivePackagePricedAtZero() {
        TourPackageDto zeroAdult = unpricedPackage(true);
        zeroAdult.setPrices(List.of(
                PriceListPerPersonDto.builder()
                        .passengerType("ADULT")
                        .pricePerPerson(BigDecimal.ZERO)
                        .build()));

        assertThrows(IllegalArgumentException.class,
                () -> tourService.createTour(createRequest(zeroAdult).build()));
    }

    @Test
    @DisplayName("Create: a contact-only package is accepted as a quote request")
    void testCreateAcceptsAnActiveContactOnlyPackage() {
        TourPackageDto contactOnly = unpricedPackage(true, true);

        assertDoesNotThrow(() -> tourService.createTour(createRequest(contactOnly).build()));
    }

    @Test
    @DisplayName("Update: switching an unpriced package on is rejected")
    void testUpdateRejectsFlippingOnAnUnpricedPackage() {
        TourPackage existingPackage = TourPackage.builder()
                .tour(existingTour)
                .name("Gói thiếu giá")
                .active(false)
                .build();
        existingPackage.setId(10L);
        existingPackage.setPrices(new java.util.ArrayList<>());
        existingTour.getPackages().add(existingPackage);
        when(tourRepository.findById(7L)).thenReturn(Optional.of(existingTour));

        TourDto dto = new TourDto();
        dto.setActive(true);
        dto.setPackages(List.of(unpricedPackage(true)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tourService.updateTour(7L, dto));

        assertTrue(ex.getMessage().contains("Gói thiếu giá"),
                "the rejection has to re-check on update, not only at creation; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("Update: a contact-only package stays switchable")
    void testUpdateAcceptsFlippingOnAContactOnlyPackage() {
        TourPackage existingPackage = TourPackage.builder()
                .tour(existingTour)
                .name("Gói liên hệ")
                .active(false)
                .isContactOnly(true)
                .build();
        existingPackage.setId(10L);
        existingPackage.setPrices(new java.util.ArrayList<>());
        existingTour.getPackages().add(existingPackage);
        when(tourRepository.findById(7L)).thenReturn(Optional.of(existingTour));

        TourDto dto = new TourDto();
        dto.setActive(true);
        dto.setPackages(List.of(unpricedPackage(true, true)));

        assertDoesNotThrow(() -> tourService.updateTour(7L, dto));
    }
}
