package com.dotnt.booking_travel_backend.user.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedToken extends MasterEntity {

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
