package com.dotnt.booking_travel_backend.intercity.internal.service;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListDto;

import java.util.List;

public interface IIntercityPriceService {
    List<PriceListDto> listByRoute(Long routeId);
    List<PriceListDto> listAll();
    PriceListDto get(Long id);
    PriceListDto savePrice(PriceListDto dto);
    PriceListDto updateStatus(Long id, boolean active);
    void delete(Long id);
    IntercityPriceCalculateResponse calculatePrice(IntercityPriceCalculateRequest request);
    void initDefaultPrices();
}
