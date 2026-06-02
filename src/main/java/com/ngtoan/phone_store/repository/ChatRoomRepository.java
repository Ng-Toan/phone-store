package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    Optional<ChatRoom> findByUserId(Integer userId);

    List<ChatRoom> findAllByOrderByUpdatedDateDesc();
}