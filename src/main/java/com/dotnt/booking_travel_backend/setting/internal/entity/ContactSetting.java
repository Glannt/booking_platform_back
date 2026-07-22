package com.dotnt.booking_travel_backend.setting.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "contact_settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class ContactSetting extends MasterEntity {

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "zalo", nullable = false)
    private String zalo;

    @Column(name = "kakaotalk")
    private String kakaotalk;

    @Column(name = "whatsapp")
    private String whatsapp;
}
