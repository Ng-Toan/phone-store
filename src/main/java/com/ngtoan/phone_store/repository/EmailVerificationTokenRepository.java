package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {

    Optional<EmailVerificationToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}