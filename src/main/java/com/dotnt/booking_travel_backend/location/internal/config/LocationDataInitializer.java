package com.dotnt.booking_travel_backend.location.internal.config;

import com.dotnt.booking_travel_backend.location.internal.entity.Location;
import com.dotnt.booking_travel_backend.location.internal.entity.LocationTranslation;
import com.dotnt.booking_travel_backend.location.internal.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(25)
public class LocationDataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (locationRepository.count() > 0) {
            log.info("Locations already seeded, skipping.");
            return;
        }

        log.info("Seeding location data...");

        record Loc(double lat, double lng, String cat,
                   String viName, String viAddr,
                   String enName, String enAddr,
                   String koName, String koAddr,
                   String ruName, String ruAddr) {}

        List<Loc> locations = List.of(
            // === HOTELS / RESORTS ===
            new Loc(12.2403, 109.1935, "hotel",
                "InterContinental Nha Trang", "16 Trần Phú, Nha Trang, Khánh Hòa",
                "InterContinental Nha Trang", "16 Tran Phu, Nha Trang, Khanh Hoa",
                "인터컨티넨탈 나트랑", "16 트란푸, 나트랑, 끼엔호아",
                "Интерконтиненталь Нячанг", "16 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2470, 109.1920, "hotel",
                "Khách sạn Havana Nha Trang", "38 Trần Phú, Nha Trang, Khánh Hòa",
                "Havana Nha Trang Hotel", "38 Tran Phu, Nha Trang, Khanh Hoa",
                "하바나 나트랑 호텔", "38 트란푸, 나트랑, 끼엔호아",
                "Отель Гавана Нячанг", "38 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2454, 109.1925, "hotel",
                "Novotel Nha Trang", "50 Trần Phú, Nha Trang, Khánh Hòa",
                "Novotel Nha Trang", "50 Tran Phu, Nha Trang, Khanh Hoa",
                "노보텔 나트랑", "50 트란푸, 나트랑, 끼엔호아",
                "Новотель Нячанг", "50 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2610, 109.2830, "resort",
                "Vinpearl Resort Nha Trang", "Đảo Hòn Tre, Nha Trang, Khánh Hòa",
                "Vinpearl Resort Nha Trang", "Hon Tre Island, Nha Trang, Khanh Hoa",
                "빈펄 리조트 나트랑", "혼 트레 섬, 나트랑, 끼엔호아",
                "Ризорт Винперл Нячанг", "остров Хон Тре, Нячанг, Кхань Хоа"),

            new Loc(12.2440, 109.1930, "hotel",
                "Sheraton Nha Trang", "26-28 Trần Phú, Nha Trang, Khánh Hòa",
                "Sheraton Nha Trang", "26-28 Tran Phu, Nha Trang, Khanh Hoa",
                "쉐라톤 나트랑", "26-28 트란푸, 나트랑, 끼엔호아",
                "Шератон Нячанг", "26-28 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2460, 109.1915, "hotel",
                "Liberty Central Nha Trang", "15 Trần Phú, Nha Trang, Khánh Hòa",
                "Liberty Central Nha Trang", "15 Tran Phu, Nha Trang, Khanh Hoa",
                "리버티 센트럴 나트랑", "15 트란푸, 나트랑, 끼엔호아",
                "Либерти Сентрал Нячанг", "15 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2390, 109.1940, "hotel",
                "Sailing Club Nha Trang", "10-12 Trần Phú, Nha Trang, Khánh Hòa",
                "Sailing Club Nha Trang", "10-12 Tran Phu, Nha Trang, Khanh Hoa",
                "세일링 클럽 나트랑", "10-12 트란푸, 나트랑, 끼엔호아",
                "Сейлинг Клуб Нячанг", "10-12 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2200, 109.1850, "resort",
                "Mia Resort Nha Trang", "Nguyễn Thiện Thuật, Nha Trang, Khánh Hòa",
                "Mia Resort Nha Trang", "Nguyen Thien Thuat, Nha Trang, Khanh Hoa",
                "미아 리조트 나트랑", "응우옌 티ện 주엣, 나트랑, 끼엔호아",
                "Ризорт Миа Нячанг", "Нгуен Тхиен Тхуат, Нячанг, Кхань Хоа"),

            new Loc(12.2500, 109.1750, "resort",
                "Amiana Resort Nha Trang", "Nguyễn Cơ Thạch, Nha Trang, Khánh Hòa",
                "Amiana Resort Nha Trang", "Nguyen Co Thach, Nha Trang, Khanh Hoa",
                "아미아나 리조트 나트랑", "응우옌 꼬 타츠, 나트랑, 끼엔호아",
                "Ризорт Амиана Нячанг", "Нгуен Ко Тхак, Нячанг, Кхань Хоа"),

            new Loc(12.2470, 109.1960, "hotel",
                "Evason Ana Mandara Nha Trang", "29 Trần Phú, Nha Trang, Khánh Hòa",
                "Evason Ana Mandara Nha Trang", "29 Tran Phu, Nha Trang, Khanh Hoa",
                "에바슨 아나 만다라 나트랑", "29 트란푸, 나트랑, 끼엔호아",
                "Эвасон Ана Мандара Нячанг", "29 Тран Фу, Нячанг, Кхань Хоа"),

            // === BEACHES ===
            new Loc(12.2450, 109.1940, "beach",
                "Bãi biển Nha Trang", "Bãi biển Nha Trang, Trần Phú, Nha Trang",
                "Nha Trang Beach", "Nha Trang Beach, Tran Phu, Nha Trang",
                "나트랑 해변", "나트랑 해변, 트란푸, 나트랑",
                "Пляж Нячанг", "Пляж Нячанг, Тран Фу, Нячанг"),

            new Loc(12.2350, 109.2200, "beach",
                "Bãi Tranh", "Bãi Tranh, Nha Trang, Khánh Hòa",
                "Bai Tranh Beach", "Bai Tranh, Nha Trang, Khanh Hoa",
                "바이特朗 해변", "바이特朗, 나트랑, 끼엔호아",
                "Пляж Бай Трань", "Бай Трань, Нячанг, Кхань Хоа"),

            new Loc(12.2700, 109.1700, "beach",
                "Bãi biển Dốc Lết", "Dốc Lết, Nha Trang, Khánh Hòa",
                "Doc Let Beach", "Doc Let, Nha Trang, Khanh Hoa",
                "닥렛 해변", "닥렛, 나트랑, 끼엔호아",
                "Пляж Док Лет", "Док Лет, Нячанг, Кхань Хоа"),

            // === LANDMARKS ===
            new Loc(12.2570, 109.1940, "landmark",
                "Tháp Po Nagar", "490 Đường 2/4, Nha Trang, Khánh Hòa",
                "Po Nagar Cham Towers", "490 2/4 Street, Nha Trang, Khanh Hoa",
                "포나가르 참탑", "490 2/4 거리, 나트랑, 끼엔호아",
                "Башни По Нагар", "490 улица 2/4, Нячанг, Кхань Хоа"),

            new Loc(12.2520, 109.1900, "landmark",
                "Chùa Long Sơn", "42 Đường 23 Tháng 10, Nha Trang, Khánh Hòa",
                "Long Son Pagoda", "42 October 23 Street, Nha Trang, Khanh Hoa",
                "롱손 사원", "42 10월 23일 거리, 나트랑, 끼엔호아",
                "Храм Лонг Сон", "42 улица 23 октября, Нячанг, Кхань Хоа"),

            new Loc(12.2480, 109.1890, "landmark",
                "Nhà thờ Nha Trang", "31 Thái Nguyên, Nha Trang, Khánh Hòa",
                "Nha Trang Cathedral", "31 Thai Nguyen, Nha Trang, Khanh Hoa",
                "나트랑 성당", "31 타이 응우옌, 나트랑, 끼엔호아",
                "Собор Нячанг", "31 Тай Нгуен, Нячанг, Кхань Хоа"),

            new Loc(12.2510, 109.1880, "landmark",
                "Bảo tàng Alexandre Yersin", "10 Đường 23 Tháng 10, Nha Trang, Khánh Hòa",
                "Alexandre Yersin Museum", "10 October 23 Street, Nha Trang, Khanh Hoa",
                "알렉산드르 예르생 박물관", "10 10월 23일 거리, 나트랑, 끼엔호아",
                "Музей Александра Ерсена", "10 улица 23 октября, Нячанг, Кхань Хоа"),

            new Loc(12.2430, 109.1960, "landmark",
                "Viện Hải dương học", "1 Cầu Đá, Nha Trang, Khánh Hòa",
                "Oceanographic Institute", "1 Cau Da, Nha Trang, Khanh Hoa",
                "해양학 연구소", "1 코 다, 나트랑, 끼엔호아",
                "Институт океанографии", "1 Кау Да, Нячанг, Кхань Хоа"),

            new Loc(12.2490, 109.1910, "landmark",
                "Tháp Trầm Hương", "44 Trần Phú, Nha Trang, Khánh Hòa",
                "Khanh Hoa Museum", "44 Tran Phu, Nha Trang, Khanh Hoa",
                "칸호아 박물관", "44 트란푸, 나트랑, 끼엔호아",
                "Музей Кхань Хоа", "44 Тран Фу, Нячанг, Кхань Хоа"),

            // === SHOPPING ===
            new Loc(12.2480, 109.1910, "shopping",
                "Chợ Đầm", "27 Trần Phú, Nha Trang, Khánh Hòa",
                "Dam Market", "27 Tran Phu, Nha Trang, Khanh Hoa",
                "담 시장", "27 트란푸, 나트랑, 끼엔호아",
                "Рынок Дам", "27 Тран Фу, Нячанг, Кхань Хоа"),

            new Loc(12.2460, 109.1930, "shopping",
                "Nha Trang Center", "60 Trần Phú, Nha Trang, Khánh Hòa",
                "Nha Trang Center", "60 Tran Phu, Nha Trang, Khanh Hoa",
                "나트랑 센터", "60 트란푸, 나트랑, 끼엔호아",
                "Нячанг Центр", "60 Тран Фу, Нячанг, Кхань Хоа"),

            // === OTHER ===
            new Loc(12.2610, 109.2830, "attraction",
                "VinWonders Nha Trang", "Đảo Hòn Tre, Nha Trang, Khánh Hòa",
                "VinWonders Nha Trang", "Hon Tre Island, Nha Trang, Khanh Hoa",
                "빈원더스 나트랑", "혼 트레 섬, 나트랑, 끼엔호아",
                "ВинВондерс Нячанг", "остров Хон Тре, Нячанг, Кхань Хоа"),

            new Loc(12.2440, 109.1970, "landmark",
                "Cảng Nha Trang", "1 Bạch Đằng, Nha Trang, Khánh Hòa",
                "Nha Trang Port", "1 Bach Dang, Nha Trang, Khanh Hoa",
                "나트랑 항구", "1 락 방, 나트랑, 끼엔호아",
                "Порт Нячанг", "1 Бак Данг, Нячанг, Кхань Хоа"),

            new Loc(11.9984, 109.2195, "landmark",
                "Sân bay Quốc tế Cam Ranh", "Cam Ranh, Khánh Hòa",
                "Cam Ranh International Airport", "Cam Ranh, Khanh Hoa",
                "깜란 국제공항", "깜란, 끼엔호아",
                "Международный аэропорт Камрань", "Камрань, Кхань Хоа")
        );

        for (Loc l : locations) {
            Location loc = locationRepository.save(Location.builder()
                    .latitude(l.lat())
                    .longitude(l.lng())
                    .category(l.cat())
                    .active(true)
                    .translations(new ArrayList<>())
                    .build());

            loc.getTranslations().add(LocationTranslation.builder()
                    .location(loc).locale("vi")
                    .name(l.viName()).address(l.viAddr())
                    .build());
            loc.getTranslations().add(LocationTranslation.builder()
                    .location(loc).locale("en")
                    .name(l.enName()).address(l.enAddr())
                    .build());
            loc.getTranslations().add(LocationTranslation.builder()
                    .location(loc).locale("ko")
                    .name(l.koName()).address(l.koAddr())
                    .build());
            loc.getTranslations().add(LocationTranslation.builder()
                    .location(loc).locale("ru")
                    .name(l.ruName()).address(l.ruAddr())
                    .build());

            locationRepository.save(loc);
        }

        log.info("Location data seeded successfully! ({} locations)", locations.size());
    }
}
