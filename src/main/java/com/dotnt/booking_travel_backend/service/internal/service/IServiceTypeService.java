package com.dotnt.booking_travel_backend.service.internal.service;

import com.dotnt.booking_travel_backend.service.api.CreateServiceTypeRequest;
import com.dotnt.booking_travel_backend.service.api.ServiceTypeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IServiceTypeService {
    ServiceTypeDto createServiceType(CreateServiceTypeRequest request);

    Page<ServiceTypeDto> getServiceTypes(Pageable pageable, String search, Boolean active);

    ServiceTypeDto getServiceTypeById(Long id);

    ServiceTypeDto updateServiceType(Long id, ServiceTypeDto serviceTypeDTO);

    void deleteServiceType(Long id);
}

