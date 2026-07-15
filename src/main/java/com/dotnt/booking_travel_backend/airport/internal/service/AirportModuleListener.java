package com.dotnt.booking_travel_backend.airport.internal.service;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportBookingDetail;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportBookingDetailRepository;
import com.dotnt.booking_travel_backend.airport.internal.repository.AirportTransferPriceRepository;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AirportModuleListener {
    private final AirportTransferPriceRepository airportTransferPriceRepository;
    private final AirportBookingDetailRepository airportBookingDetailRepository;

    @EventListener
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "airport.transferPrice.existsById" -> {
                Long id = toLong(ev.getPayload());
                boolean exists = id != null && airportTransferPriceRepository.existsById(id);
                ev.getFuture().complete(exists);
            }
            case "airport.bookingDetail.save" -> {
                AirportBookingDetail detail = (AirportBookingDetail) ev.getPayload();
                AirportBookingDetail saved = airportBookingDetailRepository.save(detail);
                ev.getFuture().complete(saved);
            }
            case "airport.bookingDetail.findById" -> {
                Long id = toLong(ev.getPayload());
                AirportBookingDetail result = id != null ? airportBookingDetailRepository.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
        }
    }

    @Transactional
    public AirportBookingDetail saveDetail(AirportBookingDetail detail) {
        return airportBookingDetailRepository.save(detail);
    }

    private Long toLong(Object payload) {
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) return Long.parseLong(s);
        return null;
    }
}
