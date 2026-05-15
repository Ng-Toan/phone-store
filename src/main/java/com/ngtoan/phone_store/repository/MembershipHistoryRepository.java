package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Integer> {

    List<MembershipHistory> findTop5ByOrderByUpdatedAtDesc();
}