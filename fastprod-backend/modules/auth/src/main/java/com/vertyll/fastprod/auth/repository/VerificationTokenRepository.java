package com.vertyll.fastprod.auth.repository;

import java.util.Optional;

import com.vertyll.fastprod.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}
