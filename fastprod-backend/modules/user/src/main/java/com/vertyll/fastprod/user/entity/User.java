package com.vertyll.fastprod.user.entity;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.entity.BaseEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "\"user\"",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_is_active", columnList = "is_active"),
        @Index(name = "idx_user_is_verified", columnList = "is_verified"),
        @Index(name = "idx_user_created_at", columnList = "created_at"),
        @Index(name = "idx_user_is_active_is_verified", columnList = "is_active, is_verified"),
    }
)
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "keycloak_id", nullable = false, unique = true, updatable = false)
    private String keycloakId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_role_user")),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role_role"))
    )
    private Set<Role> roles;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean active;

    @SuppressWarnings(
        {
            "NullAway.Init",
            "java:S2637"
        }
    )
    protected User() {
        super();
        this.roles = new HashSet<>();
    }

    @Builder
    private User(
        String keycloakId,
        String firstName,
        String lastName,
        String email,
        @Nullable Set<Role> roles,
        boolean verified,
        @Nullable Boolean active
    ) {
        super();
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
        this.verified = verified;
        this.active = !Boolean.FALSE.equals(active);
    }

    public void assignRoles(Set<Role> assignedRoles) {
        this.roles = new HashSet<>(assignedRoles);
    }

    public void syncIdentity(String newEmail, String newFirstName, String newLastName, boolean emailVerified) {
        this.email = newEmail;
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.verified = emailVerified;
    }

    public void deactivate() {
        this.active = false;
    }

    public void rename(String newFirstName, String newLastName) {
        this.firstName = newFirstName;
        this.lastName = newLastName;
    }

    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateDetails(String newFirstName, String newLastName, String newEmail) {
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.email = newEmail;
    }
}
