package com.dotnt.booking_travel_backend.selfdrive.internal.config;

import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicy;
import com.dotnt.booking_travel_backend.selfdrive.internal.entity.SelfDrivePolicyTranslation;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDrivePolicyRepository;
import com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDrivePolicyTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(25)
public class SelfDrivePolicyDataInitializer implements CommandLineRunner {

    private final SelfDrivePolicyRepository policyRepository;
    private final SelfDrivePolicyTranslationRepository translationRepository;

    private static final String SVG_BADGE_CHECK = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M3.85 8.62a4 4 0 0 1 4.78-4.77 4 4 0 0 1 6.74 0 4 4 0 0 1 4.78 4.78 4 4 0 0 1 0 6.74 4 4 0 0 1-4.77 4.78 4 4 0 0 1-6.75 0 4 4 0 0 1-4.78-4.77 4 4 0 0 1 0-6.76Z\"/><path d=\"m9 12 2 2 4-4\"/></svg>";
    private static final String SVG_SHIELD_CHECK = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z\"/><path d=\"m9 12 2 2 4-4\"/></svg>";
    private static final String SVG_ROUTE = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"6\" cy=\"19\" r=\"3\"/><path d=\"M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15\"/><circle cx=\"18\" cy=\"5\" r=\"3\"/></svg>";
    private static final String SVG_FUEL = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><line x1=\"3\" x2=\"15\" y1=\"22\" y2=\"22\"/><line x1=\"4\" x2=\"14\" y1=\"9\" y2=\"9\"/><path d=\"M14 22V4a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v18\"/><path d=\"M14 13h2a2 2 0 0 1 2 2v2a2 2 0 0 0 2 2a2 2 0 0 0 2-2V9.83a2 2 0 0 0-.59-1.42L18 5\"/></svg>";

    @Override
    public void run(String... args) {
        if (policyRepository.count() > 0) return;

        log.info("Seeding default self-drive policies...");

        seedPolicy("driver-age", SVG_BADGE_CHECK, 1,
                "Độ tuổi người lái", "Yêu cầu tối thiểu 21 tuổi trở lên",
                "Driver Age", "Minimum 21 years old",
                "운전자 연령", "최소 만 21세 이상",
                "Возраст водителя", "Минимум 21 год");

        seedPolicy("valid-license", SVG_SHIELD_CHECK, 2,
                "Bằng lái xe hợp lệ", "Bằng lái hạng B2/B1 còn hạn tối thiểu 1 năm",
                "Valid Driving License", "Valid B1/B2 license for at least 1 year",
                "유효한 운전면허증", "유효한 B1/B2 면허 소지 1년 이상",
                "Действительные права", "Права категории B1/B2 со стажем от 1 года");

        seedPolicy("mileage-policy", SVG_ROUTE, 3,
                "Chính sách quãng đường", "Không giới hạn quãng đường trong phạm vi nội tỉnh",
                "Mileage Policy", "Unlimited mileage within the province",
                "주행거리 정책", "도내 무제한 주행거리",
                "Лимит пробега", "Безлимитный пробег в пределах провинции");

        seedPolicy("fuel-policy", SVG_FUEL, 4,
                "Chính sách nhiên liệu", "Giao nhận xe cùng mức nhiên liệu (Nhận sao trả vậy)",
                "Fuel Policy", "Same-to-same fuel policy (Return with the same level)",
                "연료 정책", "동일 연료 반납 (받은 만큼 채워서 반납)",
                "Топливная политика", "Возврат с тем же уровнем топлива");

        log.info("Default self-drive policies seeded successfully.");
    }

    private void seedPolicy(String code, String iconSvg, int sortOrder,
                            String viName, String viDesc,
                            String enName, String enDesc,
                            String koName, String koDesc,
                            String ruName, String ruDesc) {
        SelfDrivePolicy policy = policyRepository.save(SelfDrivePolicy.builder()
                .code(code)
                .icon(iconSvg)
                .sortOrder(sortOrder)
                .active(true)
                .build());

        translationRepository.saveAll(List.of(
                SelfDrivePolicyTranslation.builder().policy(policy).locale("vi").name(viName).description(viDesc).build(),
                SelfDrivePolicyTranslation.builder().policy(policy).locale("en").name(enName).description(enDesc).build(),
                SelfDrivePolicyTranslation.builder().policy(policy).locale("ko").name(koName).description(koDesc).build(),
                SelfDrivePolicyTranslation.builder().policy(policy).locale("ru").name(ruName).description(ruDesc).build()
        ));
    }
}
