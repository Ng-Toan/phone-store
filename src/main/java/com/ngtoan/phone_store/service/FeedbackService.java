package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.FeedbackRequest;
import com.ngtoan.phone_store.dto.response.FeedbackResponse;
import com.ngtoan.phone_store.dto.response.RatingSummaryResponse;
import com.ngtoan.phone_store.entity.Feedback;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.mapper.FeedbackMapper;
import com.ngtoan.phone_store.repository.FeedbackRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper feedbackMapper;

    // CREATE
    public FeedbackResponse addFeedback(Integer userId, FeedbackRequest request) {

        productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        Feedback feedback = Feedback.builder()
                .userID(userId)
                .productID(request.getProductId())
                .comment(request.getComment())
                .rating(request.getRating())
                .createdDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();

        feedbackRepository.save(feedback);

        return buildFeedbackResponse(feedback);
    }

    // READ - Lấy tất cả feedback của 1 sản phẩm
    public List<FeedbackResponse> getFeedbackByProduct(Integer productId) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        return feedbackRepository
                .findByProductIDOrderByCreatedDateDesc(productId)
                .stream()
                .map(this::buildFeedbackResponse)
                .toList();
    }

    // ADMIN - Lấy tất cả feedback
        public List<FeedbackResponse> getAllFeedbacks() {
            return feedbackRepository
                    .findAllByOrderByCreatedDateDesc()
                    .stream()
                    .map(this::buildFeedbackResponse)
                    .toList();
        }

    // READ - Lọc feedback theo số sao
    public List<FeedbackResponse> getFeedbackByProductAndRating(Integer productId, Integer rating) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        return feedbackRepository
                .findByProductIDAndRatingOrderByCreatedDateDesc(productId, rating)
                .stream()
                .map(this::buildFeedbackResponse)
                .toList();
    }

    // READ - Lấy tổng hợp rating
    public RatingSummaryResponse getRatingSummary(Integer productId) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        Double average = feedbackRepository.getAverageRatingByProductID(productId);

        Map<Integer, Long> breakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            Long count = feedbackRepository.countByProductIDAndRating(productId, star);
            breakdown.put(star, count);
        }

        long totalReviews = breakdown.values().stream().mapToLong(Long::longValue).sum();

        RatingSummaryResponse summary = new RatingSummaryResponse();
        summary.setAverageRating(average != null ? Math.round(average * 10.0) / 10.0 : 0.0);
        summary.setTotalReviews((int) totalReviews);
        summary.setRatingBreakdown(breakdown);

        return summary;
    }

    // UPDATE - Chỉ sửa comment và rating của chính mình
    public FeedbackResponse updateFeedback(
            Integer feedbackId,
            Integer userId,
            boolean isAdmin,
            FeedbackRequest request
    ) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback not found with id: " + feedbackId));

        if (!isAdmin && !feedback.getUserID().equals(userId)) {
            throw new RuntimeException("You are not allowed to update this feedback");
        }

        feedback.setComment(request.getComment());
        feedback.setRating(request.getRating());

        feedbackRepository.save(feedback);

        return buildFeedbackResponse(feedback);
    }

    // DELETE
    public void deleteFeedback(Integer feedbackId, Integer userId, boolean isAdmin) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback not found with id: " + feedbackId));

        if (!isAdmin && !feedback.getUserID().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this feedback");
        }

        feedbackRepository.delete(feedback);
    }

    private FeedbackResponse buildFeedbackResponse(Feedback feedback) {

           String userFullName = userRepository.findById(feedback.getUserID())
            .map(u -> u.getFullName())
            .orElse("Unknown");

        String productName = productRepository.findById(feedback.getProductID())
                .map(p -> p.getName())
                .orElse("Unknown");

        FeedbackResponse response = feedbackMapper.toResponse(feedback);
        response.setUserFullName(userFullName);
        response.setProductName(productName);

        return response;
    }
}