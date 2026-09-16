package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "self_drive_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveType extends MasterEntity {
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @OneToMany(mappedBy = "selfDriveType", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SelfDriveTypeTranslation> translations = new ArrayList<>();
}
