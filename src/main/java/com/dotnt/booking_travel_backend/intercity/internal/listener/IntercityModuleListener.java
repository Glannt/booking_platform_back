package com.dotnt.booking_travel_backend.intercity.internal.listener;

import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityBookingDetail;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoute;
import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityRoutePrice;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityBookingDetailRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRoutePriceRepository;
import com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityRouteRepository;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntercityModuleListener {

    private final IntercityBookingDetailRepository intercityRepo;
    private final IntercityRouteRepository intercityRouteRepo;
    private final IntercityRoutePriceRepository intercityPriceRepo;

    @EventListener
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "intercity.intercityBookingDetail.save", "rental.intercityBookingDetail.save" -> {
                IntercityBookingDetail detail = (IntercityBookingDetail) ev.getPayload();
                IntercityBookingDetail saved = intercityRepo.save(detail);
                ev.getFuture().complete(saved);
            }
            case "intercity.intercityBookingDetail.findById", "rental.intercityBookingDetail.findById" -> {
                Long id = toLong(ev.getPayload());
                IntercityBookingDetail result = id != null ? intercityRepo.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
            case "intercity.intercityRoute.findById", "rental.intercityRoute.findById" -> {
                Long id = toLong(ev.getPayload());
                IntercityRoute result = id != null ? intercityRouteRepo.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
            case "intercity.intercityRoutePrice.findByRouteAndCategory", "rental.intercityRoutePrice.findByRouteAndCategory" -> {
                Object[] ids = (Object[]) ev.getPayload();
                IntercityRoutePrice result = ids != null && ids.length >= 2 && toLong(ids[0]) != null && toLong(ids[1]) != null
                        ? intercityPriceRepo.findByRouteIdAndVehicleCategoryId(toLong(ids[0]), toLong(ids[1])).orElse(null)
                        : null;
                ev.getFuture().complete(result);
            }
            case "intercity.intercityRoute.findOriginDestById" -> {
                Long id = toLong(ev.getPayload());
                String[] result = id != null ? intercityRouteRepo.findById(id)
                        .map(r -> new String[]{r.getOriginCity(), r.getDestinationCity()})
                        .orElse(null) : null;
                ev.getFuture().complete(result);
            }
            case "intercity.intercityRoute.findIdByCities" -> {
                Object[] cities = (Object[]) ev.getPayload();
                if (cities != null && cities.length >= 2) {
                    String origin = String.valueOf(cities[0]);
                    String dest = String.valueOf(cities[1]);
                    Long foundId = intercityRouteRepo.findAll().stream()
                            .filter(r -> origin.equalsIgnoreCase(r.getOriginCity()) && dest.equalsIgnoreCase(r.getDestinationCity()))
                            .map(IntercityRoute::getId)
                            .findFirst()
                            .orElse(null);
                    ev.getFuture().complete(foundId);
                } else {
                    ev.getFuture().complete(null);
                }
            }
            default -> log.debug("IntercityModuleListener ignoring: {}", ev.getRequestType());
        }
    }

    private Long toLong(Object payload) {
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) return Long.parseLong(s);
        return null;
    }
}
