package com.vertyll.fastprod.user.entity;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.*;

import com.vertyll.fastprod.common.entity.BaseEntity;
import com.vertyll.fastprod.role.entity.Role;

import jakarta.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "\"user\"",
        indexes = {
            @Index(name = "idx_user_email", columnList = "email"),
            @Index(name = "idx_user_is_active", columnList = "is_active"),
            @Index(name = "idx_user_is_verified", columnList = "is_verified"),
            @Index(name = "idx_user_created_at", columnList = "created_at"),
            @Index(name = "idx_user_is_active_is_verified", columnList = "is_active, is_verified"),
        })
public class User extends BaseEntity implements UserDetails {

    @Serial private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns =
                    @JoinColumn(
                            name = "user_id",
                            foreignKey = @ForeignKey(name = "fk_user_role_user")),
            inverseJoinColumns =
                    @JoinColumn(
                            name = "role_id",
                            foreignKey = @ForeignKey(name = "fk_user_role_role")))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Override
    @NullUnmarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
    }

    @Override
    @NullUnmarked
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
