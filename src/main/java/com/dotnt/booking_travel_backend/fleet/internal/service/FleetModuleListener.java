package com.dotnt.booking_travel_backend.fleet.internal.service;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FleetModuleListener {
    private final VehicleCategoryRepository vehicleCategoryRepository;

    @EventListener
    @Transactional(readOnly = true)
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "fleet.vehicleCategory.exists" -> {
                Object payload = ev.getPayload();
                Long id = toLong(payload);
                boolean exists = id != null && vehicleCategoryRepository.findById(id).isPresent();
                ev.getFuture().complete(exists);
            }
            case "fleet.vehicleCategory.findById" -> {
                Long id = toLong(ev.getPayload());
                VehicleCategory result = id != null ? vehicleCategoryRepository.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
            case "fleet.vehicleCategory.findByCode" -> {
                String code = ev.getPayload() instanceof String s ? s : null;
                VehicleCategory result = code != null ? vehicleCategoryRepository.findByCode(code).orElse(null) : null;
                ev.getFuture().complete(result);
            }
        }
    }

    private Long toLong(Object payload) {
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) return Long.parseLong(s);
        return null;
    }
}
