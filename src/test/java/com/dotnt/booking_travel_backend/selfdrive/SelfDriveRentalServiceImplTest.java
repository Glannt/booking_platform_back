package com.dotnt.booking_travel_backend.selfdrive;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.service.FleetModuleListener;
import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDriveRental;
import com.dotnt.booking_travel_backend.selfdrive.internal.mapper.SelfDriveMapper;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveRentalRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveTypeTranslationRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.service.impl.SelfDriveRentalServiceImpl;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SelfDriveRentalServiceImplTest {

    @Mock
    private SelfDriveRentalRepository rentalRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private SelfDriveTypeRepository rentalTypeRepository;

    @Mock
    private SelfDriveTypeTranslationRepository rentalTypeTranslationRepository;

    @Mock
    private SelfDriveMapper rentalMapper;

    @Mock
    private VehicleCategoryRepository vehicleCategoryRepository;

    private FleetModuleListener fleetModuleListener;
    private SelfDriveRentalServiceImpl rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new SelfDriveRentalServiceImpl(
                rentalRepository,
                applicationEventPublisher,
                rentalTypeRepository,
                rentalTypeTranslationRepository,
                rentalMapper
        );
        fleetModuleListener = new FleetModuleListener(vehicleCategoryRepository);

        doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof ModuleRequestEvent ev) {
                fleetModuleListener.handleModuleRequest(ev);
            }
            return null;
        }).when(applicationEventPublisher).publishEvent(any(Object.class));

        doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof ModuleRequestEvent ev) {
                fleetModuleListener.handleModuleRequest(ev);
            }
            return null;
        }).when(applicationEventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void whenUpdateSelfDriveRental_withValidVehicleCategory_succeeds() {
        Long categoryId = 1L;
        Long rentalId = 10L;

        VehicleCategory cat = VehicleCategory.builder().code("4_SEATS").build();
        cat.setId(categoryId);
        when(vehicleCategoryRepository.findById(categoryId)).thenReturn(Optional.of(cat));

        SelfDriveRental existingSelfDriveRental = SelfDriveRental.builder()
                .vehicleCategoryId(categoryId)
                .deposit(BigDecimal.valueOf(200000))
                .minimumDays(1)
                .active(true)
                .build();
        existingSelfDriveRental.setId(rentalId);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(existingSelfDriveRental));
        when(rentalRepository.findByVehicleCategoryIdAndActiveTrue(categoryId)).thenReturn(Collections.singletonList(existingSelfDriveRental));
        when(rentalRepository.save(any(SelfDriveRental.class))).thenAnswer(i -> i.getArgument(0));

        SelfDriveRentalDto dto = SelfDriveRentalDto.builder()
                .vehicleCategoryId(categoryId)
                .deposit(BigDecimal.valueOf(210000))
                .minimumDays(1)
                .active(true)
                .driverRequired(false)
                .build();

        SelfDriveRentalDto result = rentalService.update(rentalId, dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(210000), result.getDeposit());
        assertEquals(categoryId, result.getVehicleCategoryId());
    }
}
