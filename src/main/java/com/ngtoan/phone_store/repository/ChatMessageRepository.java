package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderByCreatedDateAsc(Integer roomId);

    Long countByRoomIdAndSenderIdNotAndIsReadFalse(Integer roomId, Integer senderId);
    
    List<ChatMessage> findByRoomIdAndSenderIdNotAndIsReadFalse(
        Integer roomId,
        Integer senderId
    );
}