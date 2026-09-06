package com.dotnt.booking_travel_backend.setting.api;

import com.dotnt.booking_travel_backend.setting.api.dtos.CurrencyDto;
import com.dotnt.booking_travel_backend.setting.internal.service.ICurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/currencies")
@RequiredArgsConstructor
public class PublicCurrencyController {

    private final ICurrencyService currencyService;

    @GetMapping("/active")
    public ResponseEntity<List<CurrencyDto>> listActive() {
        return ResponseEntity.ok(currencyService.getAllActiveCurrencies());
    }

    @GetMapping
    public ResponseEntity<List<CurrencyDto>> list() {
        return ResponseEntity.ok(currencyService.getAllActiveCurrencies());
    }
}
