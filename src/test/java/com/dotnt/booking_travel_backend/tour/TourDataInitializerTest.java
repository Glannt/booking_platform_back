package com.dotnt.booking_travel_backend.tour;

import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.tour.internal.config.TourDataInitializer;
import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPackage;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourTranslation;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TourDataInitializerTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourCategoryRepository tourCategoryRepository;
    @Mock
    private VehicleCategoryRepository vehicleCategoryRepository;
    @Mock
    private TourOptionRepository tourOptionRepository;
    @Mock
    private TourOptionTranslationRepository tourOptionTranslationRepository;
    @Mock
    private TourTranslationRepository tourTranslationRepository;
    @Mock
    private TourPriceRepository tourPriceRepository;
    @Mock
    private TourDayRepository tourDayRepository;
    @Mock
    private TourDayTranslationRepository tourDayTranslationRepository;
    @Mock
    private TourDayActivityRepository tourDayActivityRepository;
    @Mock
    private TourDayActivityTranslationRepository tourDayActivityTranslationRepository;
    @Mock
    private TourPackageRepository tourPackageRepository;
    @Mock
    private TourPackageTranslationRepository tourPackageTranslationRepository;
    @Mock
    private TourCategoryTranslationRepository tourCategoryTranslationRepository;

    @InjectMocks
    private TourDataInitializer tourDataInitializer;

    private Tour existingTour;

    @BeforeEach
    void setUp() {
        existingTour = Tour.builder()
                .active(true)
                .build();
        existingTour.setId(1L);
        // Notice: existingTour has NO translations loaded in its lazy list
    }

    @Test
    @DisplayName("TourDataInitializer seeds packages without LazyInitializationException when tours exist")
    void whenToursExistAndPackagesMissing_seedsUsingTourTranslationRepositoryWithoutLazyException() {
        when(tourRepository.count()).thenReturn(1L);
        when(tourPackageRepository.count()).thenReturn(0L);
        when(tourRepository.findAll()).thenReturn(List.of(existingTour));

        // Tour translations queried via repository directly (avoiding lazy loading on unmanaged entity)
        when(tourTranslationRepository.findByTourId(1L)).thenReturn(List.of(
                TourTranslation.builder().name("Tour 3 Đảo Nha Trang VIP").locale("vi").build(),
                TourTranslation.builder().name("Nha Trang 3 Islands VIP Tour").locale("en").build()
        ));

        when(tourPackageRepository.save(any(TourPackage.class))).thenAnswer(inv -> {
            TourPackage p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        assertDoesNotThrow(() -> tourDataInitializer.run());

        verify(tourRepository, atLeastOnce()).save(existingTour);
        verify(tourPackageRepository, times(3)).save(any(TourPackage.class));
        verify(tourPackageTranslationRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("TourDataInitializer skips seeding when packages already present")
    void whenPackagesAlreadySeeded_skipsSafely() {
        when(tourRepository.count()).thenReturn(1L);
        when(tourPackageRepository.count()).thenReturn(3L);

        assertDoesNotThrow(() -> tourDataInitializer.run());

        verify(tourRepository, never()).findAll();
        verify(tourPackageRepository, never()).save(any());
    }
}
