package com.vertyll.fastprod.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);
}
