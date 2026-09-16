package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addon_category_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"addon_category_id", "locale"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"addonCategory"})
@ToString(exclude = {"addonCategory"})
@Builder
public class AddOnCategoryTranslation extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_category_id", nullable = false)
    private AddOnCategory addonCategory;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
