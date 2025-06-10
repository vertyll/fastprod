package com.vertyll.fastprod.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}