package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.FeedbackRequest;
import com.ngtoan.phone_store.dto.response.FeedbackResponse;
import com.ngtoan.phone_store.dto.response.RatingSummaryResponse;
import com.ngtoan.phone_store.service.FeedbackService;
import com.ngtoan.phone_store.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    private Integer getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username).getUserId();
    }

    // CREATE
    @PostMapping("/add")
    public ResponseEntity<FeedbackResponse> addFeedback(
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                feedbackService.addFeedback(getUserId(authentication), request)
        );
    }

    // READ - Lấy tất cả feedback của sản phẩm (công khai)
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByProduct(
            @PathVariable Integer productId) {

        return ResponseEntity.ok(
                feedbackService.getFeedbackByProduct(productId)
        );
    }

    // READ - Lọc theo số sao
    // VD: GET /api/feedback/product/1/filter?rating=5
    @GetMapping("/product/{productId}/filter")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByRating(
            @PathVariable Integer productId,
            @RequestParam Integer rating) {

        return ResponseEntity.ok(
                feedbackService.getFeedbackByProductAndRating(productId, rating)
        );
    }

    // READ - Lấy tổng hợp rating
    // VD: GET /api/feedback/product/1/summary
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(
            @PathVariable Integer productId) {

        return ResponseEntity.ok(
                feedbackService.getRatingSummary(productId)
        );
    }

    // UPDATE - USER sửa của mình, ADMIN sửa được tất cả
    @PutMapping("/update/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Integer feedbackId,
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                feedbackService.updateFeedback(
                        feedbackId,
                        getUserId(authentication),
                        isAdmin,
                        request
                )
        );
    }

    // DELETE - USER xoá của mình, ADMIN xoá được tất cả
    @DeleteMapping("/delete/{feedbackId}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Integer feedbackId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        feedbackService.deleteFeedback(
                feedbackId,
                getUserId(authentication),
                isAdmin
        );

        return ResponseEntity.ok("Feedback deleted successfully");
    }

    // ADMIN - Lấy tất cả feedback
        @GetMapping("/admin/all")
        public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
        }
}