package com.dotnt.booking_travel_backend.setting.api;

import com.dotnt.booking_travel_backend.setting.api.dtos.CreateCurrencyRequest;
import com.dotnt.booking_travel_backend.setting.api.dtos.CurrencyDto;
import com.dotnt.booking_travel_backend.setting.internal.service.ICurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final ICurrencyService currencyService;

    @GetMapping
    public ResponseEntity<Page<CurrencyDto>> list(Pageable pageable) {
        return ResponseEntity.ok(currencyService.getCurrencies(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CurrencyDto>> listActive() {
        return ResponseEntity.ok(currencyService.getAllActiveCurrencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyDto> get(@PathVariable Long id) {
        CurrencyDto dto = currencyService.getCurrency(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('SETTING_UPDATE')")
    @PostMapping
    public ResponseEntity<CurrencyDto> create(@RequestBody CreateCurrencyRequest req) {
        return ResponseEntity.ok(currencyService.createCurrency(req));
    }

    @PreAuthorize("hasAuthority('SETTING_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<CurrencyDto> update(@PathVariable Long id, @RequestBody CurrencyDto dto) {
        CurrencyDto updated = currencyService.updateCurrency(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('SETTING_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SETTING_UPDATE')")
    @PostMapping("/sync")
    public ResponseEntity<List<CurrencyDto>> sync() {
        return ResponseEntity.ok(currencyService.syncRatesFromExternal());
    }
}
