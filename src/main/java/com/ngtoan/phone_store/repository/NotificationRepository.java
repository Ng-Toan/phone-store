package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIDOrderByCreatedDateDesc(Integer userID);

    List<Notification> findByRoleTargetOrderByCreatedDateDesc(String roleTarget);

    Long countByUserIDAndIsReadFalse(Integer userID);

    Long countByRoleTargetAndIsReadFalse(String roleTarget);
}
