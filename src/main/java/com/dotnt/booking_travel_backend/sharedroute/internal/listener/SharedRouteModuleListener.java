package com.dotnt.booking_travel_backend.sharedroute.internal.listener;

import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedRoute;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SharedRouteModuleListener {

    private final SharedRouteRepository sharedRouteRepository;

    @EventListener
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "sharedroute.findByLinkedIntercityRouteId" -> {
                Long routeId = toLong(ev.getPayload());
                if (routeId == null) {
                    ev.getFuture().complete(null);
                    return;
                }
                Long linkedId = sharedRouteRepository.findByLinkedIntercityRouteIdAndActiveTrue(routeId)
                        .map(SharedRoute::getId)
                        .orElse(null);
                ev.getFuture().complete(linkedId);
            }
            default -> log.trace("SharedRouteModuleListener ignoring: {}", ev.getRequestType());
        }
    }

    private Long toLong(Object payload) {
        if (payload == null) return null;
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
