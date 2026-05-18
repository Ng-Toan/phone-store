package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.MembershipLevelRequest;
import com.ngtoan.phone_store.dto.response.MembershipLevelResponse;
import com.ngtoan.phone_store.dto.response.UserMembershipResponse;
import com.ngtoan.phone_store.service.MembershipLevelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/membership-levels")
@RequiredArgsConstructor
public class MembershipLevelController {

    private final MembershipLevelService membershipLevelService;

    // USER / ADMIN xem danh sách cấp độ chưa bị xóa mềm
    @GetMapping
    public ResponseEntity<List<MembershipLevelResponse>> getAllLevels() {
        return ResponseEntity.ok(
                membershipLevelService.getAllLevels()
        );
    }

    // USER xem hạng thành viên của chính mình
    @GetMapping("/user/me")
    public ResponseEntity<UserMembershipResponse> getMyMembership(
            Authentication authentication
    ) {
        String username = authentication.getName();

        return ResponseEntity.ok(
                membershipLevelService.getMyMembership(username)
        );
    }

    // ADMIN thêm level
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipLevelResponse> createLevel(
            @Valid @RequestBody MembershipLevelRequest request
    ) {
        return ResponseEntity.ok(
                membershipLevelService.createLevel(request)
        );
    }

    // ADMIN sửa level
    @PutMapping("/admin/{levelID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipLevelResponse> updateLevel(
            @PathVariable Integer levelID,
            @Valid @RequestBody MembershipLevelRequest request
    ) {
        return ResponseEntity.ok(
                membershipLevelService.updateLevel(levelID, request)
        );
    }

    // ADMIN xóa mềm level
    @DeleteMapping("/admin/{levelID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteLevel(
            @PathVariable Integer levelID
    ) {
        membershipLevelService.deleteLevel(levelID);

        return ResponseEntity.ok("Membership level hidden successfully");
    }

    // ADMIN tính lại level toàn bộ user
    @PutMapping("/admin/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recalculateAllUsersMembership() {

        membershipLevelService.recalculateAllUsersMembership();

        return ResponseEntity.ok("All users membership recalculated successfully");
    }
}