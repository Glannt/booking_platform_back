package com.dotnt.booking_travel_backend.tour.internal.config;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicy;
import com.dotnt.booking_travel_backend.tour.internal.entity.TourPolicyTranslation;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPolicyRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourPolicyTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(26)
public class TourPolicyDataInitializer implements CommandLineRunner {

    private final TourPolicyRepository policyRepository;
    private final TourPolicyTranslationRepository translationRepository;

    private static final String SVG_REFUND = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8\"/><path d=\"M3 3v5h5\"/><path d=\"M12 7v5l4 2\"/></svg>";
    private static final String SVG_CHILD = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m8 3 4 8 5-5 5 15H2L8 3z\"/></svg>";
    private static final String SVG_ID_CARD = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><rect width=\"18\" height=\"14\" x=\"3\" y=\"5\" rx=\"2\"/><path d=\"M7 15h4M15 15h2M7 11h2\"/><circle cx=\"15\" cy=\"10\" r=\"1\"/></svg>";
    private static final String SVG_WEATHER = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z\"/></svg>";

    @Override
    public void run(String... args) {
        if (policyRepository.count() > 0) return;

        log.info("Seeding default tour policies...");

        seedPolicy("cancellation-policy", SVG_REFUND, 1,
                "Chính sách hoàn hủy", "Miễn phí hủy trước 48 giờ khởi hành. Hủy trong vòng 24 - 48 giờ tính phí 50%, hủy dưới 24 giờ tính phí 100%.",
                "Cancellation Policy", "Free cancellation up to 48 hours before departure. 50% charge for 24-48 hours, 100% within 24 hours.",
                "취소 및 환불 규정", "출발 48시간 전 무료 취소. 24~48시간 전 취소 시 50% 수수료, 24시간 이내 취소 시 100% 수수료.",
                "Правила отмены", "Бесплатная отмена за 48 часов до выезда. 50% сбор за 24-48 часов, 100% менее 24 часов.");

        seedPolicy("child-policy", SVG_CHILD, 2,
                "Chính sách trẻ em", "Trẻ em dưới 1m hoặc dưới 5 tuổi miễn phí (ngồi chung với bố mẹ). Trẻ em từ 1m - 1m29 tính giá trẻ em.",
                "Child Policy", "Children under 1m or under 5 years old are free (sharing seat with parents). Children 1m - 1m29 standard child rate.",
                "어린이 규정", "키 1m 미만 또는 만 5세 미만 무료 (부모와 좌석 공유). 키 1m~1.29m 아동 요금 적용.",
                "Детская политика", "Дети до 1 м или до 5 лет бесплатно (на коленях у родителей). Дети 1-1.29 м по детскому тарифу.");

        seedPolicy("id-requirements", SVG_ID_CARD, 3,
                "Giấy tờ tùy thân", "Quý khách vui lòng mang theo CCCD/Hộ chiếu bản gốc còn hạn để làm thủ tục tham quan hoặc lưu trú.",
                "Identification Requirements", "Please bring original valid ID card or Passport for sightseeing checkpoints and check-ins.",
                "신분증 요구 사항", "관광지 입장 및 체크인을 위해 유효한 원본 신분증 또는 여권을 반드시 지참해 주십시오.",
                "Документы", "Пожалуйста, возьмите с собой оригинал удостоверения личности или паспорта для регистрации.");

        seedPolicy("weather-policy", SVG_WEATHER, 4,
                "Thời tiết & Khởi hành", "Tour vẫn khởi hành khi trời mưa nhỏ. Trong trường hợp bão lũ thiên tai bất khả kháng, quý khách được dời ngày hoặc hoàn 100%.",
                "Weather & Departure", "Tours depart in light rain. In cases of severe storms or force majeure, reschedule or 100% refund is provided.",
                "날씨 및 출발", "가벼운 비에도 투어는 정상 출발합니다. 태풍 등 천재지변의 경우 일정 변경 또는 100% 전액 환불됩니다.",
                "Погода и выезд", "Экскурсия состоится при небольшом дожде. В случае шторма или форс-мажора предоставляется перенос даты или 100% возврат.");

        log.info("Default tour policies seeded successfully.");
    }

    private void seedPolicy(String code, String iconSvg, int sortOrder,
                            String viName, String viDesc,
                            String enName, String enDesc,
                            String koName, String koDesc,
                            String ruName, String ruDesc) {
        TourPolicy policy = policyRepository.save(TourPolicy.builder()
                .code(code)
                .icon(iconSvg)
                .sortOrder(sortOrder)
                .active(true)
                .build());

        translationRepository.saveAll(List.of(
                TourPolicyTranslation.builder().policy(policy).locale("vi").name(viName).description(viDesc).build(),
                TourPolicyTranslation.builder().policy(policy).locale("en").name(enName).description(enDesc).build(),
                TourPolicyTranslation.builder().policy(policy).locale("ko").name(koName).description(koDesc).build(),
                TourPolicyTranslation.builder().policy(policy).locale("ru").name(ruName).description(ruDesc).build()
        ));
    }
}
