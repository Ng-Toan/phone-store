package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByFullNameContainingIgnoreCase(String name);
    List<User> findByLevelId(Integer levelId);
    boolean existsByLevelId(Integer levelId);
}
