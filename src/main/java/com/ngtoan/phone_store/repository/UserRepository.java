package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByFullNameContainingIgnoreCase(String name);
}
