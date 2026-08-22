package com.dotnt.booking_travel_backend.user.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Role {

    public static final String CODE_ADMIN = "ADMIN";
    public static final String CODE_MODERATOR = "MODERATOR";
    public static final String CODE_STAFF = "STAFF";
    public static final String CODE_CUSTOMER = "CUSTOMER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"})
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
