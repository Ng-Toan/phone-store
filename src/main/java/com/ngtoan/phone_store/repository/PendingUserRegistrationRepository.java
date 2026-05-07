package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.PendingUserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUserRegistrationRepository extends JpaRepository<PendingUserRegistration, Integer> {

    Optional<PendingUserRegistration> findByEmail(String email);

    Optional<PendingUserRegistration> findByUsername(String username);

    void deleteByEmail(String email);
}