package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.ChatMessageRequest;
import com.ngtoan.phone_store.dto.response.ChatMessageResponse;
import com.ngtoan.phone_store.dto.response.ChatRoomResponse;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.UserRepository;
import com.ngtoan.phone_store.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping("/room")
    public ChatRoomResponse getOrCreateMyRoom(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return chatService.getOrCreateMyRoom(user);
    }

    @GetMapping("/admin/rooms")
    public List<ChatRoomResponse> getAllRooms(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return chatService.getAllRooms(user);
    }

    @GetMapping("/history/{roomId}")
    public List<ChatMessageResponse> getHistory(
            @PathVariable Integer roomId,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        return chatService.getHistory(roomId, user);
    }

    @PostMapping("/send")
    public ChatMessageResponse sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        return chatService.sendMessage(request, user);
    }

    @PutMapping("/read/{roomId}")
    public String markAsRead(
            @PathVariable Integer roomId,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        chatService.markAsRead(roomId, user);
        return "Messages marked as read";
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userRepository.findByUsername(authentication.getName());

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return user;
    }
}