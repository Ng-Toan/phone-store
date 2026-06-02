package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ChatMessageRequest;
import com.ngtoan.phone_store.dto.response.ChatMessageResponse;
import com.ngtoan.phone_store.dto.response.ChatRoomResponse;
import com.ngtoan.phone_store.entity.ChatMessage;
import com.ngtoan.phone_store.entity.ChatRoom;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.ForbiddenException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.ChatMessageRepository;
import com.ngtoan.phone_store.repository.ChatRoomRepository;
import com.ngtoan.phone_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRoomResponse getOrCreateMyRoom(User user) {
        if (user.getRoleId() == 1) {
            throw new ForbiddenException("Admin không thể tạo phòng chat user");
        }

        ChatRoom room = chatRoomRepository.findByUserId(user.getUserId())
                .orElseGet(() -> createRoom(user.getUserId()));

        return toRoomResponse(room);
    }

    public List<ChatRoomResponse> getAllRooms(User user) {
        checkAdmin(user);

        return chatRoomRepository.findAllByOrderByUpdatedDateDesc()
                .stream()
                .map(this::toRoomResponse)
                .toList();
    }

    public List<ChatMessageResponse> getHistory(Integer roomId, User user) {
        ChatRoom room = getRoom(roomId);
        checkRoomPermission(room, user);

        return chatMessageRepository.findByRoomIdOrderByCreatedDateAsc(roomId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public ChatMessageResponse sendMessage(ChatMessageRequest request, User sender) {
        ChatRoom room = getRoom(request.getRoomId());
        checkRoomPermission(room, sender);

        ChatMessage message = new ChatMessage();
        message.setRoomId(room.getRoomId());
        message.setSenderId(sender.getUserId());
        message.setMessage(request.getMessage());
        message.setIsRead(false);
        message.setCreatedDate(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);

        room.setUpdatedDate(LocalDateTime.now());
        chatRoomRepository.save(room);

        ChatMessageResponse response = toMessageResponse(saved);

        messagingTemplate.convertAndSend(
                "/topic/chat/room/" + room.getRoomId(),
                response
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/admin",
                toRoomResponse(room)
        );

        return response;
    }

    public void markAsRead(Integer roomId, User user) {
        ChatRoom room = getRoom(roomId);
        checkRoomPermission(room, user);

        List<ChatMessage> unreadMessages =
            chatMessageRepository.findByRoomIdAndSenderIdNotAndIsReadFalse(
                    roomId,
                    user.getUserId()
            );

        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
        }
        chatMessageRepository.saveAll(unreadMessages);
    }

    private ChatRoom createRoom(Integer userId) {
        ChatRoom room = new ChatRoom();
        room.setUserId(userId);
        room.setCreatedDate(LocalDateTime.now());
        room.setUpdatedDate(LocalDateTime.now());

        return chatRoomRepository.save(room);
    }

    private ChatRoom getRoom(Integer roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
    }

    private void checkRoomPermission(ChatRoom room, User user) {
        if (user.getRoleId() == 1) {
            return;
        }

        if (!room.getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Bạn không có quyền xem phòng chat này");
        }
    }

    private void checkAdmin(User user) {
        if (user.getRoleId() != 1) {
            throw new ForbiddenException("Chỉ admin được dùng chức năng này");
        }
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        User user = userRepository.findById(room.getUserId())
                .orElse(null);

        List<ChatMessage> messages =
                chatMessageRepository.findByRoomIdOrderByCreatedDateAsc(room.getRoomId());

        ChatMessage lastMessage = messages.isEmpty()
                ? null
                : messages.get(messages.size() - 1);

        ChatRoomResponse response = new ChatRoomResponse();
        response.setRoomId(room.getRoomId());
        response.setUserId(room.getUserId());
        response.setUpdatedDate(room.getUpdatedDate());

        if (user != null) {
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setEmail(user.getEmail());

            Long unreadCount =
                    chatMessageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(
                            room.getRoomId(),
                            1
                    );

            response.setUnreadCount(unreadCount);
        }

        if (lastMessage != null) {
            response.setLastMessage(lastMessage.getMessage());
        }

        return response;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElse(null);

        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(message.getMessageId());
        response.setRoomId(message.getRoomId());
        response.setSenderId(message.getSenderId());
        response.setMessage(message.getMessage());
        response.setIsRead(message.getIsRead());
        response.setCreatedDate(message.getCreatedDate());

        if (sender != null) {
            response.setSenderName(sender.getFullName());
        }

        return response;
    }
}