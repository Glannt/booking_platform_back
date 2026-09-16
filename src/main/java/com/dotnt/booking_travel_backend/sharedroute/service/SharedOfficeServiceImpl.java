package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedOfficeDto;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedOffice;
import com.dotnt.booking_travel_backend.sharedroute.internal.repository.SharedOfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedOfficeServiceImpl implements ISharedOfficeService {

    private final SharedOfficeRepository officeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SharedOfficeDto> getAllOffices(boolean activeOnly) {
        List<SharedOffice> offices = activeOnly ? officeRepository.findByActiveTrue() : officeRepository.findAll();
        return offices.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedOfficeDto> getOfficesByCity(String city) {
        return officeRepository.findByCityAndActiveTrue(city).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SharedOfficeDto getOfficeById(Long id) {
        SharedOffice office = officeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Office not found with id: " + id));
        return toDto(office);
    }

    @Override
    public SharedOfficeDto createOffice(SharedOfficeDto dto) {
        SharedOffice office = SharedOffice.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        office = officeRepository.save(office);
        return toDto(office);
    }

    @Override
    public SharedOfficeDto updateOffice(Long id, SharedOfficeDto dto) {
        SharedOffice office = officeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Office not found with id: " + id));

        office.setName(dto.getName());
        office.setAddress(dto.getAddress());
        office.setCity(dto.getCity());
        office.setLatitude(dto.getLatitude());
        office.setLongitude(dto.getLongitude());
        if (dto.getActive() != null) {
            office.setActive(dto.getActive());
        }

        office = officeRepository.save(office);
        return toDto(office);
    }

    @Override
    public void deleteOffice(Long id) {
        officeRepository.deleteById(id);
    }

    private SharedOfficeDto toDto(SharedOffice office) {
        return SharedOfficeDto.builder()
                .id(office.getId())
                .name(office.getName())
                .address(office.getAddress())
                .city(office.getCity())
                .latitude(office.getLatitude())
                .longitude(office.getLongitude())
                .active(office.isActive())
                .build();
    }
}
