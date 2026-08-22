package com.dotnt.booking_travel_backend.rental.internal.service;

import com.dotnt.booking_travel_backend.rental.api.dtos.IntercityRouteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IIntercityRouteService {
    IntercityRouteDto create(IntercityRouteDto dto);
    Page<IntercityRouteDto> list(Pageable pageable);
    List<IntercityRouteDto> listActive();
    IntercityRouteDto get(Long id);
    IntercityRouteDto getBySlug(String slug);
    IntercityRouteDto update(Long id, IntercityRouteDto dto);
    void delete(Long id);
}
