package com.dotnt.booking_travel_backend.tour.internal.config;

import com.dotnt.booking_travel_backend.tour.internal.entity.*;
import com.dotnt.booking_travel_backend.tour.internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(20)
@Transactional
public class TourDataInitializer implements CommandLineRunner {

    private final TourRepository tourRepository;
    private final TourCategoryRepository tourCategoryRepository;
    private final TourOptionRepository tourOptionRepository;
    private final TourOptionTranslationRepository tourOptionTranslationRepository;
    private final TourTranslationRepository tourTranslationRepository;
    private final TourDayRepository tourDayRepository;
    private final TourDayTranslationRepository tourDayTranslationRepository;
    private final TourDayActivityRepository tourDayActivityRepository;
    private final TourDayActivityTranslationRepository tourDayActivityTranslationRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourPackageTranslationRepository tourPackageTranslationRepository;
    private final TourCategoryTranslationRepository tourCategoryTranslationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tourRepository.count() > 0) {
            log.info("Tours already seeded, verifying packages...");
            seedPackagesIfMissing();
            return;
        }

        log.info("Seeding tour data...");

        TourCategory canoCategory = tourCategoryRepository.findByCode("island-cano").orElseGet(() -> {
            TourCategory cat = TourCategory.builder()
                    .code("island-cano")
                    .icon("ship")
                    .sortOrder(1)
                    .isActive(true)
                    .title("Tour Đảo (Cano / Thuyền)")
                    .description("Khám phá các hòn đảo xinh đẹp vịnh Nha Trang bằng cano cao tốc")
                    .build();
            cat = tourCategoryRepository.save(cat);

            List<TourCategoryTranslation> trans = List.of(
                    TourCategoryTranslation.builder().tourCategory(cat).locale("vi").name("Tour Đảo (Cano / Thuyền)").slug("tour-dao-cano").description("Khám phá các hòn đảo xinh đẹp vịnh Nha Trang bằng cano cao tốc").build(),
                    TourCategoryTranslation.builder().tourCategory(cat).locale("en").name("Island Tour (Speedboat)").slug("island-tour-speedboat").description("Explore the scenic Nha Trang islands by speedboat").build(),
                    TourCategoryTranslation.builder().tourCategory(cat).locale("ko").name("아일랜드 투어 (스피드보트)").slug("island-tour-speedboat-ko").description("스피드보트를 타고 나트랑의 아름다운 섬들을 탐험하세요").build(),
                    TourCategoryTranslation.builder().tourCategory(cat).locale("ru").name("Островной тур (катер)").slug("island-tour-speedboat-ru").description("Исследуйте живописные острова залива Нячанг на скоростном катере").build()
            );
            tourCategoryTranslationRepository.saveAll(trans);
            return cat;
        });

        if (tourCategoryRepository.findByCode("land-car").isEmpty()) {
            TourCategory carCat = TourCategory.builder()
                    .code("land-car")
                    .icon("car")
                    .sortOrder(2)
                    .isActive(true)
                    .title("Tour Xe / Đất Liền")
                    .description("Khám phá danh lam thắng cảnh và thành phố bằng xe ô tô du lịch")
                    .build();
            carCat = tourCategoryRepository.save(carCat);

            List<TourCategoryTranslation> carTrans = List.of(
                    TourCategoryTranslation.builder().tourCategory(carCat).locale("vi").name("Tour Xe / Đất Liền").slug("tour-xe-dat-lien").description("Khám phá danh lam thắng cảnh và thành phố bằng xe ô tô du lịch").build(),
                    TourCategoryTranslation.builder().tourCategory(carCat).locale("en").name("Land / Car Tour").slug("land-car-tour").description("Explore city sights and mainland attractions by comfortable car").build(),
                    TourCategoryTranslation.builder().tourCategory(carCat).locale("ko").name("육상 / 차량 투어").slug("land-car-tour-ko").description("편안한 차량으로 도시 명소와 육상 관광지를 둘러보세요").build(),
                    TourCategoryTranslation.builder().tourCategory(carCat).locale("ru").name("Наземный тур (авто)").slug("land-car-tour-ru").description("Экскурсии по достопримечательностям города и материка на автомобиле").build()
            );
            tourCategoryTranslationRepository.saveAll(carTrans);
        }

        // Tour 1
        Tour tour1 = Tour.builder()
                .category(canoCategory)
                .slug("tour-3-dao-nha-trang-vip")
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
                .slug("tour-3-dao-nha-trang-vip")
                .description("Trải nghiệm cano cao tốc tham quan Bãi Tranh, Làng Chài và Vịnh San Hô với bữa trưa hải sản vô cùng hấp dẫn.")
                .content("<p>Hành trình khám phá vịnh Nha Trang xinh đẹp bằng cano cao tốc đời mới, đưa quý khách đến với những bãi tắm hoang sơ và trải nghiệm lặn biển ngắm san hô tuyệt vời.</p>")
                .build();
        TourTranslation t1En = TourTranslation.builder()
                .tour(tour1)
                .locale("en")
                .name("VIP Nha Trang 3 Islands Tour (Speedboat)")
                .slug("vip-nha-trang-3-islands-tour")
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

        TourDayActivity t1Act1 = TourDayActivity.builder().tourDay(t1d1).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0)).location("Cảng Nha Trang").build();
        tourDayActivityRepository.save(t1Act1);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act1).locale("vi").name("Đón khách và Lên Cano").description("Xe đón khách tại khách sạn trung tâm TP Nha Trang di chuyển xuống cảng.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act1).locale("en").name("Pick up and Board Speedboat").description("Pick up guests from Nha Trang city center hotels to the port.").build());

        TourDayActivity t1Act2 = TourDayActivity.builder().tourDay(t1d1).startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(11, 30)).location("Bãi Tranh").build();
        tourDayActivityRepository.save(t1Act2);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act2).locale("vi").name("Tự do tắm biển & Chụp ảnh").description("Quý khách tham quan chụp ảnh check-in và tắm biển Bãi Tranh.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t1Act2).locale("en").name("Free Swim & Photography").description("Enjoy swimming, relaxing and checking in at Bãi Tranh Beach.").build());

        // Tour 2
        Tour tour2 = Tour.builder()
                .category(canoCategory)
                .slug("tour-du-thuyen-5-sao-ngam-hoang-hon-vinh-nha-trang")
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
                .slug("tour-du-thuyen-5-sao-ngam-hoang-hon-vinh-nha-trang")
                .description("Thưởng thức tiệc cocktail hoàng hôn lãng mạn và bữa tối buffet sang trọng trên du thuyền 5 sao tiêu chuẩn quốc tế.")
                .content("<p>Hành trình lãng mạn ngắm mặt trời lặn trên vịnh Nha Trang lung linh ánh đèn. Trải nghiệm âm nhạc live acoustic du dương và các món ngon hảo hạng.</p>")
                .build();
        TourTranslation t2En = TourTranslation.builder()
                .tour(tour2).locale("en")
                .name("5-Star Sunset Cruise Tour Nha Trang Bay")
                .slug("5-star-sunset-cruise-tour-nha-trang-bay")
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

        TourDayActivity t2Act1 = TourDayActivity.builder().tourDay(t2d1).startTime(LocalTime.of(16, 30)).endTime(LocalTime.of(17, 0)).location("Cảng Nha Trang").build();
        tourDayActivityRepository.save(t2Act1);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act1).locale("vi").name("Lên du thuyền và Khởi hành").description("Chào đón khách lên tàu với nước uống trái cây và nhạc đón chào.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act1).locale("en").name("Board Cruise & Depart").description("Welcome guests on board with fresh drinks and welcome music.").build());

        TourDayActivity t2Act2 = TourDayActivity.builder().tourDay(t2d1).startTime(LocalTime.of(17, 30)).endTime(LocalTime.of(20, 30)).location("Du Thuyền").build();
        tourDayActivityRepository.save(t2Act2);
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act2).locale("vi").name("Tiệc tối & Nghe nhạc Live").description("Thưởng thức tiệc buffet và ngắm nhìn hoàng hôn rực rỡ.").build());
        tourDayActivityTranslationRepository.save(TourDayActivityTranslation.builder().tourDayActivity(t2Act2).locale("en").name("Dinner & Live Music").description("Enjoy buffet dinner and witness the magnificent sunset over Nha Trang Bay.").build());

        log.info("Successfully seeded tour data!");
        seedPackagesIfMissing();
    }

    private void seedPackagesIfMissing() {
        if (tourPackageRepository.count() > 0) {
            return;
        }
        log.info("Seeding tour packages and inclusions/exclusions for tours...");
        List<Tour> allTours = tourRepository.findAll();
        for (Tour tour : allTours) {
            List<TourTranslation> translations = tourTranslationRepository.findByTourId(tour.getId());
            boolean isTour1 = translations.stream()
                    .anyMatch(tr -> tr.getName() != null && (tr.getName().contains("3 Đảo") || tr.getName().contains("3 Islands")));
            if (isTour1) {
                tour.setInclusions(
                        "- Cano ghép theo hành trình.\n" +
                        "- Bữa trưa theo thực đơn tour.\n" +
                        "- Vé tham quan theo chương trình.\n" +
                        "- Xe & HDV đón khách tại khách sạn/điểm hẹn trung tâm theo lịch tour.\n" +
                        "- Tham quan Bãi Tranh, Làng Chài và Hòn Tằm theo chương trình."
                );
                tour.setExclusions(
                        "- Mô tô nước.\n" +
                        "- Dù bay.\n" +
                        "- Lặn ngắm san hô.\n" +
                        "- Đi bộ dưới đáy biển.\n" +
                        "- Phao chuối và các dịch vụ ngoài chương trình."
                );
                tourRepository.save(tour);

                // Gói 1: Cano ghép – Tiêu chuẩn
                List<PriceListPerPerson> p1Prices = new ArrayList<>(List.of(
                        PriceListPerPerson.builder().passengerType("ADULT").pricePerPerson(new BigDecimal("650000")).minPax(1).maxPax(30).active(true).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(new BigDecimal("450000")).minPax(1).maxPax(30).active(true).build(),
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).minPax(1).maxPax(30).active(true).build()
                ));
                TourPackage pkg1 = TourPackage.builder()
                        .tour(tour)
                        .name("Cano ghép – Tiêu chuẩn")
                        .prices(p1Prices)
                        .childPolicyNote("Trẻ em 1m–1m29: 450.000đ")
                        .infantPolicyNote("Dưới 1m: Miễn phí")
                        .highlights(
                                "- Cano ghép theo hành trình\n" +
                                "- Ăn trưa theo thực đơn tour\n" +
                                "- Vé tham quan theo chương trình\n" +
                                "- Trẻ em 1m–1m29: 450.000đ"
                        )
                        .inclusions(
                                "- Cano ghép theo hành trình.\n" +
                                "- Bữa trưa hải sản theo thực đơn tour.\n" +
                                "- Vé tham quan các đảo theo chương trình.\n" +
                                "- Xe & HDV đưa đón trung tâm."
                        )
                        .exclusions(
                                "- Trò chơi thể thao biển (mô tô nước, dù bay).\n" +
                                "- Lặn ngắm san hô, đi bộ dưới đáy biển.\n" +
                                "- Chi phí cá nhân phát sinh ngoài tour."
                        )
                        .active(true)
                        .sortOrder(1)
                        .build();
                TourPackage savedPkg1 = tourPackageRepository.save(pkg1);
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg1).locale("vi").name("Cano ghép – Tiêu chuẩn")
                        .highlights(pkg1.getHighlights()).inclusions(pkg1.getInclusions()).exclusions(pkg1.getExclusions())
                        .childPolicyNote("Trẻ em 1m–1m29: 450.000đ").infantPolicyNote("Dưới 1m: Miễn phí").build());
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg1).locale("en").name("Shared Speedboat – Standard")
                        .highlights("- Shared high-speed boat\n- Seafood lunch\n- Sightseeing tickets\n- Child 1m–1m29: 450.000 VND")
                        .inclusions("- Shared speedboat itinerary.\n- Lunch menu included.\n- Island entrance tickets.\n- City center transfer.")
                        .exclusions("- Water sports (jetski, parasailing).\n- Scuba diving / Sea walking.\n- Personal expenses.")
                        .childPolicyNote("Children 1m–1m29: 450,000 VND").infantPolicyNote("Under 1m: Free").build());

                // Gói 2: Hòn Tằm – Tắm Bùn
                List<PriceListPerPerson> p2Prices = new ArrayList<>(List.of(
                        PriceListPerPerson.builder().passengerType("ADULT").pricePerPerson(new BigDecimal("950000")).minPax(1).maxPax(30).active(true).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(new BigDecimal("700000")).minPax(1).maxPax(30).active(true).build(),
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).minPax(1).maxPax(30).active(true).build()
                ));
                TourPackage pkg2 = TourPackage.builder()
                        .tour(tour)
                        .name("Hòn Tằm – Tắm Bùn")
                        .prices(p2Prices)
                        .childPolicyNote("Trẻ em 1m–1m29: 700.000đ")
                        .infantPolicyNote("Dưới 1m: Miễn phí")
                        .highlights(
                                "- Chương trình Tour 3 đảo trọn gói\n" +
                                "- Thêm vé tắm bùn khoáng nóng & tiện ích Hòn Tằm\n" +
                                "- Ăn trưa hải sản cao cấp\n" +
                                "- Trẻ em 1m–1m29: 700.000đ"
                        )
                        .inclusions(
                                "- Toàn bộ dịch vụ của Tour 3 đảo.\n" +
                                "- Vé tắm bùn khoáng nóng thiên nhiên Hòn Tằm.\n" +
                                "- Sử dụng hồ bơi vô cực, khăn tắm, đồ tắm miễn phí tại Hòn Tằm.\n" +
                                "- Bữa trưa hải sản."
                        )
                        .exclusions(
                                "- Trò chơi thể thao biển tự túc.\n" +
                                "- Lặn biển bình dưỡng khí.\n" +
                                "- Chi phí cá nhân."
                        )
                        .active(true)
                        .sortOrder(2)
                        .build();
                TourPackage savedPkg2 = tourPackageRepository.save(pkg2);
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg2).locale("vi").name("Hòn Tằm – Tắm Bùn")
                        .highlights(pkg2.getHighlights()).inclusions(pkg2.getInclusions()).exclusions(pkg2.getExclusions())
                        .childPolicyNote("Trẻ em 1m–1m29: 700.000đ").infantPolicyNote("Dưới 1m: Miễn phí").build());
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg2).locale("en").name("Hon Tam – Mud Bath Package")
                        .highlights("- 3 Islands full tour program\n- Hon Tam hot mineral mud bath ticket\n- Premium lunch\n- Child 1m–1m29: 700.000 VND")
                        .inclusions("- All 3 Islands tour services.\n- Hon Tam natural mud bath.\n- Infinity pool access, towels and swimwear.\n- Seafood lunch.")
                        .exclusions("- Sea sports & scuba diving.\n- Personal expenses.")
                        .childPolicyNote("Children 1m–1m29: 700,000 VND").infantPolicyNote("Under 1m: Free").build());

                // Gói 3: Cano riêng – Linh hoạt
                TourPackage pkg3 = TourPackage.builder()
                        .tour(tour)
                        .name("Cano riêng – Linh hoạt")
                        .isContactOnly(true)
                        .prices(new ArrayList<>())
                        .highlights(
                                "- Cano riêng cao tốc đời mới\n" +
                                "- Phục vụ riêng theo đoàn\n" +
                                "- Lịch trình trao đổi theo nhu cầu thực tế\n" +
                                "- Phù hợp gia đình, nhóm và đoàn khách"
                        )
                        .inclusions(
                                "- Cano cao tốc riêng cho đoàn.\n" +
                                "- Tài xế cano và hướng dẫn viên riêng.\n" +
                                "- Lịch trình linh hoạt theo yêu cầu đoàn khách."
                        )
                        .exclusions(
                                "- Vé các điểm tham quan theo lựa chọn thực tế.\n" +
                                "- Ăn uống và trò chơi biển tự chọn."
                        )
                        .active(true)
                        .sortOrder(3)
                        .build();
                TourPackage savedPkg3 = tourPackageRepository.save(pkg3);
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg3).locale("vi").name("Cano riêng – Linh hoạt")
                        .highlights(pkg3.getHighlights()).inclusions(pkg3.getInclusions()).exclusions(pkg3.getExclusions()).build());
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg3).locale("en").name("Private Speedboat – Flexible")
                        .highlights("- Private high-speed boat\n- Exclusive service for your group\n- Flexible custom itinerary\n- Ideal for families and groups")
                        .inclusions("- Private speedboat charter.\n- Private skipper and guide.\n- Flexible timing and custom stops.")
                        .exclusions("- Optional attraction tickets.\n- Meals and water sports.")
                        .build());
            } else {
                tour.setInclusions(
                        "- Du thuyền 5 sao sang trọng tham quan vịnh Nha Trang.\n" +
                        "- Tiệc cocktail ngắm hoàng hôn lãng mạn.\n" +
                        "- Bữa tối buffet hải sản quốc tế thượng hạng.\n" +
                        "- Nhạc sống acoustic du dương suốt hành trình."
                );
                tour.setExclusions(
                        "- Đồ uống có cồn cao cấp ngoài thực đơn.\n" +
                        "- Tiền tip và chi phí cá nhân."
                );
                tourRepository.save(tour);

                List<PriceListPerPerson> pCruisePrices = new ArrayList<>(List.of(
                        PriceListPerPerson.builder().passengerType("ADULT").pricePerPerson(new BigDecimal("1200000")).minPax(1).maxPax(20).active(true).build(),
                        PriceListPerPerson.builder().passengerType("CHILD").pricePerPerson(new BigDecimal("850000")).minPax(1).maxPax(20).active(true).build(),
                        PriceListPerPerson.builder().passengerType("INFANT").pricePerPerson(BigDecimal.ZERO).minPax(1).maxPax(20).active(true).build()
                ));
                TourPackage pkg1 = TourPackage.builder()
                        .tour(tour)
                        .name("Tiêu Chuẩn Du Thuyền Hoàng Hôn")
                        .prices(pCruisePrices)
                        .childPolicyNote("Trẻ em 1m–1m29: 850.000đ")
                        .infantPolicyNote("Dưới 1m: Miễn phí")
                        .highlights("- Du thuyền ngắm hoàng hôn\n- Tiệc cocktail & Buffet hải sản\n- Nhạc Acoustic live")
                        .inclusions("- Vé lên du thuyền 5 sao.\n- Bữa tối buffet cao cấp.\n- Live music & cocktail.")
                        .exclusions("- Chi phí cá nhân ngoài thực đơn.")
                        .active(true)
                        .sortOrder(1)
                        .build();
                TourPackage savedPkg1 = tourPackageRepository.save(pkg1);
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg1).locale("vi").name("Tiêu Chuẩn Du Thuyền Hoàng Hôn")
                        .highlights(pkg1.getHighlights()).inclusions(pkg1.getInclusions()).exclusions(pkg1.getExclusions()).build());
                tourPackageTranslationRepository.save(TourPackageTranslation.builder()
                        .tourPackage(savedPkg1).locale("en").name("Sunset Cruise Standard")
                        .highlights("- 5-star sunset cruise\n- Cocktail party & Seafood buffet\n- Live Acoustic music")
                        .inclusions("- 5-star cruise ticket.\n- Premium buffet dinner.\n- Live music & cocktail.")
                        .exclusions("- Personal expenses.")
                        .build());
            }
        }
        log.info("Finished seeding tour packages!");
    }
}

