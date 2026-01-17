package com.vertyll.fastprod.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vertyll.fastprod.common.enums.RoleType;
import com.vertyll.fastprod.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);
}
