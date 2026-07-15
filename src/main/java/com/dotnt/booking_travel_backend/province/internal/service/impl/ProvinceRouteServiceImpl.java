package com.dotnt.booking_travel_backend.province.internal.service.impl;

import com.dotnt.booking_travel_backend.province.api.dtos.ProvinceRouteDto;
import com.dotnt.booking_travel_backend.province.internal.entity.ProvinceRoute;
import com.dotnt.booking_travel_backend.province.internal.repository.ProvinceRouteRepository;
import com.dotnt.booking_travel_backend.province.internal.service.IProvinceRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvinceRouteServiceImpl implements IProvinceRouteService {
    private final ProvinceRouteRepository repository;

    @Override
    public ProvinceRouteDto create(ProvinceRouteDto dto) {
        ProvinceRoute r = ProvinceRoute.builder()
                .code(dto.getCode())
                .origin(dto.getOrigin())
                .destination(dto.getDestination())
                .distance(dto.getDistance())
                .duration(dto.getDuration())
                .active(dto.isActive())
                .build();
        ProvinceRoute saved = repository.save(r);
        return toDto(saved);
    }

    @Override
    public Page<ProvinceRouteDto> list(Pageable pageable) {
        Page<ProvinceRoute> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public ProvinceRouteDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public ProvinceRouteDto update(Long id, ProvinceRouteDto dto) {
        return repository.findById(id).map(r -> {
            r.setCode(dto.getCode());
            r.setOrigin(dto.getOrigin());
            r.setDestination(dto.getDestination());
            r.setDistance(dto.getDistance());
            r.setDuration(dto.getDuration());
            r.setActive(dto.isActive());
            return toDto(repository.save(r));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ProvinceRouteDto toDto(ProvinceRoute r) {
        return ProvinceRouteDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .origin(r.getOrigin())
                .destination(r.getDestination())
                .distance(r.getDistance())
                .duration(r.getDuration())
                .active(r.isActive())
                .build();
    }
}

