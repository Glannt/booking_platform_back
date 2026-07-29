package com.dotnt.booking_travel_backend.sections.internal.config;

import com.dotnt.booking_travel_backend.sections.internal.entity.Section;
import com.dotnt.booking_travel_backend.sections.internal.entity.SectionTranslation;
import com.dotnt.booking_travel_backend.sections.internal.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class SectionDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final SectionRepository sectionRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        new Thread(() -> {
            try {
                seedHomepageHeroBanner();
                seedToursHeroBanner();
                seedSelfDriveHeroBanner();
                seedAirportTransferHeroBanner();
                seedRentCarHeroBanner();
                seedSlideCard();
                seedSlideReview();
                seedInspiration();
                seedBookingProcess();
                seedSelfDriveBookingProcess();
                seedRentCarRoutesHeroBanner();
                log.info("Section initialization complete.");
            } catch (Exception e) {
                log.error("Failed to initialize sections", e);
            }
        }).start();
    }

    @Transactional
    public void seedHomepageHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/", "HERO_BANNER")) {
            log.info("HERO_BANNER for / already exists, skipping.");
            return;
        }
        sectionRepository.save(buildHomepageHeroBanner());
        log.info("Seeded HERO_BANNER for /.");
    }

    @Transactional
    public void seedToursHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/tours", "HERO_BANNER")) {
            log.info("HERO_BANNER for /tours already exists, skipping.");
            return;
        }
        sectionRepository.save(buildToursHeroBanner());
        log.info("Seeded HERO_BANNER for /tours.");
    }

    @Transactional
    public void seedSelfDriveHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/self-drive", "HERO_BANNER")) {
            log.info("HERO_BANNER for /self-drive already exists, skipping.");
            return;
        }
        sectionRepository.save(buildSelfDriveHeroBanner());
        log.info("Seeded HERO_BANNER for /self-drive.");
    }

    @Transactional
    public void seedAirportTransferHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/airport-transfer", "HERO_BANNER")) {
            log.info("HERO_BANNER for /airport-transfer already exists, skipping.");
            return;
        }
        sectionRepository.save(buildAirportTransferHeroBanner());
        log.info("Seeded HERO_BANNER for /airport-transfer.");
    }

    @Transactional
    public void seedRentCarHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/rent-car", "HERO_BANNER")) {
            log.info("HERO_BANNER for /rent-car already exists, skipping.");
            return;
        }
        sectionRepository.save(buildRentCarHeroBanner());
        log.info("Seeded HERO_BANNER for /rent-car.");
    }

    @Transactional
    public void seedSlideCard() {
        if (sectionRepository.existsBySectionType("SLIDE_CARD")) {
            log.info("SLIDE_CARD sections already exist, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Slide Card - Homepage")
                .sectionType("SLIDE_CARD")
                .pagePath("/")
                .orderIndex(5)
                .active(true)
                .contentData("{\"badgeTitle\":{\"vi\":\"Đề xuất\",\"en\":\"Featured\"},\"cards\":[{\"title\":{\"vi\":\"Sân bay Cam Ranh đi Nha Trang\",\"en\":\"Cam Ranh Airport to Nha Trang\"},\"description\":{\"vi\":\"Trung chuyển nhanh chóng, dòng xe hiện đại, hủy chuyến miễn phí.\",\"en\":\"Fast & reliable transfer, modern sedan, free cancellation.\"},\"badge\":{\"vi\":\"Trung chuyển sân bay\",\"en\":\"Airport Transfer\"},\"price\":{\"vi\":\"350,000đ\",\"en\":\"$15\"},\"imageUrl\":\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600\",\"linkUrl\":\"/airport-transfer\"},{\"title\":{\"vi\":\"Đà Nẵng đi Phố cổ Hội An\",\"en\":\"Da Nang to Hoi An Ancient Town\"},\"description\":{\"vi\":\"Tuyến đường đẹp, tài xế thân thiện, dừng chân ngắm cảnh tùy chọn.\",\"en\":\"Scenic route transfer, English-speaking driver, scenic stops.\"},\"badge\":{\"vi\":\"Thuê xe liên tỉnh\",\"en\":\"Inter-provincial\"},\"price\":{\"vi\":\"450,000đ\",\"en\":\"$19\"},\"imageUrl\":\"https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=600\",\"linkUrl\":\"/rent-car\"},{\"title\":{\"vi\":\"Sân bay Nội Bài đi Trung tâm Hà Nội\",\"en\":\"Noi Bai Airport to Hanoi Center\"},\"description\":{\"vi\":\"Xe riêng đi thẳng, đã bao gồm phí cầu đường, không phí ẩn.\",\"en\":\"Direct private car service, meet & greet included, no hidden fees.\"},\"badge\":{\"vi\":\"Trung chuyển sân bay\",\"en\":\"Airport Transfer\"},\"price\":{\"vi\":\"280,000đ\",\"en\":\"$12\"},\"imageUrl\":\"https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?q=80&w=600\",\"linkUrl\":\"/airport-transfer\"},{\"title\":{\"vi\":\"Sân bay Phú Quốc đi Dương Đông\",\"en\":\"Phu Quoc Airport to Duong Dong\"},\"description\":{\"vi\":\"Đón ngay tại sảnh ra, xe đời mới máy lạnh êm ái.\",\"en\":\"Instant pickup at terminal, air-conditioned comfortable ride.\"},\"badge\":{\"vi\":\"Trung chuyển sân bay\",\"en\":\"Airport Transfer\"},\"price\":{\"vi\":\"200,000đ\",\"en\":\"$9\"},\"imageUrl\":\"https://images.unsplash.com/photo-1506929562872-bb421503ef21?q=80&w=600\",\"linkUrl\":\"/airport-transfer\"},{\"title\":{\"vi\":\"Tân Sơn Nhất đi Biển Vũng Tàu\",\"en\":\"Tan Son Nhat to Vung Tau Beach\"},\"description\":{\"vi\":\"Dòng xe Limousine rộng rãi, trang bị nước uống và Wi-Fi miễn phí.\",\"en\":\"Limousine private car service, spacious seats, water & Wi-Fi.\"},\"badge\":{\"vi\":\"Thuê xe liên tỉnh\",\"en\":\"Inter-provincial\"},\"price\":{\"vi\":\"1,200,000đ\",\"en\":\"$50\"},\"imageUrl\":\"https://images.unsplash.com/photo-1449965408869-ebd3fee46565?q=80&w=600\",\"linkUrl\":\"/rent-car\"}]}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Chuyến đi phổ biến")
                .subtitle("Khám phá các tuyến đường được yêu thích nhất")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Popular Transfers & Tours")
                .subtitle("Discover the most loved routes and experiences")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("인기 이송 및 투어")
                .subtitle("가장 사랑받는 노선과 경험을 발견하세요")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Популярные трансферы и туры")
                .subtitle("Откройте самые популярные маршруты и впечатления")
                .build());

        sectionRepository.save(section);
        log.info("Seeded SLIDE_CARD section.");
    }

    @Transactional
    public void seedSlideReview() {
        if (sectionRepository.existsBySectionType("SLIDE_REVIEW")) {
            log.info("SLIDE_REVIEW sections already exist, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Slide Review - Homepage")
                .sectionType("SLIDE_REVIEW")
                .pagePath("/")
                .orderIndex(6)
                .active(true)
                .contentData("{\"ratingSummary\":\"4.9/5 (1,200+ reviews)\",\"reviews\":[{\"title\":{\"vi\":\"Tuyệt vời ngoài mong đợi!\",\"en\":\"Excellent service!\",\"ko\":\"기대 이상의 서비스!\",\"ru\":\"Отличный сервис!\"},\"description\":{\"vi\":\"Tài xế đón tôi đúng giờ tại sân bay với bảng tên rõ ràng. Xe mới sạch sẽ, lái xe rất an toàn.\",\"en\":\"The driver was waiting for us with a name sign. Very professional, neat car, and very safe driver.\",\"ko\":\"기사가 공항에서 제 이름이 적힌 피켓을 들고 제시간에 대기했습니다. 차가 깨끗하고 안전하게 운전해 주셨습니다.\",\"ru\":\"Водитель ждал меня в аэропорту с табличкой. Машина чистая, вождение безопасное.\"},\"rating\":5,\"author\":\"Minh Khoa\",\"avatarBg\":\"bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400\",\"avatarText\":\"MK\",\"route\":\"Cam Ranh ➔ Nha Trang\"},{\"title\":{\"vi\":\"Rất chuyên nghiệp\",\"en\":\"Very professional\",\"ko\":\"매우 전문적입니다\",\"ru\":\"Очень профессионально\"},\"description\":{\"vi\":\"Đặt xe trực tuyến cực kỳ đơn giản. Nhân viên hỗ trợ nhiệt tình khi chuyến bay của tôi bị hoãn 2 tiếng.\",\"en\":\"Booking online was super easy. Customer support was helpful when my flight was delayed by 2 hours.\",\"ko\":\"온라인 예약이 아주 간편했습니다. 비행기가 2시간 지연되었을 때 고객 지원팀이 친절하게 도와주었습니다.\",\"ru\":\"Бронирование онлайн очень простое. Поддержка помогла, когда мой рейс задержали на 2 часа.\"},\"rating\":5,\"author\":\"Elena S.\",\"avatarBg\":\"bg-emerald-100 text-emerald-600 dark:bg-emerald-900/40 dark:text-emerald-400\",\"avatarText\":\"ES\",\"route\":\"Đà Nẵng ➔ Hội An\"},{\"title\":{\"vi\":\"Sẽ tiếp tục ủng hộ\",\"en\":\"Highly recommended!\",\"ko\":\"적극 추천합니다!\",\"ru\":\"Рекомендую всем!\"},\"description\":{\"vi\":\"Dịch vụ tuyệt vời, xe Limousine rộng rãi thoải mái. Giá cả minh bạch không phát sinh thêm chi phí.\",\"en\":\"Excellent limousine service, spacious and cozy. Transparent pricing, no hidden costs.\",\"ko\":\"훌륭한 리무진 서비스, 넓고 아늑합니다. 요금이 투명하고 추가 요금이 없습니다.\",\"ru\":\"Отличный сервис лимузинов, просторно и уютно. Прозрачные цены без скрытых комиссий.\"},\"rating\":5,\"author\":\"David L.\",\"avatarBg\":\"bg-purple-100 text-purple-600 dark:bg-purple-900/40 dark:text-purple-400\",\"avatarText\":\"DL\",\"route\":\"Hà Nội ➔ Nội Bài\"},{\"title\":{\"vi\":\"Xe sạch sẽ, đi êm ái\",\"en\":\"Clean and smooth ride\",\"ko\":\"차량이 청결하고 안락함\",\"ru\":\"Чисто и плавно\"},\"description\":{\"vi\":\"Xe rất sạch sẽ và thơm, tài xế lịch sự chạy xe cẩn thận. Giá cả phải chăng.\",\"en\":\"The car was clean and smelled nice. The driver was polite and drove carefully. Affordable price.\",\"ko\":\"차가 깨끗하고 좋은 향기가 났습니다. 기사가 정중하고 조심히 운전했습니다. 가격도 합리적입니다.\",\"ru\":\"Машина была чистой, водитель вежлив и вел аккуратно. Приемлемая цена.\"},\"rating\":5,\"author\":\"Park M.\",\"avatarBg\":\"bg-amber-100 text-amber-600 dark:bg-amber-900/40 dark:text-amber-400\",\"avatarText\":\"PM\",\"route\":\"Sài Gòn ➔ Vũng Tàu\"},{\"title\":{\"vi\":\"Hỗ trợ khách hàng tuyệt vời\",\"en\":\"Fantastic support\",\"ko\":\"최고의 고객 지원\",\"ru\":\"Отличная поддержка\"},\"description\":{\"vi\":\"Tôi cần thay đổi giờ đón phút chót và được tổng đài hỗ trợ đổi xe chỉ trong 10 phút. Rất hài lòng!\",\"en\":\"I needed to change my pick-up time last minute, and support re-arranged it in 10 minutes. Extremely satisfied!\",\"ko\":\"마지막 순간에 시간을 변경해야 했는데 지원팀에서 10분 만에 차량을 재배정해 주었습니다. 대만족!\",\"ru\":\"Мне нужно было изменить время встречи в последний момент, поддержка решила все за 10 минут. Очень доволен!\"},\"rating\":5,\"author\":\"Ivan T.\",\"avatarBg\":\"bg-rose-100 text-rose-600 dark:bg-rose-900/40 dark:text-rose-400\",\"avatarText\":\"IT\",\"route\":\"Nha Trang ➔ Đà Lạt\"}]}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Khách hàng yêu thích")
                .subtitle("Hơn 1,200 đánh giá 5 sao từ khách hàng thật")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Loved by Travelers")
                .subtitle("Over 1,200 five-star reviews from real customers")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("여행자들의 사랑")
                .subtitle("실제 고객 리뷰 1,200건 이상의 5점 만점 평가")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Любимо путешественниками")
                .subtitle("Более 1200 пятёздочных отзывов от реальных клиентов")
                .build());

        sectionRepository.save(section);
        log.info("Seeded SLIDE_REVIEW section.");
    }

    @Transactional
    public void seedInspiration() {
        if (sectionRepository.existsBySectionType("INSPIRATION")) {
            log.info("INSPIRATION sections already exist, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Travel Inspiration - Homepage")
                .sectionType("INSPIRATION")
                .pagePath("/")
                .orderIndex(7)
                .active(true)
                .contentData("{\"items\":[{\"imageUrl\":\"https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=600\",\"label\":{\"vi\":\"Kinh nghiệm du lịch Tây Bắc\",\"en\":\"Northwest Roadtrip Tips\"}},{\"imageUrl\":\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600\",\"label\":{\"vi\":\"Cẩm nang biển Nha Trang\",\"en\":\"Nha Trang Coastal Guide\"}},{\"imageUrl\":\"https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=600\",\"label\":{\"vi\":\"Hành trình xe cao cấp\",\"en\":\"Premium Fleets & Travel\"}}],\"showInstagram\":true,\"instagramHandle\":\"@minhkhoatravel_vn\"}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Truyền cảm hứng du lịch")
                .subtitle("Khám phá các bài viết và mẹo du lịch hữu ích")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Travel Inspiration")
                .subtitle("Discover useful travel guides and tips")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("여행 영감")
                .subtitle("유용한 여행 가이드와 팁을 발견하세요")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Вдохновение для путешествий")
                .subtitle("Откройте полезные путеводители и советы")
                .build());

        sectionRepository.save(section);
        log.info("Seeded INSPIRATION section.");
    }

    private Section buildHomepageHeroBanner() {
        Section section = Section.builder()
                .sectionName("Hero Banner - Homepage")
                .sectionType("HERO_BANNER")
                .pagePath("/")
                .orderIndex(0)
                .active(true)
                .bgColor("#1a1a2e")
                .textColor("#ffffff")
                .cssClass("hero-home")
                .images(null)
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Khám Phá Nha Trang")
                .subtitle("Minh Khoa Travel")
                .description("Đưa đón sân bay, thuê xe, du lịch — Trải nghiệm Nha Trang trọn vẹn với dịch vụ uy tín và giá tốt nhất.")
                .btnText("Khám phá ngay")
                .btnLink("/tours")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Discover Nha Trang")
                .subtitle("Minh Khoa Travel")
                .description("Airport transfers, car rentals, tours — Experience Nha Trang with trusted service and the best prices.")
                .btnText("Explore now")
                .btnLink("/en/tours")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("나트랑을 발견하세요")
                .subtitle("민 코아 트래블")
                .description("공항 이동, 차량 렌트, 투어 — 최고의 서비스와 가격으로 나트랑을 경험하세요.")
                .btnText("지금 알아보기")
                .btnLink("/ko/tours")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Откройте Нячанг")
                .subtitle("Минь Коа Трэвел")
                .description("Трансферы из аэропорта, аренда автомобилей, туры — испытайте Нячанг с надёжным сервисом и лучшими ценами.")
                .btnText("Узнать сейчас")
                .btnLink("/ru/tours")
                .build());

        return section;
    }

    private Section buildToursHeroBanner() {
        Section section = Section.builder()
                .sectionName("Hero Banner - Tours")
                .sectionType("HERO_BANNER")
                .pagePath("/tours")
                .orderIndex(0)
                .active(true)
                .bgColor("#111827")
                .textColor("#ffffff")
                .cssClass("hero-tours")
                .images(null)
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Tour Nha Trang")
                .subtitle("Du lịch trải nghiệm")
                .description("Khám phá những điểm đến tuyệt vời nhất tại Nha Trang với các tour chất lượng và giá tốt nhất.")
                .btnText("Liên hệ tư vấn")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Nha Trang Tours")
                .subtitle("Travel experiences")
                .description("Discover the best destinations in Nha Trang with quality tours at the best prices.")
                .btnText("Contact us")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("나트랑 투어")
                .subtitle("여행 경험")
                .description("최고의 투어로 나트랑의 멋진 명소를 발견하세요.")
                .btnText("문의하기")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Туры по Нячангу")
                .subtitle("Путешествия")
                .description("Откройте лучшие места Нячанга с качественными турами по лучшим ценам.")
                .btnText("Связаться с нами")
                .btnLink("tel:0764971971")
                .build());

        return section;
    }

    private Section buildSelfDriveHeroBanner() {
        Section section = Section.builder()
                .sectionName("Hero Banner - Self Drive")
                .sectionType("HERO_BANNER")
                .pagePath("/self-drive")
                .orderIndex(0)
                .active(true)
                .bgColor("#1a1a2e")
                .textColor("#ffffff")
                .cssClass("hero-self-drive")
                .images(null)
                .contentData("{\"badges\":[{\"text\":{\"vi\":\"Xe 5 chỗ từ 500k/ngày\",\"en\":\"5-seat from 500k/day\"},\"textColor\":\"#ffffff\",\"bgColor\":\"#3b82f6\"},{\"text\":{\"vi\":\"Xe 7 chỗ từ 700k/ngày\",\"en\":\"7-seat from 700k/day\"},\"textColor\":\"#ffffff\",\"bgColor\":\"#22c55e\"}]}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Thuê Xe Tự Lái Nha Trang")
                .subtitle("Minh Khoa Travel")
                .description("Xe đời mới — Thủ tục nhanh — Hợp đồng rõ ràng — Hỗ trợ 24/7. Giao/nhận xe tại trung tâm, khách sạn, bến xe hoặc sân bay Cam Ranh.")
                .btnText("Liên hệ ngay")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Self-Drive Car Rental Nha Trang")
                .subtitle("Minh Khoa Travel")
                .description("Modern cars – Quick procedure – Transparent contract – 24/7 support. Pickup/delivery at central, hotels, stations, or Cam Ranh Airport.")
                .btnText("Contact now")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("나트랑 셀프 드라이브 렌트카")
                .subtitle("민 코아 트래블")
                .description("최신 차량 – 빠른 절차 – 투명한 계약 – 24/7 지원. 중앙, 호텔, 역, 깜란 공항에서 픽업/딜리버리 가능.")
                .btnText("문의하기")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Аренда авто в Нячанге")
                .subtitle("Минь Коа Трэвел")
                .description("Современные авто – Быстрые процедуры – Прозрачный договор – Поддержка 24/7. Забор/доставка в центре, отелях, вокзалах или аэропорту Камрань.")
                .btnText("Связаться")
                .btnLink("tel:0764971971")
                .build());

        return section;
    }

    private Section buildAirportTransferHeroBanner() {
        Section section = Section.builder()
                .sectionName("Hero Banner - Airport Transfer")
                .sectionType("HERO_BANNER")
                .pagePath("/airport-transfer")
                .orderIndex(0)
                .active(true)
                .bgColor("#0f172a")
                .textColor("#ffffff")
                .cssClass("hero-airport-transfer")
                .images(null)
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Đưa Đón Sân Bay Cam Ranh")
                .subtitle("Minh Khoa Travel")
                .description("Dịch vụ đưa đón sân bay Cam Ranh — Nhanh chóng, an toàn, đúng giờ. Đội ngũ tài xế chuyên nghiệp, xe đời mới.")
                .btnText("Hotline: 0764 971 971")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Cam Ranh Airport Transfer")
                .subtitle("Minh Khoa Travel")
                .description("Cam Ranh airport transfer service — Fast, safe, on time. Professional drivers, modern vehicles.")
                .btnText("Hotline: 0764 971 971")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("깜란 공항 픽업/딜리버리")
                .subtitle("민 코아 트래블")
                .description("깜란 공항 픽업 서비스 — 빠르고 안전하고 정시. 전문 기사, 최신 차량.")
                .btnText("전화: 0764 971 971")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Трансфер из аэропорта Камрань")
                .subtitle("Минь Коа Трэвел")
                .description("Трансфер из аэропорта Камрань — Быстро, безопасно, вовремя. Профессиональные водители, современные автомобили.")
                .btnText("Тел: 0764 971 971")
                .btnLink("tel:0764971971")
                .build());

        return section;
    }

    private Section buildRentCarHeroBanner() {
        Section section = Section.builder()
                .sectionName("Hero Banner - Rent Car with Driver")
                .sectionType("HERO_BANNER")
                .pagePath("/rent-car")
                .orderIndex(0)
                .active(true)
                .bgColor("#1a1a2e")
                .textColor("#ffffff")
                .cssClass("hero-rent-car")
                .images(null)
                .contentData("{\"badges\":[{\"text\":\"Minh Khoa Travel\",\"textColor\":\"#C9A227\",\"bgColor\":\"rgba(255,255,255,0.1)\",\"iconUrl\":\"\"}]}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Thuê Xe Có Tài Xế Đi Tỉnh")
                .subtitle("Minh Khoa Travel")
                .description("Xe riêng 4–45 chỗ – Đưa đón tận nơi – Giá tốt – Tài xế chuyên tuyến. Trải nghiệm du lịch Nha Trang tiện lợi và an toàn.")
                .btnText("Đặt xe ngay")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Private Car Rental with Driver for Intercity Trips")
                .subtitle("Minh Khoa Travel")
                .description("Private cars 4–45 seats – Door-to-door transfer – Best prices – Professional drivers. Convenient and safe Nha Trang travel.")
                .btnText("Book now")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("시외 이동 기사 포함 차량 렌트")
                .subtitle("민 코아 트래블")
                .description("프라이빗 차량 4–45석 – 문까지 배송 – 최저가 – 전문 기사. 편안하고 안전한 나트랑 여행.")
                .btnText("지금 예약")
                .btnLink("tel:0764971971")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Аренда автомобиля с водителем для междугородних поездок")
                .subtitle("Минь Коа Трэвел")
                .description("Частные автомобили 4–45 мест – Доставка от двери до двери – Лучшие цены – Профессиональные водители.")
                .btnText("Забронировать")
                .btnLink("tel:0764971971")
                .build());

        return section;
    }

    @Transactional
    public void seedBookingProcess() {
        if (sectionRepository.existsBySectionType("BOOKING_PROCESS")) {
            log.info("BOOKING_PROCESS sections already exist, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Booking Process - Rent Car")
                .sectionType("BOOKING_PROCESS")
                .pagePath("/rent-car")
                .orderIndex(2)
                .active(true)
                .contentData("{\"steps\":[{\"step\":1,\"icon\":\"Phone\",\"title\":{\"vi\":\"Liên hệ & báo giá chính xác\",\"en\":\"Contact & Exact Quote\"},\"description\":{\"vi\":\"Liên hệ Hotline/Zalo: 0764971971 để báo giá theo ngày giờ, loại xe và điểm đón – trả. Có hỗ trợ định tuyến sân bay Cam Ranh.\",\"en\":\"Contact Hotline/Zalo: 0764971971 for exact quote based on date, time, vehicle type and pickup/dropoff points. Cam Ranh airport routing supported.\"}},{\"step\":2,\"icon\":\"CreditCard\",\"title\":{\"vi\":\"Xác nhận & đặt cọc 20%\",\"en\":\"Confirm & 20% Deposit\"},\"description\":{\"vi\":\"Xe riêng: cọc khi chốt lịch. Dcar: cọc sau khi bộ phận điều hành công ty xác nhận và cấp số ghế.\",\"en\":\"Private car: deposit upon schedule confirmation. Dcar: deposit after operations team confirms and assigns seat number.\"}},{\"step\":3,\"icon\":\"Car\",\"title\":{\"vi\":\"Gửi thông tin xe & tài xế\",\"en\":\"Send Vehicle & Driver Info\"},\"description\":{\"vi\":\"Xe riêng: gửi thông tin trước 30 phút. Dcar: tài xế liên hệ trước 30 phút.\",\"en\":\"Private car: info sent 30 minutes before. Dcar: driver contacts you 30 minutes before departure.\"}},{\"step\":4,\"icon\":\"Wallet\",\"title\":{\"vi\":\"Thanh toán\",\"en\":\"Payment\"},\"description\":{\"vi\":\"Thanh toán tiền mặt hoặc chuyển khoản khi đến nơi. Hỗ trợ hóa đơn VAT 8% khi yêu cầu.\",\"en\":\"Pay cash or bank transfer upon arrival. VAT 8% invoice available upon request.\"}},{\"step\":5,\"icon\":\"Handshake\",\"title\":{\"vi\":\"Hỗ trợ sau chuyến\",\"en\":\"Post-Trip Support\"},\"description\":{\"vi\":\"Sẵn sàng tiếp nhận phản hồi & cung cấp chứng từ khi cần.\",\"en\":\"Ready to receive feedback & provide documentation when needed.\"}}],\"notes\":{\"title\":{\"vi\":\"Lưu ý (áp dụng cho Dcar Limousine)\",\"en\":\"Notes (applies to Dcar Limousine)\"},\"items\":[{\"vi\":\"Vui lòng sử dụng đúng số ghế đã được cấp.\",\"en\":\"Please use the assigned seat number.\"},{\"vi\":\"Phí trung chuyển khách sạn/sân bay (nếu có) sẽ được thông báo trước.\",\"en\":\"Hotel/airport shuttle fee (if any) will be notified in advance.\"},{\"vi\":\"Giờ khởi hành cố định:\",\"en\":\"Fixed departure times:\"}],\"schedules\":[{\"direction\":{\"vi\":\"Nha Trang → Đà Lạt\",\"en\":\"Nha Trang → Da Lat\"},\"times\":[\"08:00\",\"11:00\",\"13:00\",\"17:00\"]},{\"direction\":{\"vi\":\"Đà Lạt → Nha Trang\",\"en\":\"Da Lat → Nha Trang\"},\"times\":[\"08:00\",\"13:00\",\"16:00\",\"17:00\"]}]}}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Quy trình đặt xe")
                .subtitle("Minh Khoa Travel — Đơn giản, nhanh chóng, minh bạch")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Booking Process")
                .subtitle("Minh Khoa Travel — Simple, fast, transparent")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("예약 절차")
                .subtitle("민 코아 트래블 — 간단하고 빠르고 투명하게")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Процесс бронирования")
                .subtitle("Минь Коа Трэвел — Просто, быстро, прозрачно")
                .build());

        sectionRepository.save(section);
        log.info("Seeded BOOKING_PROCESS section.");
    }

    @Transactional
    public void seedSelfDriveBookingProcess() {
        if (sectionRepository.existsByPagePathAndSectionType("/self-drive", "BOOKING_PROCESS")) {
            log.info("BOOKING_PROCESS for /self-drive already exists, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Booking Process - Self Drive")
                .sectionType("BOOKING_PROCESS")
                .pagePath("/self-drive")
                .orderIndex(2)
                .active(true)
                .contentData("{\"steps\":[{\"step\":1,\"icon\":\"Car\",\"title\":{\"vi\":\"Chọn xe & ngày\",\"en\":\"Choose car & date\"},\"description\":{\"vi\":\"Chọn phân khúc xe phù hợp, chọn ngày thuê và xem giá.\",\"en\":\"Choose suitable vehicle category, select rental dates and view pricing.\"}},{\"step\":2,\"icon\":\"FileText\",\"title\":{\"vi\":\"Gửi giấy tờ\",\"en\":\"Submit documents\"},\"description\":{\"vi\":\"Gửi CCCD và bằng lái xe. Hỗ trợ gửi trực tuyến.\",\"en\":\"Submit ID card and driver's license. Online submission supported.\"}},{\"step\":3,\"icon\":\"CreditCard\",\"title\":{\"vi\":\"Đặt cọc giữ xe\",\"en\":\"Deposit to secure\"},\"description\":{\"vi\":\"Đặt cọc theo quy định để giữ xe. Xác nhận qua Zalo/Hotline.\",\"en\":\"Pay deposit as required to secure vehicle. Confirmation via Zalo/Hotline.\"}},{\"step\":4,\"icon\":\"MapPin\",\"title\":{\"vi\":\"Nhận xe & tận hưởng\",\"en\":\"Receive car & enjoy\"},\"description\":{\"vi\":\"Nhận xe tại trung tâm, khách sạn hoặc sân bay. Kiểm tra xe trước khi nhận.\",\"en\":\"Receive car at center, hotel or airport. Inspect vehicle before pickup.\"}}]}")
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Quy trình thuê xe tự lái")
                .subtitle("Minh Khoa Travel — Đơn giản, nhanh chóng, an toàn")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("Self-Drive Rental Process")
                .subtitle("Minh Khoa Travel — Simple, fast, safe")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("셀프 드라이브 렌트 절차")
                .subtitle("민 코아 트래블 — 간단하고 빠르고 안전하게")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Процесс аренды авто")
                .subtitle("Минь Коа Трэвел — Просто, быстро, безопасно")
                .build());

        sectionRepository.save(section);
        log.info("Seeded BOOKING_PROCESS for /self-drive.");
    }

    @Transactional
    public void seedRentCarRoutesHeroBanner() {
        if (sectionRepository.existsByPagePathAndSectionType("/rent-car/routes", "HERO_BANNER")) {
            log.info("HERO_BANNER for /rent-car/routes already exists, skipping.");
            return;
        }

        Section section = Section.builder()
                .sectionName("Hero Banner - Rent Car Routes")
                .sectionType("HERO_BANNER")
                .pagePath("/rent-car/routes")
                .orderIndex(0)
                .active(true)
                .bgColor("#0f172a")
                .textColor("#ffffff")
                .cssClass("hero-rent-car-routes")
                .images(null)
                .translations(new ArrayList<>())
                .build();

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("vi")
                .title("Tất cả tuyến xe đi tỉnh")
                .subtitle("Minh Khoa Travel")
                .description("Danh sách đầy đủ các tuyến xe đi tỉnh từ Nha Trang. Xem giá, giờ khởi hành và loại xe.")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("en")
                .title("All Intercity Routes")
                .subtitle("Minh Khoa Travel")
                .description("Complete list of intercity routes from Nha Trang. View prices, departure times and vehicle types.")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ko")
                .title("모든 시외 노선")
                .subtitle("민 코아 트래블")
                .description("나트랑에서 출발하는 모든 시외 노선 목록. 가격, 출발 시간, 차종을 확인하세요.")
                .build());

        section.getTranslations().add(SectionTranslation.builder()
                .section(section).locale("ru")
                .title("Все межгородние маршруты")
                .subtitle("Минь Коа Трэвел")
                .description("Полный список межгородних маршрутов из Нячанга. Смотрите цены, время отправления и типы автомобилей.")
                .build());

        sectionRepository.save(section);
        log.info("Seeded HERO_BANNER for /rent-car/routes.");
    }
}
