package com.dotnt.booking_travel_backend.selfdrive.internal.service;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDriveTypeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISelfDriveTypeService {
    SelfDriveTypeDto create(SelfDriveTypeDto dto);
    Page<SelfDriveTypeDto> list(Pageable pageable);
    SelfDriveTypeDto get(Long id);
    SelfDriveTypeDto update(Long id, SelfDriveTypeDto dto);
    void delete(Long id);
}
