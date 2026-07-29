package com.dotnt.booking_travel_backend.setting.internal.service;

import com.dotnt.booking_travel_backend.setting.api.dtos.CreateCurrencyRequest;
import com.dotnt.booking_travel_backend.setting.api.dtos.CurrencyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICurrencyService {
    CurrencyDto createCurrency(CreateCurrencyRequest request);
    Page<CurrencyDto> getCurrencies(Pageable pageable);
    List<CurrencyDto> getAllActiveCurrencies();
    CurrencyDto getCurrency(Long id);
    CurrencyDto updateCurrency(Long id, CurrencyDto dto);
    void deleteCurrency(Long id);
    List<CurrencyDto> syncRatesFromExternal();
}
