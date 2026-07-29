package com.dotnt.booking_travel_backend.setting.internal.config;

import com.dotnt.booking_travel_backend.setting.internal.entity.Currency;
import com.dotnt.booking_travel_backend.setting.internal.entity.CurrencyTranslation;
import com.dotnt.booking_travel_backend.setting.internal.repository.CurrencyRepository;
import com.dotnt.booking_travel_backend.setting.internal.service.ICurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(5)
public class CurrencyDataInitializer implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;
    private final ICurrencyService currencyService;

    private static final Map<String, CurrencySeed> SEEDS = Map.of(
            "VND", new CurrencySeed("VND", "₫", "Đồng Việt Nam", "Vietnamese Dong", "베트남 동", "Вьетнамский донг"),
            "USD", new CurrencySeed("USD", "$", "Đô la Mỹ", "US Dollar", "미국 달러", "Доллар США"),
            "KRW", new CurrencySeed("KRW", "₩", "Won Hàn Quốc", "South Korean Won", "한국 원", "Корейская вона"),
            "RUB", new CurrencySeed("RUB", "₽", "Rúp Nga", "Russian Ruble", "러시아 루블", "Российский рубль")
    );

    @Override
    public void run(String... args) {
        if (currencyRepository.count() > 0) {
            log.info("Currencies already seeded, skipping.");
            return;
        }

        log.info("Seeding currency data...");

        for (CurrencySeed seed : SEEDS.values()) {
            Currency currency = Currency.builder()
                    .code(seed.code)
                    .symbol(seed.symbol)
                    .rateToVnd(BigDecimal.ONE)
                    .isActive(true)
                    .translations(new ArrayList<>())
                    .build();
            Currency saved = currencyRepository.save(currency);

            List<CurrencyTranslation> translations = List.of(
                    CurrencyTranslation.builder().currency(saved).locale("vi").name(seed.vi).build(),
                    CurrencyTranslation.builder().currency(saved).locale("en").name(seed.en).build(),
                    CurrencyTranslation.builder().currency(saved).locale("ko").name(seed.ko).build(),
                    CurrencyTranslation.builder().currency(saved).locale("ru").name(seed.ru).build()
            );
            saved.getTranslations().addAll(translations);
            currencyRepository.save(saved);
        }

        log.info("Seeded {} currencies, syncing rates...", SEEDS.size());

        try {
            currencyService.syncRatesFromExternal();
            log.info("Currency rates synced from API plugin.");
        } catch (Exception e) {
            log.warn("Failed to sync currency rates from API, using placeholder rates.", e);
        }
    }

    private record CurrencySeed(String code, String symbol, String vi, String en, String ko, String ru) {}
}
