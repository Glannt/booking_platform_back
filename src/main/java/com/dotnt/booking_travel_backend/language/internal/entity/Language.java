package com.dotnt.booking_travel_backend.language.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "languages")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language extends MasterEntity {

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "flag_icon", length = 50)
    private String flagIcon;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;
}
