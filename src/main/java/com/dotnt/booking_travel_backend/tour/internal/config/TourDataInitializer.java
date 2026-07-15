package com.dotnt.booking_travel_backend.tour.internal.config;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.tour.internal.entity.*;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(20)
public class TourDataInitializer implements CommandLineRunner {

    private final TourRepository tourRepository;
    private final TourCategoryRepository tourCategoryRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final TourOptionRepository tourOptionRepository;
    private final TourOptionTranslationRepository tourOptionTranslationRepository;
    private final TourTranslationRepository tourTranslationRepository;
    private final TourPriceRepository tourPriceRepository;
    private final TourDayRepository tourDayRepository;
    private final TourDayTranslationRepository tourDayTranslationRepository;
    private final TourDayActivityRepository tourDayActivityRepository;
    private final TourDayActivityTranslationRepository tourDayActivityTranslationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tourRepository.count() > 0) {
            log.info("Tours already seeded, skipping.");
            return;
        }

        log.info("Seeding tour data...");

        TourCategory category = TourCategory.builder()
                .title("Tour Du Lịch")
                .description("Các tour tham quan ngắm cảnh Nha Trang hấp dẫn")
                .isActive(true)
                .build();
        tourCategoryRepository.save(category);

        VehicleCategory cat4 = vehicleCategoryRepository.findByCode("4-seater").orElse(null);
        VehicleCategory cat7 = vehicleCategoryRepository.findByCode("7-seater").orElse(null);
        VehicleCategory cat16 = vehicleCategoryRepository.findByCode("16-seater").orElse(null);

        // Tour 1
        Tour tour1 = Tour.builder()
                .category(category)
                .durationDays(1)
                .durationNights(0)
                .minGuest(2)
                .maxGuest(30)
                .thumbnailId(1L)
                .active(true)
                .build();
        tourRepository.save(tour1);

        TourTranslation t1Vi = TourTranslation.builder()
                .tour(tour1)
                .locale("vi")
                .name("Tour 3 Đảo Nha Trang VIP (Cano Cao Tốc)")
                .description("Trải nghiệm cano cao tốc tham quan Bãi Tranh, Làng Chài và Vịnh San Hô với bữa trưa hải sản vô cùng hấp dẫn.")
                .content("<p>Hành trình khám phá vịnh Nha Trang xinh đẹp bằng cano cao tốc đời mới, đưa quý khách đến với những bãi tắm hoang sơ và trải nghiệm lặn biển ngắm san hô tuyệt vời.</p>")
                .build();
        TourTranslation t1En = TourTranslation.builder()
                .tour(tour1)
                .locale("en")
                .name("VIP Nha Trang 3 Islands Tour (Speedboat)")
                .description("Experience high-speed canoe sightseeing Bãi Tranh, Làng Chài, and Coral Bay with a delicious seafood lunch.")
                .content("<p>Explore the beautiful Nha Trang Bay by modern speedboat, taking you to pristine beaches and wonderful snorkeling spots.</p>")
                .build();
        tourTranslationRepository.saveAll(Arrays.asList(t1Vi, t1En));

        TourOption t1Opt1 = TourOption.builder().tour(tour1).iconUrl("ship").colorText("#0ea5e9").colorIcon("#0ea5e9").build();
        tourOptionRepository.save(t1Opt1);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt1).locale("vi").content("Cano cao tốc đời mới").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt1).locale("en").content("Modern Speedboat").build());

        TourOption t1Opt2 = TourOption.builder().tour(tour1).iconUrl("utensils").colorText("#f97316").colorIcon("#f97316").build();
        tourOptionRepository.save(t1Opt2);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt2).locale("vi").content("Ăn trưa hải sản").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt2).locale("en").content("Seafood Lunch").build());

        TourOption t1Opt3 = TourOption.builder().tour(tour1).iconUrl("shield").colorText("#22c55e").colorIcon("#22c55e").build();
        tourOptionRepository.save(t1Opt3);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt3).locale("vi").content("Bảo hiểm du lịch").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t1Opt3).locale("en").content("Travel Insurance").build());

        TourDay t1d1 = TourDay.builder().tour(tour1).dayNumber(1).build();
        tourDayRepository.save(t1d1);
        tourDayTranslationRepository.save(TourDayTranslation.builder().tourDay(t1d1).locale("vi").name("Khám phá 3 Đảo Nha Trang").description("08:00 - 16:00").build());
        tourDayTranslationRepository.save(TourDayTranslation.builder().tourDay(t1d1).locale("en").name("Discover 3 Islands").description("08:00 - 16:00").build());

        TourDayActivity t1Act1 = TourDayActivity.builder().tourDay(t1d1).startTime(Instant.parse("2026-07-08T08:00:00Z")).endTime(Instant.parse("2026-07-08T09:00:00Z")).location("Cảng Nha Trang").build();
        tourDayActivityRepository.save(t1Act1);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act1).locale("vi").name("Đón khách và Lên Cano").description("Xe đón khách tại khách sạn trung tâm TP Nha Trang di chuyển xuống cảng.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act1).locale("en").name("Pick up and Board Speedboat").description("Pick up guests from Nha Trang city center hotels to the port.").build());

        TourDayActivity t1Act2 = TourDayActivity.builder().tourDay(t1d1).startTime(Instant.parse("2026-07-08T09:30:00Z")).endTime(Instant.parse("2026-07-08T11:30:00Z")).location("Bãi Tranh").build();
        tourDayActivityRepository.save(t1Act2);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act2).locale("vi").name("Tự do tắm biển & Chụp ảnh").description("Quý khách tham quan chụp ảnh check-in và tắm biển Bãi Tranh.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act2).locale("en").name("Free Swim & Photography").description("Enjoy swimming, relaxing and checking in at Bãi Tranh Beach.").build());

        if (cat4 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour1.getId()).vehicleCategoryId(cat4.getId()).currency("VND").amount(new BigDecimal("600000")).active(true).build());
        if (cat7 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour1.getId()).vehicleCategoryId(cat7.getId()).currency("VND").amount(new BigDecimal("900000")).active(true).build());
        if (cat16 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour1.getId()).vehicleCategoryId(cat16.getId()).currency("VND").amount(new BigDecimal("1500000")).active(true).build());

        // Tour 2
        Tour tour2 = Tour.builder()
                .category(category)
                .durationDays(1)
                .durationNights(0)
                .minGuest(2)
                .maxGuest(20)
                .thumbnailId(2L)
                .active(true)
                .build();
        tourRepository.save(tour2);

        TourTranslation t2Vi = TourTranslation.builder()
                .tour(tour2).locale("vi")
                .name("Tour Du Thuyền 5 Sao Ngắm Hoàng Hôn Vịnh Nha Trang")
                .description("Thưởng thức tiệc cocktail hoàng hôn lãng mạn và bữa tối buffet sang trọng trên du thuyền 5 sao tiêu chuẩn quốc tế.")
                .content("<p>Hành trình lãng mạn ngắm mặt trời lặn trên vịnh Nha Trang lung linh ánh đèn. Trải nghiệm âm nhạc live acoustic du dương và các món ngon hảo hạng.</p>")
                .build();
        TourTranslation t2En = TourTranslation.builder()
                .tour(tour2).locale("en")
                .name("5-Star Sunset Cruise Tour Nha Trang Bay")
                .description("Enjoy a romantic sunset cocktail party and a luxurious buffet dinner on an international 5-star cruise ship.")
                .content("<p>A romantic journey watching the sun set over the glowing Nha Trang Bay. Experience live acoustic music and premium cuisine.</p>")
                .build();
        tourTranslationRepository.saveAll(Arrays.asList(t2Vi, t2En));

        TourOption t2Opt1 = TourOption.builder().tour(tour2).iconUrl("crown").colorText("#eab308").colorIcon("#eab308").build();
        tourOptionRepository.save(t2Opt1);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt1).locale("vi").content("Du thuyền 5 sao sang trọng").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt1).locale("en").content("Luxury 5-Star Cruise").build());

        TourOption t2Opt2 = TourOption.builder().tour(tour2).iconUrl("music").colorText("#ec4899").colorIcon("#ec4899").build();
        tourOptionRepository.save(t2Opt2);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt2).locale("vi").content("Tiệc cocktail & Nhạc live").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt2).locale("en").content("Sunset Cocktail & Live Music").build());

        TourOption t2Opt3 = TourOption.builder().tour(tour2).iconUrl("wine").colorText("#a855f7").colorIcon("#a855f7").build();
        tourOptionRepository.save(t2Opt3);
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt3).locale("vi").content("Ăn tối buffet chuẩn VIP").build());
        tourOptionTranslationRepository.save(TourOptionTranslation.builder().tourOption(t2Opt3).locale("en").content("VIP Buffet Dinner").build());

        TourDay t2d1 = TourDay.builder().tour(tour2).dayNumber(1).build();
        tourDayRepository.save(t2d1);
        tourDayTranslationRepository.save(TourDayTranslation.builder().tourDay(t2d1).locale("vi").name("Hành trình Hoàng Hôn Lãng Mạn").description("16:30 - 20:30").build());
        tourDayTranslationRepository.save(TourDayTranslation.builder().tourDay(t2d1).locale("en").name("Romantic Sunset Journey").description("16:30 - 20:30").build());

        TourDayActivity t2Act1 = TourDayActivity.builder().tourDay(t2d1).startTime(Instant.parse("2026-07-08T16:30:00Z")).endTime(Instant.parse("2026-07-08T17:00:00Z")).location("Cảng Nha Trang").build();
        tourDayActivityRepository.save(t2Act1);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act1).locale("vi").name("Lên du thuyền và Khởi hành").description("Chào đón khách lên tàu với nước uống trái cây và nhạc đón chào.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act1).locale("en").name("Board Cruise & Depart").description("Welcome guests on board with fresh drinks and welcome music.").build());

        TourDayActivity t2Act2 = TourDayActivity.builder().tourDay(t2d1).startTime(Instant.parse("2026-07-08T17:30:00Z")).endTime(Instant.parse("2026-07-08T20:30:00Z")).location("Du Thuyền").build();
        tourDayActivityRepository.save(t2Act2);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act2).locale("vi").name("Tiệc tối & Nghe nhạc Live").description("Thưởng thức tiệc buffet và ngắm nhìn hoàng hôn rực rỡ.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act2).locale("en").name("Dinner & Live Music").description("Enjoy buffet dinner and witness the magnificent sunset over Nha Trang Bay.").build());

        if (cat4 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour2.getId()).vehicleCategoryId(cat4.getId()).currency("VND").amount(new BigDecimal("1200000")).active(true).build());
        if (cat7 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour2.getId()).vehicleCategoryId(cat7.getId()).currency("VND").amount(new BigDecimal("1800000")).active(true).build());
        if (cat16 != null) tourPriceRepository.save(TourPrice.builder().tourId(tour2.getId()).vehicleCategoryId(cat16.getId()).currency("VND").amount(new BigDecimal("2800000")).active(true).build());

        log.info("Successfully seeded tour data!");
    }
}
