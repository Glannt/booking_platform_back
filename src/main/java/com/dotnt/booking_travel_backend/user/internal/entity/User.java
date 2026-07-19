package com.dotnt.booking_travel_backend.user.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class User extends MasterEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "fullname", length = 255)
    private String fullname;

    @Column(name = "role", length = 50)
    private Role role; // "admin", "customer", "partner", "driver"

    @Column(name = "status", length = 50)
    private String status; // "active", "inactive", "blocked"

    @Column(name = "kakao_talk_id")
    private String kakaoTalkId;

    @Column(name = "zalo_id")
    private String zaloId;



}
