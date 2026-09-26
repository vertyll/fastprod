package com.vertyll.fastprod.role.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.sharedinfrastructure.entity.BaseEntity;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "role",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_role_is_active", columnList = "is_active"),
        @Index(name = "idx_role_created_at", columnList = "created_at"),
        @Index(name = "idx_role_name_is_active", columnList = "name, is_active")
    }
)
public class Role extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleType name;

    private String description;

    @Column(nullable = false)
    private boolean active;

    @SuppressWarnings("NullAway.Init")
    protected Role() {
        super();
    }

    @Builder
    private Role(RoleType name, String description, @Nullable Boolean active) {
        super();
        this.name = name;
        this.description = description;
        this.active = active == null || active;
    }

    public void update(RoleType name, @Nullable String description) {
        this.name = name;
        if (description != null) {
            this.description = description;
        }
    }
}
