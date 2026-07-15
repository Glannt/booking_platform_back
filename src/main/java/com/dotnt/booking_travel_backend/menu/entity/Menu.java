package com.dotnt.booking_travel_backend.menu;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Menu entity representing a menu item in the system.
 * Extends MasterEntity for common audit and base fields.
 */
@Entity
@Table(name = "menu")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@DiscriminatorValue("MENU")
public class Menu extends MasterEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price")
    private Double price;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
