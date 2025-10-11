package com.vertyll.fastprod.role.repository;

import java.util.Optional;

import com.vertyll.fastprod.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
