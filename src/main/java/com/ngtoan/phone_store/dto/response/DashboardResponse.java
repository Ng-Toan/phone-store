package com.ngtoan.phone_store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {

    BigDecimal totalRevenueThisMonth;
    Long totalOrders;
    Long todayOrders;
    Long totalUsers;

    Long pendingPayments;
    Long totalProducts;
    Long lowStockProducts;

    Long importsThisMonth;
    BigDecimal importAmountThisMonth;

    Long totalSuppliers;
    Long activeSuppliers;

    List<RevenueChartItem> revenueLast7Days;
    List<OrderStatusStat> orderStatusStats;
    List<RecentOrderItem> recentOrders;

    TodayOrderSummary todayOrderSummary;

    List<MembershipOverviewItem> membershipOverview;
    List<RecentUpgradedMemberItem> recentUpgradedMembers;

    List<TopSellingProductItem> topSellingProducts;
    List<LowStockProductItem> lowStockItems;

    // Giữ lại 1 đánh giá mới nhất để frontend cũ không lỗi
    LatestReviewItem latestReview;

    // Dùng cho frontend mới hiển thị nhiều đánh giá, ví dụ 2 đánh giá mới nhất
    List<LatestReviewItem> latestReviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RevenueChartItem {
        LocalDate date;
        String label;
        BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderStatusStat {
        String status;
        String label;
        Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecentOrderItem {
        Integer orderID;
        String orderCode;
        String customerName;
        BigDecimal totalAmount;
        String status;
        String statusLabel;
        LocalDateTime createdDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TodayOrderSummary {
        Long pendingOrders;
        Long shippingOrders;
        BigDecimal todayRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MembershipOverviewItem {
        Integer levelID;
        String levelName;
        Long userCount;
        Integer percent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecentUpgradedMemberItem {
        Integer userID;
        String fullName;
        String fromLevel;
        String toLevel;
        LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TopSellingProductItem {
        Integer productID;
        String productName;
        Long soldQuantity;
        BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LowStockProductItem {
        Integer productID;
        String productName;
        Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LatestReviewItem {
        Integer feedbackID;
        Integer productID;
        String productName;
        Integer rating;
        String comment;
        LocalDateTime createdDate;
    }
}