package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "self_drive_policy_translations",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"policy_id", "locale"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDrivePolicyTranslation extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SelfDrivePolicy policy;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;
}
