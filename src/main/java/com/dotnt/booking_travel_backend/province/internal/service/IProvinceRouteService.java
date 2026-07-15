package com.dotnt.booking_travel_backend.province.internal.service;

import com.dotnt.booking_travel_backend.province.api.dtos.ProvinceRouteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProvinceRouteService {
    ProvinceRouteDto create(ProvinceRouteDto dto);
    Page<ProvinceRouteDto> list(Pageable pageable);
    ProvinceRouteDto get(Long id);
    ProvinceRouteDto update(Long id, ProvinceRouteDto dto);
    void delete(Long id);
}

