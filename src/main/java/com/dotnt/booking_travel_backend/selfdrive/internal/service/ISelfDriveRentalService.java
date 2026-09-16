package com.dotnt.booking_travel_backend.selfdrive.internal.service;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveRentalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISelfDriveRentalService {
    SelfDriveRentalDto create(SelfDriveRentalDto dto);
    Page<SelfDriveRentalDto> list(Pageable pageable);
    SelfDriveRentalDto get(Long id);
    SelfDriveRentalDto update(Long id, SelfDriveRentalDto dto);
    void delete(Long id);
}
