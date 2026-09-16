package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedOfficeDto;

import java.util.List;

public interface ISharedOfficeService {
    List<SharedOfficeDto> getAllOffices(boolean activeOnly);
    List<SharedOfficeDto> getOfficesByCity(String city);
    SharedOfficeDto getOfficeById(Long id);
    SharedOfficeDto createOffice(SharedOfficeDto dto);
    SharedOfficeDto updateOffice(Long id, SharedOfficeDto dto);
    void deleteOffice(Long id);
}
