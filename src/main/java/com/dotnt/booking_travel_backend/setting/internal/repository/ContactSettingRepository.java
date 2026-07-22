package com.dotnt.booking_travel_backend.setting.internal.repository;

import com.dotnt.booking_travel_backend.setting.internal.entity.ContactSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactSettingRepository extends JpaRepository<ContactSetting, Long> {
}
