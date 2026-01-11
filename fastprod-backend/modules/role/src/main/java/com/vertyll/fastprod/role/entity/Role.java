package com.vertyll.fastprod.role.entity;

import java.io.Serial;

import lombok.*;

import com.vertyll.fastprod.common.entity.BaseEntity;

import jakarta.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "role",
        uniqueConstraints = {@UniqueConstraint(name = "uk_role_name", columnNames = "name")},
        indexes = {
            @Index(name = "idx_role_is_active", columnList = "is_active"),
            @Index(name = "idx_role_created_at", columnList = "created_at"),
            @Index(name = "idx_role_name_is_active", columnList = "name, is_active")
        })
public class Role extends BaseEntity {

    @Serial private static final long serialVersionUID = 1L;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
