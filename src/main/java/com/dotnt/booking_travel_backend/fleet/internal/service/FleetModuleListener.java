package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FleetModuleListener {
    private final VehicleCategoryRepository vehicleCategoryRepository;

    @EventListener
    public void handleModuleRequest(ModuleRequestEvent<?> ev) {
        if ("vehicleCategory.exists".equals(ev.getRequestType())) {
            Object payload = ev.getPayload();
            Long id = null;
            if (payload instanceof Number) id = ((Number) payload).longValue();
            else if (payload instanceof String) id = Long.valueOf((String) payload);
            boolean exists = id != null && vehicleCategoryRepository.findById(id).isPresent();
            //noinspection unchecked
            ((ModuleRequestEvent<Boolean>) ev).getFuture().complete(exists);
        }
    }
}

