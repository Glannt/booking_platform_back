package com.dotnt.booking_travel_backend.intercity.internal.service.impl;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListDto;
import com.dotnt.booking_travel_backend.intercity.api.dtos.PriceListTranslationDto;
import com.dotnt.booking_travel_backend.intercity.internal.entity.*;
import com.dotnt.booking_travel_backend.intercity.internal.repository.*;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityPriceService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import com.dotnt.booking_travel_backend.shared.utils.PriceMath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntercityPriceServiceImpl implements IIntercityPriceService {

    private final PriceTypeRepository priceTypeRepository;
    private final PriceListRepository priceListRepository;
    private final PriceListTranslationRepository translationRepository;
    private final PriceListRouteRepository priceListRouteRepository;
    private final PriceListDailyRateRepository priceListDailyRateRepository;
    private final IntercityRouteRepository routeRepository;

    private static final String SCOPE_INTERCITY = "INTERCITY_ROUTE";

    @Override
    @Transactional(readOnly = true)
    public List<PriceListDto> listByRoute(Long routeId) {
        if (routeId == null) return Collections.emptyList();
        return priceListRepository.findByScopeTypeAndScopeRefId(SCOPE_INTERCITY, String.valueOf(routeId))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceListDto> listAll() {
        return priceListRepository.findByScopeType(SCOPE_INTERCITY)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PriceListDto get(Long id) {
        return priceListRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public PriceListDto savePrice(PriceListDto dto) {
        String routeRefId = String.valueOf(dto.getRouteId() != null ? dto.getRouteId() : dto.getScopeRefId());
        Long routeId = Long.parseLong(routeRefId);
        Long categoryId = dto.getVehicleCategoryId();

        // Ensure price types exist
        ensurePriceTypes();

        if (routeRefId == null || categoryId == null) {
            throw new IllegalArgumentException("routeId and vehicleCategoryId are required");
        }

        String statusCode = "ACTIVE";
        if (dto.getActive() != null) {
            statusCode = dto.getActive() ? "ACTIVE" : "INACTIVE";
        } else if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            statusCode = dto.getStatus();
        }

        PriceList savedResult = null;

        // Specific price type targeted (ONE_WAY, ROUND_TRIP_SAME_DAY, or DAILY_RATE)
        if (dto.getPriceTypeCode() != null && !dto.getPriceTypeCode().isBlank()) {
            String code = dto.getPriceTypeCode().toUpperCase();
            PriceType targetType = priceTypeRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalStateException("PriceType " + code + " not found"));

            PriceList targetList = null;
            if (dto.getId() != null) {
                targetList = priceListRepository.findById(dto.getId()).orElse(null);
            }
            if (targetList == null) {
                targetList = priceListRepository
                        .findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(SCOPE_INTERCITY, routeRefId, categoryId, code)
                        .orElse(PriceList.builder()
                                .priceType(targetType)
                                .scopeType(SCOPE_INTERCITY)
                                .scopeRefId(routeRefId)
                                .vehicleCategoryId(categoryId)
                                .build());
            }

            BigDecimal price = dto.getBasePrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                if ("ONE_WAY".equals(code) && dto.getOneWayPrice() != null) price = dto.getOneWayPrice();
                else if ("ROUND_TRIP_SAME_DAY".equals(code) && dto.getRoundTripPrice() != null) price = dto.getRoundTripPrice();
                else if ("DAILY_RATE".equals(code) && dto.getDailyRatePrice() != null) price = dto.getDailyRatePrice();
                else price = BigDecimal.ZERO;
            }

            targetList.setScopeRefId(routeRefId);
            targetList.setVehicleCategoryId(categoryId);
            targetList.setPriceType(targetType);
            targetList.setBasePrice(price);
            targetList.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "VND");
            targetList.setStatus(statusCode);
            targetList.setDepositPolicy(toDepositPolicy(dto));
            targetList = priceListRepository.save(targetList);

            if ("DAILY_RATE".equals(code)) {
                int minDays = dto.getMinDays() != null && dto.getMinDays() > 0 ? dto.getMinDays() : 1;
                PriceListDailyRate dr = priceListDailyRateRepository.findByPriceListId(targetList.getId())
                        .orElse(PriceListDailyRate.builder().priceList(targetList).build());
                dr.setMinDays(minDays);
                dr.setMaxDays(dto.getMaxDays());
                dr.setKmIncludedPerDay(dto.getKmIncludedPerDay());
                dr.setOveragePricePerKm(dto.getOveragePricePerKm());
                priceListDailyRateRepository.save(dr);
            } else {
                PriceListRoute pr = priceListRouteRepository.findByPriceListId(targetList.getId())
                        .orElse(PriceListRoute.builder().priceList(targetList).routeId(Long.parseLong(routeRefId)).build());
                pr.setRouteId(Long.parseLong(routeRefId));
                if (dto.getWaitingFee() != null) pr.setWaitingFee(dto.getWaitingFee());
                if (dto.getMaxWaitingMin() != null) pr.setMaxWaitingMin(dto.getMaxWaitingMin());
                priceListRouteRepository.save(pr);
            }

            saveDefaultTranslations(targetList, code, dto.getTranslations());

            if ("ACTIVE".equalsIgnoreCase(statusCode)) {
                deactivateConflictingActivePrices(targetList);
            }

            return toDto(targetList);
        }

        // Legacy fallback when priceTypeCode is not explicitly passed
        String typeCategory = dto.getPriceTypeCategory();
        if (typeCategory == null || typeCategory.isBlank()) {
            typeCategory = "IN_DAY";
        }

        if ("IN_DAY".equalsIgnoreCase(typeCategory)) {
            // Legacy batch path: write only the price types the caller actually priced. Both old
            // fallbacks here stored a figure nobody set — a missing round trip became
            // `oneWay * 2`, and a missing price became 0, which is a bookable free trip. Absent
            // input now means no row at all.
            PriceList oneWaySaved = saveLegacyPriceRow(dto, "ONE_WAY", routeRefId, categoryId, statusCode,
                    firstPositive(dto.getOneWayPrice(), dto.getBasePrice()));
            PriceList roundTripSaved = saveLegacyPriceRow(dto, "ROUND_TRIP_SAME_DAY", routeRefId, categoryId, statusCode,
                    firstPositive(dto.getRoundTripPrice()));

            if (oneWaySaved == null && roundTripSaved == null) {
                throw new IllegalArgumentException(
                        "Giá bắt buộc: cần oneWayPrice, roundTripPrice hoặc basePrice");
            }
            savedResult = oneWaySaved != null ? oneWaySaved : roundTripSaved;
        } else {
            PriceList dailySaved = saveLegacyPriceRow(dto, "DAILY_RATE", routeRefId, categoryId, statusCode,
                    firstPositive(dto.getDailyRatePrice(), dto.getBasePrice()));
            if (dailySaved == null) {
                throw new IllegalArgumentException("Giá bắt buộc: cần dailyRatePrice hoặc basePrice");
            }

            int minDays = dto.getMinDays() != null && dto.getMinDays() > 0 ? dto.getMinDays() : 1;
            PriceListDailyRate dr = priceListDailyRateRepository.findByPriceListId(dailySaved.getId())
                    .orElse(PriceListDailyRate.builder().priceList(dailySaved).build());
            dr.setMinDays(minDays);
            dr.setMaxDays(dto.getMaxDays());
            dr.setKmIncludedPerDay(dto.getKmIncludedPerDay());
            dr.setOveragePricePerKm(dto.getOveragePricePerKm());
            priceListDailyRateRepository.save(dr);

            savedResult = dailySaved;
        }

        return toDto(savedResult);
    }

    @Override
    @Transactional
    public PriceListDto updateStatus(Long id, boolean active) {
        PriceList pl = priceListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PriceList not found: " + id));
        pl.setStatus(active ? "ACTIVE" : "INACTIVE");
        pl = priceListRepository.save(pl);

        if (active) {
            deactivateConflictingActivePrices(pl);
        }

        return toDto(pl);
    }

    /**
     * Mutual exclusion: When activating a price list, ensure any other active price list
     * on the same route with matching vehicleCategoryId, matching priceType.category,
     * and matching priceType.code is automatically deactivated.
     */
    private void deactivateConflictingActivePrices(PriceList current) {
        if (current == null || current.getPriceType() == null) return;
        String category = current.getPriceType().getCategory();
        String code = current.getPriceType().getCode();
        Long categoryId = current.getVehicleCategoryId();
        String scopeRefId = current.getScopeRefId();
        String scopeType = current.getScopeType();

        List<PriceList> existingLists = priceListRepository.findByScopeTypeAndScopeRefIdAndVehicleCategoryId(scopeType, scopeRefId, categoryId);
        for (PriceList pl : existingLists) {
            if (pl.getId() != null && !pl.getId().equals(current.getId())) {
                PriceType pt = pl.getPriceType();
                if (pt != null && category.equalsIgnoreCase(pt.getCategory()) && code.equalsIgnoreCase(pt.getCode())) {
                    if ("ACTIVE".equalsIgnoreCase(pl.getStatus())) {
                        log.info("Mutual exclusion: deactivating conflicting price list #{} (route={}, vehicleCategory={}, typeCategory={}, typeCode={})",
                                pl.getId(), scopeRefId, categoryId, pt.getCategory(), pt.getCode());
                        pl.setStatus("INACTIVE");
                        priceListRepository.save(pl);
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        priceListRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public IntercityPriceCalculateResponse calculatePrice(IntercityPriceCalculateRequest req) {
        String routeRefId = String.valueOf(req.getRouteId());
        Long catId = req.getVehicleCategoryId();
        String code = normalizeCode(req.getPriceTypeCode());

        if ("DAILY_RATE".equalsIgnoreCase(code)) {
            Optional<PriceList> plOpt = priceListRepository
                    .findFirstByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCodeAndStatus(SCOPE_INTERCITY, routeRefId, catId, "DAILY_RATE", "ACTIVE")
                    .or(() -> priceListRepository.findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(SCOPE_INTERCITY, routeRefId, catId, "DAILY_RATE"));

            PriceList daily = plOpt.filter(p -> p.getBasePrice() != null && p.getBasePrice().signum() > 0)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No DAILY_RATE price configured for route " + routeRefId + " vehicle category " + catId));

            // minDays is a floor the customer is also told about, so it must apply here too —
            // otherwise the backend bills fewer days than the quote page showed.
            int minDays = priceListDailyRateRepository.findByPriceListId(daily.getId())
                    .map(PriceListDailyRate::getMinDays)
                    .filter(v -> v != null && v > 0)
                    .orElse(1);

            long days = minDays;
            if (req.getDepartureDate() != null && req.getReturnDate() != null) {
                long requested = ChronoUnit.DAYS.between(req.getDepartureDate(), req.getReturnDate());
                if (requested > 0) days = Math.max(requested, minDays);
            }

            BigDecimal total = daily.getBasePrice().multiply(BigDecimal.valueOf(days));

            return IntercityPriceCalculateResponse.builder()
                    .routeId(req.getRouteId())
                    .vehicleCategoryId(catId)
                    .priceTypeCode("DAILY_RATE")
                    .priceTypeName(resolveName(daily, req.getLocale(), "Giá theo ngày"))
                    .basePrice(daily.getBasePrice())
                    .days((int) days)
                    .totalPrice(PriceMath.roundToThousand(total))
                    .currency("VND")
                    .fallbackRoundTrip(false)
                    .build();
        }

        if ("ROUND_TRIP_SAME_DAY".equalsIgnoreCase(code)) {
            Optional<PriceList> rtOpt = priceListRepository
                    .findFirstByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCodeAndStatus(SCOPE_INTERCITY, routeRefId, catId, "ROUND_TRIP_SAME_DAY", "ACTIVE")
                    .or(() -> priceListRepository.findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(SCOPE_INTERCITY, routeRefId, catId, "ROUND_TRIP_SAME_DAY"));

            // No 2x-one-way fallback: a round trip costs whatever ROUND_TRIP_SAME_DAY says.
            // Doubling silently overcharged every same-day return trip (~11% on seeded data).
            PriceList roundTrip = rtOpt.filter(p -> p.getBasePrice() != null && p.getBasePrice().signum() > 0)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No ROUND_TRIP_SAME_DAY price configured for route " + routeRefId + " vehicle category " + catId));

            return IntercityPriceCalculateResponse.builder()
                    .routeId(req.getRouteId())
                    .vehicleCategoryId(catId)
                    .priceTypeCode("ROUND_TRIP_SAME_DAY")
                    .priceTypeName(resolveName(roundTrip, req.getLocale(), "Khứ hồi trong ngày"))
                    .basePrice(roundTrip.getBasePrice())
                    .days(1)
                    .totalPrice(PriceMath.roundToThousand(roundTrip.getBasePrice()))
                    .currency("VND")
                    .fallbackRoundTrip(false)
                    .build();
        }

        // Default: ONE_WAY
        Optional<PriceList> owOpt = priceListRepository
                .findFirstByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCodeAndStatus(SCOPE_INTERCITY, routeRefId, catId, "ONE_WAY", "ACTIVE")
                .or(() -> priceListRepository.findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(SCOPE_INTERCITY, routeRefId, catId, "ONE_WAY"));

        PriceList oneWay = owOpt.filter(p -> p.getBasePrice() != null && p.getBasePrice().signum() > 0)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No ONE_WAY price configured for route " + routeRefId + " vehicle category " + catId));

        return IntercityPriceCalculateResponse.builder()
                .routeId(req.getRouteId())
                .vehicleCategoryId(catId)
                .priceTypeCode("ONE_WAY")
                .priceTypeName(resolveName(oneWay, req.getLocale(), "Một chiều"))
                .basePrice(oneWay.getBasePrice())
                .days(1)
                .totalPrice(PriceMath.roundToThousand(oneWay.getBasePrice()))
                .currency("VND")
                .fallbackRoundTrip(false)
                .build();
    }

    @Override
    @Transactional
    public void initDefaultPrices() {
        log.info("Initializing standard default prices for all intercity routes...");
        ensurePriceTypes();

        priceListRepository.deleteByScopeTypeAndScopeRefId(SCOPE_INTERCITY, "%"); // clear all
        priceListRepository.findAll().stream()
                .filter(pl -> SCOPE_INTERCITY.equals(pl.getScopeType()))
                .forEach(priceListRepository::delete);

        var routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            log.warn("No intercity routes found to seed prices.");
            return;
        }

        // Standard Vehicle Categories (1: 4-seat, 2: 7-seat, 3: 16-seat, 4: Limousine 9-seat)
        List<Long> standardCategories = List.of(1L, 2L, 3L, 4L);

        for (var route : routes) {
            double distance = route.getDistanceKm() != null ? route.getDistanceKm().doubleValue() : 100.0;
            if (distance <= 0) distance = 100.0;

            for (Long catId : standardCategories) {
                double perKm;
                double minOneWay;
                double dailyRateAmount;

                if (catId == 1L) { // 4-seat
                    perKm = 10000;
                    minOneWay = 350000;
                    dailyRateAmount = 1200000;
                } else if (catId == 2L) { // 7-seat
                    perKm = 12500;
                    minOneWay = 450000;
                    dailyRateAmount = 1500000;
                } else if (catId == 3L) { // 16-seat
                    perKm = 18000;
                    minOneWay = 800000;
                    dailyRateAmount = 2200000;
                } else { // Limousine
                    perKm = 22000;
                    minOneWay = 1100000;
                    dailyRateAmount = 2800000;
                }

                BigDecimal oneWay = PriceMath.roundToThousand(BigDecimal.valueOf(Math.max(minOneWay, distance * perKm)));
                // Standard round-trip in same day gets a discount (~1.8x instead of 2x)
                BigDecimal roundTrip = PriceMath.roundToThousand(oneWay.multiply(BigDecimal.valueOf(1.8)));
                BigDecimal daily = PriceMath.roundToThousand(BigDecimal.valueOf(dailyRateAmount));

                // 1. ONE_WAY
                PriceType owType = priceTypeRepository.findByCode("ONE_WAY").orElse(null);
                if (owType != null) {
                    PriceList pl = PriceList.builder()
                            .priceType(owType)
                            .scopeType(SCOPE_INTERCITY)
                            .scopeRefId(String.valueOf(route.getId()))
                            .vehicleCategoryId(catId)
                            .basePrice(oneWay)
                            .currency("VND")
                            .status("ACTIVE")
                            .effectiveFrom(LocalDate.now())
                            .build();
                    pl = priceListRepository.save(pl);
                    saveDefaultTranslations(pl, "ONE_WAY", null);
                }

                // 2. ROUND_TRIP_SAME_DAY
                PriceType rtType = priceTypeRepository.findByCode("ROUND_TRIP_SAME_DAY").orElse(null);
                if (rtType != null) {
                    PriceList pl = PriceList.builder()
                            .priceType(rtType)
                            .scopeType(SCOPE_INTERCITY)
                            .scopeRefId(String.valueOf(route.getId()))
                            .vehicleCategoryId(catId)
                            .basePrice(roundTrip)
                            .currency("VND")
                            .status("ACTIVE")
                            .effectiveFrom(LocalDate.now())
                            .build();
                    pl = priceListRepository.save(pl);
                    saveDefaultTranslations(pl, "ROUND_TRIP_SAME_DAY", null);
                }

                // 3. DAILY_RATE
                PriceType drType = priceTypeRepository.findByCode("DAILY_RATE").orElse(null);
                if (drType != null) {
                    PriceList pl = PriceList.builder()
                            .priceType(drType)
                            .scopeType(SCOPE_INTERCITY)
                            .scopeRefId(String.valueOf(route.getId()))
                            .vehicleCategoryId(catId)
                            .basePrice(daily)
                            .currency("VND")
                            .status("ACTIVE")
                            .effectiveFrom(LocalDate.now())
                            .build();
                    pl = priceListRepository.save(pl);

                    PriceListDailyRate pdr = PriceListDailyRate.builder()
                            .priceList(pl)
                            .minDays(1)
                            .kmIncludedPerDay(BigDecimal.valueOf(250))
                            .overagePricePerKm(BigDecimal.valueOf(8000))
                            .build();
                    priceListDailyRateRepository.save(pdr);

                    saveDefaultTranslations(pl, "DAILY_RATE", null);
                }
            }
        }
        log.info("Initialized standard default prices for {} routes successfully.", routes.size());
    }

    private void ensurePriceTypes() {
        if (!priceTypeRepository.existsByCode("ONE_WAY")) {
            priceTypeRepository.save(PriceType.builder()
                    .code("ONE_WAY")
                    .category("IN_DAY")
                    .defaultName("Một chiều")
                    .description("Chuyến đi một chiều theo tuyến cố định")
                    .active(true)
                    .build());
        }
        if (!priceTypeRepository.existsByCode("ROUND_TRIP_SAME_DAY")) {
            priceTypeRepository.save(PriceType.builder()
                    .code("ROUND_TRIP_SAME_DAY")
                    .category("IN_DAY")
                    .defaultName("Khứ hồi trong ngày")
                    .description("Chuyến đi và về trong cùng ngày")
                    .active(true)
                    .build());
        }
        if (!priceTypeRepository.existsByCode("DAILY_RATE")) {
            priceTypeRepository.save(PriceType.builder()
                    .code("DAILY_RATE")
                    .category("DAILY_RATE")
                    .defaultName("Giá theo ngày")
                    .description("Thuê xe có tài xế theo ngày (nhân theo số ngày)")
                    .active(true)
                    .build());
        }
    }

    private void saveDefaultTranslations(PriceList pl, String code, List<PriceListTranslationDto> customList) {
        List<PriceListTranslation> existing = translationRepository.findByPriceListId(pl.getId());
        Map<String, PriceListTranslation> existingMap = existing.stream()
                .filter(t -> t.getLocale() != null)
                .collect(Collectors.toMap(t -> t.getLocale().toLowerCase(), t -> t, (a, b) -> a));

        if (customList != null && !customList.isEmpty()) {
            for (var c : customList) {
                if (c.getLocale() == null || c.getLocale().isBlank()) continue;
                String loc = c.getLocale().toLowerCase();
                PriceListTranslation tr = existingMap.get(loc);
                if (tr != null) {
                    tr.setName(c.getName() != null && !c.getName().isBlank() ? c.getName() : tr.getName());
                    if (c.getDescription() != null) {
                        tr.setDescription(c.getDescription());
                    }
                    translationRepository.save(tr);
                } else {
                    translationRepository.save(PriceListTranslation.builder()
                            .priceList(pl)
                            .locale(loc)
                            .name(c.getName())
                            .description(c.getDescription())
                            .build());
                }
            }
            return;
        }

        Map<String, String> names = new HashMap<>();
        if ("ONE_WAY".equals(code)) {
            names.put("vi", "Một chiều");
            names.put("en", "One-way");
            names.put("ko", "편도");
            names.put("ru", "В одну сторону");
        } else if ("ROUND_TRIP_SAME_DAY".equals(code)) {
            names.put("vi", "Khứ hồi trong ngày");
            names.put("en", "Round trip");
            names.put("ko", "왕복");
            names.put("ru", "В обе стороны");
        } else {
            names.put("vi", "Giá theo ngày");
            names.put("en", "Daily rate");
            names.put("ko", "일일 요금");
            names.put("ru", "Посуточный тариф");
        }

        for (var entry : names.entrySet()) {
            String loc = entry.getKey().toLowerCase();
            PriceListTranslation tr = existingMap.get(loc);
            if (tr != null) {
                if (tr.getName() == null || tr.getName().isBlank()) {
                    tr.setName(entry.getValue());
                    translationRepository.save(tr);
                }
            } else {
                translationRepository.save(PriceListTranslation.builder()
                        .priceList(pl)
                        .locale(loc)
                        .name(entry.getValue())
                        .description(entry.getValue())
                        .build());
            }
        }
    }

    private String resolveName(PriceList pl, String locale, String defaultName) {
        if (pl == null) return defaultName;
        String loc = locale != null && !locale.isBlank() ? locale : "vi";
        return translationRepository.findByPriceListId(pl.getId()).stream()
                .filter(t -> loc.equalsIgnoreCase(t.getLocale()))
                .map(PriceListTranslation::getName)
                .findFirst()
                .orElse(pl.getPriceType() != null ? pl.getPriceType().getDefaultName() : defaultName);
    }

    /**
 * Legacy batch-path row writer. Returns null when no usable price was supplied, which is the
 * honest answer: the caller simply did not price that type, so there is nothing to store.
 */
private PriceList saveLegacyPriceRow(PriceListDto dto, String priceTypeCode, String routeRefId,
        Long categoryId, String statusCode, BigDecimal basePrice) {
    if (basePrice == null) return null;

    PriceType type = priceTypeRepository.findByCode(priceTypeCode)
            .orElseThrow(() -> new IllegalStateException("PriceType " + priceTypeCode + " not found"));

    PriceList list = priceListRepository
            .findByScopeTypeAndScopeRefIdAndVehicleCategoryIdAndPriceTypeCode(SCOPE_INTERCITY, routeRefId, categoryId, priceTypeCode)
            .orElseGet(() -> PriceList.builder()
                    .priceType(type)
                    .scopeType(SCOPE_INTERCITY)
                    .scopeRefId(routeRefId)
                    .vehicleCategoryId(categoryId)
                    .build());

    list.setPriceType(type);
    list.setBasePrice(basePrice);
    list.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "VND");
    list.setStatus(statusCode);
    list.setDepositPolicy(toDepositPolicy(dto));
    list = priceListRepository.save(list);
    saveDefaultTranslations(list, priceTypeCode, dto.getTranslations());

    if ("ACTIVE".equalsIgnoreCase(statusCode)) {
        deactivateConflictingActivePrices(list);
    }
    return list;
}

/** First candidate that is a real price. Null, zero and negative all count as "not supplied". */
private BigDecimal firstPositive(BigDecimal... candidates) {
    for (BigDecimal c : candidates) {
        if (c != null && c.compareTo(BigDecimal.ZERO) > 0) return c;
    }
    return null;
}

private String normalizeCode(String code) {
        if (code == null) return "ONE_WAY";
        String upper = code.toUpperCase();
        if (upper.contains("MULTI") || upper.contains("DAILY")) return "DAILY_RATE";
        if (upper.contains("ROUND") || upper.contains("RETURN")) return "ROUND_TRIP_SAME_DAY";
        return "ONE_WAY";
    }

    /**
     * Deposit on a price row is opt-in: all three fields blank means "no deposit", and the policy
     * stays null. DepositPolicies.from() would write DEFAULT_RATE (20%) into the columns instead,
     * which is the write-time default that made route-level deposits unreachable elsewhere.
     */
    private DepositPolicy toDepositPolicy(PriceListDto dto) {
        boolean noType = dto.getDepositType() == null || dto.getDepositType().isBlank();
        if (noType && dto.getDepositRate() == null && dto.getDepositAmount() == null) {
            return null;
        }
        return DepositPolicies.from(dto.getDepositType(), dto.getDepositRate(), dto.getDepositAmount());
    }

    private PriceListDto toDto(PriceList pl) {
        if (pl == null) return null;
        Long routeId = null;
        try {
            routeId = Long.parseLong(pl.getScopeRefId());
        } catch (Exception ignored) {}

        var translations = translationRepository.findByPriceListId(pl.getId()).stream()
                .map(t -> PriceListTranslationDto.builder()
                        .id(t.getId())
                        .priceListId(pl.getId())
                        .locale(t.getLocale())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toList());

        var routeDetails = priceListRouteRepository.findByPriceListId(pl.getId()).orElse(null);
        var dailyDetails = priceListDailyRateRepository.findByPriceListId(pl.getId()).orElse(null);
        var deposit = pl.getDepositPolicy();

        String typeCode = pl.getPriceType() != null ? pl.getPriceType().getCode() : "ONE_WAY";
        String category = pl.getPriceType() != null ? pl.getPriceType().getCategory() : "IN_DAY";

        boolean isActive = "ACTIVE".equalsIgnoreCase(pl.getStatus());
        Boolean inDayActive = "IN_DAY".equalsIgnoreCase(category) ? isActive : null;
        Boolean dailyRateActive = "DAILY_RATE".equalsIgnoreCase(category) ? isActive : null;

        return PriceListDto.builder()
                .id(pl.getId())
                .priceTypeId(pl.getPriceType() != null ? pl.getPriceType().getId() : null)
                .priceTypeCode(typeCode)
                .priceTypeCategory(category)
                .scopeType(pl.getScopeType())
                .scopeRefId(pl.getScopeRefId())
                .routeId(routeId)
                .vehicleCategoryId(pl.getVehicleCategoryId())
                .scopeMeta(pl.getScopeMeta())
                .currency(pl.getCurrency())
                .basePrice(pl.getBasePrice())
                .depositType(deposit != null ? deposit.getType() : null)
                .depositRate(deposit != null ? deposit.getRate() : null)
                .depositAmount(deposit != null ? deposit.getAmount() : null)
                .effectiveFrom(pl.getEffectiveFrom())
                .effectiveTo(pl.getEffectiveTo())
                .status(pl.getStatus())
                .active(isActive)
                .inDayActive(inDayActive)
                .dailyRateActive(dailyRateActive)
                .translations(translations)
                .waitingFee(routeDetails != null ? routeDetails.getWaitingFee() : BigDecimal.ZERO)
                .maxWaitingMin(routeDetails != null ? routeDetails.getMaxWaitingMin() : 0)
                .minDays(dailyDetails != null ? dailyDetails.getMinDays() : 1)
                .maxDays(dailyDetails != null ? dailyDetails.getMaxDays() : null)
                .kmIncludedPerDay(dailyDetails != null ? dailyDetails.getKmIncludedPerDay() : null)
                .overagePricePerKm(dailyDetails != null ? dailyDetails.getOveragePricePerKm() : null)
                .build();
    }
}
