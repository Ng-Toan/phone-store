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

    // UPDATE - Chỉ sửa feedback của chính mình (yêu cầu đăng nhập)
    @PutMapping("/update/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Integer feedbackId,
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                feedbackService.updateFeedback(feedbackId, getUserId(authentication), request)
        );
    }

    // DELETE - Chỉ xoá feedback của chính mình (yêu cầu đăng nhập)
    @DeleteMapping("/delete/{feedbackId}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Integer feedbackId,
            Authentication authentication) {

        feedbackService.deleteFeedback(feedbackId, getUserId(authentication));
        return ResponseEntity.ok("Feedback deleted successfully");
    }
}