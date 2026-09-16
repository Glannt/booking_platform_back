package com.dotnt.booking_travel_backend.setting.internal.service.impl;

import com.dotnt.booking_travel_backend.setting.api.dtos.CreateCurrencyRequest;
import com.dotnt.booking_travel_backend.setting.api.dtos.CurrencyDto;
import com.dotnt.booking_travel_backend.setting.api.dtos.CurrencyTranslationDto;
import com.dotnt.booking_travel_backend.setting.internal.entity.Currency;
import com.dotnt.booking_travel_backend.setting.internal.entity.CurrencyTranslation;
import com.dotnt.booking_travel_backend.setting.internal.repository.CurrencyRepository;
import com.dotnt.booking_travel_backend.setting.internal.repository.CurrencyTranslationRepository;
import com.dotnt.booking_travel_backend.setting.internal.service.ICurrencyService;
import com.dotnt.booking_travel_backend.shared.utils.EntityMergeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements ICurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyTranslationRepository currencyTranslationRepository;

    @Value("${app.currency.api-base-url}")
    private String apiBaseUrl;

    @Value("${app.currency.api-key}")
    private String apiKey;

    @Override
    @Transactional
    public CurrencyDto createCurrency(CreateCurrencyRequest request) {
        Currency currency = Currency.builder()
                .code(request.getCode().toUpperCase())
                .symbol(request.getSymbol())
                .rateToVnd(request.getRateToVnd())
                .isActive(request.isActive())
                .translations(new ArrayList<>())
                .build();
        Currency saved = currencyRepository.save(currency);

        if (request.getTranslations() != null) {
            for (CurrencyTranslationDto transDto : request.getTranslations()) {
                CurrencyTranslation trans = CurrencyTranslation.builder()
                        .currency(saved)
                        .locale(transDto.getLocale())
                        .name(transDto.getName())
                        .build();
                currencyTranslationRepository.save(trans);
            }
        }

        return getCurrency(saved.getId());
    }

    @Override
    public Page<CurrencyDto> getCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public List<CurrencyDto> getAllActiveCurrencies() {
        return currencyRepository.findAll().stream()
                .filter(Currency::isActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyDto getCurrency(Long id) {
        return currencyRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public CurrencyDto updateCurrency(Long id, CurrencyDto dto) {
        Currency existing = currencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found"));

        existing.setCode(dto.getCode().toUpperCase());
        existing.setSymbol(dto.getSymbol());
        existing.setRateToVnd(dto.getRateToVnd());
        existing.setActive(dto.isActive());

        if (dto.getTranslations() != null) {
            final Currency finalCurrency = existing;
            EntityMergeUtils.mergeCollection(
                    existing.getTranslations(),
                    dto.getTranslations(),
                    t -> t.getLocale() != null ? t.getLocale().toLowerCase() : null,
                    tDto -> tDto.getLocale() != null ? tDto.getLocale().toLowerCase() : null,
                    (tr, tDto) -> {
                        tr.setName(tDto.getName());
                    },
                    tDto -> CurrencyTranslation.builder()
                            .currency(finalCurrency)
                            .locale(tDto.getLocale().toLowerCase())
                            .name(tDto.getName())
                            .build()
            );
        }

        currencyRepository.save(existing);
        return getCurrency(id);
    }

    @Override
    @Transactional
    public void deleteCurrency(Long id) {
        currencyRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<CurrencyDto> syncRatesFromExternal() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = apiBaseUrl + "/" + apiKey + "/rates";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> json = parseSimpleJson(response.body());

                if (json != null && json.containsKey("rates")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Double> rates = (Map<String, Double>) json.get("rates");

                    Double vndRate = rates.get("VND");
                    if (vndRate == null || vndRate <= 0) {
                        log.warn("VND rate not found in API response");
                        return currencyRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
                    }

                    List<Currency> currencies = currencyRepository.findAll();
                    for (Currency currency : currencies) {
                        String code = currency.getCode();
                        if ("VND".equals(code)) {
                            currency.setRateToVnd(BigDecimal.ONE);
                        } else {
                            Double rate = rates.get(code);
                            if (rate != null && rate > 0) {
                                // API returns rates relative to USD; compute 1 foreign = ? VND
                                double rateToVnd = vndRate / rate;
                                currency.setRateToVnd(BigDecimal.valueOf(rateToVnd));
                            }
                        }
                        currencyRepository.save(currency);
                    }

                    log.info("Currency rates synced from API plugin ({} currencies updated)", currencies.size());
                }
            } else {
                log.warn("Failed to sync rates: HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error syncing currency rates", e);
        }

        return currencyRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Minimal JSON parser — extracts top-level string/number/object values.
     * Avoids pulling in a full JSON library for this simple use case.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSimpleJson(String json) {
        try {
            // Use a basic approach: parse with built-in tools
            // For a production system, consider using Jackson which is already on classpath
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON response", e);
            return null;
        }
    }

    private CurrencyDto toDto(Currency currency) {
        List<CurrencyTranslationDto> transDtos = currency.getTranslations().stream()
                .map(t -> CurrencyTranslationDto.builder()
                        .id(t.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .build())
                .collect(Collectors.toList());

        return CurrencyDto.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .symbol(currency.getSymbol())
                .rateToVnd(currency.getRateToVnd())
                .active(currency.isActive())
                .translations(transDtos)
                .build();
    }
}
