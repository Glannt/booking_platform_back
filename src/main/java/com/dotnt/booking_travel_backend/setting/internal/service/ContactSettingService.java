package com.dotnt.booking_travel_backend.setting.internal.service;

import com.dotnt.booking_travel_backend.setting.internal.entity.ContactSetting;
import com.dotnt.booking_travel_backend.setting.internal.repository.ContactSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactSettingService {

    private final ContactSettingRepository repository;

    @Transactional(readOnly = true)
    public ContactSetting getContactSetting() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ContactSetting defaultSetting = ContactSetting.builder()
                            .phone("0764971971")
                            .zalo("https://zalo.me/0764971971")
                            .kakaotalk("")
                            .whatsapp("")
                            .build();
                    return repository.save(defaultSetting);
                });
    }

    @Transactional
    public ContactSetting updateContactSetting(ContactSetting updateData) {
        ContactSetting existing = getContactSetting();
        existing.setPhone(updateData.getPhone());
        existing.setZalo(updateData.getZalo());
        existing.setKakaotalk(updateData.getKakaotalk());
        existing.setWhatsapp(updateData.getWhatsapp());
        return repository.save(existing);
    }
}
