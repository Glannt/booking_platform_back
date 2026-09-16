package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "self_drive_type_translations",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"rental_type_id", "locale"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveTypeTranslation extends MasterEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_type_id", nullable = false)
    private SelfDriveType selfDriveType;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
