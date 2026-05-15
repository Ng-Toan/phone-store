package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface MembershipLevelRepository extends JpaRepository<MembershipLevel, Integer> {

    boolean existsByLevelNameIgnoreCase(String levelName);

    boolean existsByLevelNameIgnoreCaseAndLevelIDNot(String levelName, Integer levelID);

    boolean existsByMinSpent(BigDecimal minSpent);

    boolean existsByMinSpentAndLevelIDNot(BigDecimal minSpent, Integer levelID);

    Optional<MembershipLevel> findTopByMinSpentLessThanEqualOrderByMinSpentDesc(BigDecimal totalSpent);

    Optional<MembershipLevel> findTopByOrderByMinSpentAsc();

    
}