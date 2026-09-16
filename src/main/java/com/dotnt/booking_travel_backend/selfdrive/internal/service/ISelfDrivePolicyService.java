package com.dotnt.booking_travel_backend.selfdrive.internal.service;

import com.dotnt.booking_travel_backend.selfdrive.api.dtos.SelfDrivePolicyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISelfDrivePolicyService {
    List<SelfDrivePolicyDto> getActivePolicies();

    Page<SelfDrivePolicyDto> list(Pageable pageable);

    SelfDrivePolicyDto get(Long id);

    SelfDrivePolicyDto create(SelfDrivePolicyDto dto);

    SelfDrivePolicyDto update(Long id, SelfDrivePolicyDto dto);

    void delete(Long id);

    SelfDrivePolicyDto toggleActive(Long id);
}
