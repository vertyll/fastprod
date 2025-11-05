package com.vertyll.fastprod.user.repository;

import java.util.Optional;

import com.vertyll.fastprod.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.roles r " +
           "WHERE u.isActive = true")
    Page<User> findActiveUsers(Pageable pageable);
}
