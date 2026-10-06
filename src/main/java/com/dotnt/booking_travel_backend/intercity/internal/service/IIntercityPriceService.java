package com.dotnt.booking_travel_backend.intercity.internal.service;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListDto;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;

import java.util.List;

public interface IIntercityPriceService {
    List<PriceListDto> listByRoute(Long routeId);
    List<PriceListDto> listAll();
    PriceListDto get(Long id);
    PriceListDto savePrice(PriceListDto dto);
    PriceListDto updateStatus(Long id, boolean active);
    void delete(Long id);
    IntercityPriceCalculateResponse calculatePrice(IntercityPriceCalculateRequest request);

    /**
     * The deposit carried by whichever price row {@link #calculatePrice} would pick for this trip,
     * so the booking flow can price the line and the deposit off the same row.
     *
     * @return the stored policy, or {@code null} when no row matches or that row has none — never
     *         a defaulted figure, because "not configured" is the one case the caller has to see
     */
    DepositPolicy resolveDepositPolicy(IntercityPriceCalculateRequest request);

    void initDefaultPrices();
}
