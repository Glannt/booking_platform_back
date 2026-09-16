package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.TourPolicyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TourPolicyService {
    List<TourPolicyDto> getActivePolicies();

    Page<TourPolicyDto> list(Pageable pageable);

    TourPolicyDto get(Long id);

    TourPolicyDto create(TourPolicyDto dto);

    TourPolicyDto update(Long id, TourPolicyDto dto);

    void delete(Long id);

    TourPolicyDto toggleActive(Long id);
}
