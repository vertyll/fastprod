package com.vertyll.fastprod.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}