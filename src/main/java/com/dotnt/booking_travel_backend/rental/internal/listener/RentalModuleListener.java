package com.dotnt.booking_travel_backend.rental.internal.listener;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.entity.SelfDriveBookingDetail;
import com.dotnt.booking_travel_backend.rental.internal.repository.IntercityBookingDetailRepository;
import com.dotnt.booking_travel_backend.rental.internal.repository.SelfDriveBookingDetailRepository;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalModuleListener {

    private final IntercityBookingDetailRepository intercityRepo;
    private final SelfDriveBookingDetailRepository selfDriveRepo;

    @EventListener
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "rental.intercityBookingDetail.save" -> {
                IntercityBookingDetail detail = (IntercityBookingDetail) ev.getPayload();
                IntercityBookingDetail saved = intercityRepo.save(detail);
                ev.getFuture().complete(saved);
            }
            case "rental.intercityBookingDetail.findById" -> {
                Long id = toLong(ev.getPayload());
                IntercityBookingDetail result = id != null ? intercityRepo.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
            case "rental.selfDriveBookingDetail.save" -> {
                SelfDriveBookingDetail detail = (SelfDriveBookingDetail) ev.getPayload();
                SelfDriveBookingDetail saved = selfDriveRepo.save(detail);
                ev.getFuture().complete(saved);
            }
            case "rental.selfDriveBookingDetail.findById" -> {
                Long id = toLong(ev.getPayload());
                SelfDriveBookingDetail result = id != null ? selfDriveRepo.findById(id).orElse(null) : null;
                ev.getFuture().complete(result);
            }
            default -> log.debug("RentalModuleListener ignoring: {}", ev.getRequestType());
        }
    }

    private Long toLong(Object payload) {
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) return Long.parseLong(s);
        return null;
    }
}
