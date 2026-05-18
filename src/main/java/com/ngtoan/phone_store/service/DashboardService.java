package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.DashboardResponse;
import com.ngtoan.phone_store.entity.*;
import com.ngtoan.phone_store.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    OrderRepository orderRepository;
    OrderDetailRepository orderDetailRepository;
    UserRepository userRepository;
    PaymentRepository paymentRepository;
    ProductRepository productRepository;
    SupplierRepository supplierRepository;
    ImportReceiptRepository importReceiptRepository;
    MembershipLevelRepository membershipLevelRepository;
    MembershipHistoryRepository membershipHistoryRepository;
    FeedbackRepository feedbackRepository;

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();

        BigDecimal totalRevenueThisMonth = orderRepository.sumRevenueByStatusAndDateRange(
                OrderStatus.DELIVERED,
                monthStart,
                nextMonthStart
        );

        Long totalOrders = orderRepository.count();
        Long todayOrders = orderRepository.countByCreatedDateBetween(todayStart, tomorrowStart);
        Long totalUsers = userRepository.count();

        Long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);

        Long totalProducts = productRepository.count();
        Long lowStockProducts = productRepository.countByQuantityLessThanEqualAndStatus(5, 1);

        Long importsThisMonth = importReceiptRepository.countByCreatedDateBetween(monthStart, nextMonthStart);
        BigDecimal importAmountThisMonth = importReceiptRepository.sumCompletedImportAmountByDateRange(
                monthStart,
                nextMonthStart
        );

        Long totalSuppliers = supplierRepository.count();
        Long activeSuppliers = supplierRepository.countByStatus("ACTIVE");

        List<DashboardResponse.LatestReviewItem> latestReviews = buildLatestReviews(2);

        return DashboardResponse.builder()
                .totalRevenueThisMonth(nullToZero(totalRevenueThisMonth))
                .totalOrders(totalOrders)
                .todayOrders(todayOrders)
                .totalUsers(totalUsers)
                .pendingPayments(pendingPayments)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .importsThisMonth(importsThisMonth)
                .importAmountThisMonth(nullToZero(importAmountThisMonth))
                .totalSuppliers(totalSuppliers)
                .activeSuppliers(activeSuppliers)
                .revenueLast7Days(buildRevenueLast7Days(today))
                .orderStatusStats(buildOrderStatusStats())
                .recentOrders(buildRecentOrders())
                .todayOrderSummary(buildTodayOrderSummary(todayStart, tomorrowStart))
                .membershipOverview(buildMembershipOverview(totalUsers))
                .recentUpgradedMembers(buildRecentUpgradedMembers())
                .topSellingProducts(buildTopSellingProducts(monthStart, nextMonthStart))
                .lowStockItems(buildLowStockItems())

                // Giữ lại latestReview để frontend cũ không lỗi
                .latestReview(latestReviews.isEmpty() ? null : latestReviews.get(0))

                // Dùng cho frontend mới hiển thị 2 đánh giá
                .latestReviews(latestReviews)

                .build();
    }

    private List<DashboardResponse.RevenueChartItem> buildRevenueLast7Days(LocalDate today) {
        List<DashboardResponse.RevenueChartItem> items = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            BigDecimal revenue = orderRepository.sumRevenueByStatusAndDateRange(
                    OrderStatus.DELIVERED,
                    start,
                    end
            );

            items.add(
                    DashboardResponse.RevenueChartItem.builder()
                            .date(date)
                            .label(getVietnameseDayLabel(date.getDayOfWeek()))
                            .revenue(nullToZero(revenue))
                            .build()
            );
        }

        return items;
    }

    private List<DashboardResponse.OrderStatusStat> buildOrderStatusStats() {
        List<DashboardResponse.OrderStatusStat> items = new ArrayList<>();

        items.add(buildOrderStatusItem(OrderStatus.PENDING, "Chờ xử lý"));
        items.add(buildOrderStatusItem(OrderStatus.CONFIRMED, "Đã xác nhận"));
        items.add(buildOrderStatusItem(OrderStatus.SHIPPING, "Đang giao"));
        items.add(buildOrderStatusItem(OrderStatus.DELIVERED, "Hoàn thành"));
        items.add(buildOrderStatusItem(OrderStatus.CANCELLED, "Đã hủy"));

        return items;
    }

    private DashboardResponse.OrderStatusStat buildOrderStatusItem(OrderStatus status, String label) {
        return DashboardResponse.OrderStatusStat.builder()
                .status(status.name())
                .label(label)
                .count(orderRepository.countByStatus(status))
                .build();
    }

    private List<DashboardResponse.RecentOrderItem> buildRecentOrders() {
        return orderRepository.findTop4ByOrderByCreatedDateDesc()
                .stream()
                .map(order -> DashboardResponse.RecentOrderItem.builder()
                        .orderID(order.getOrderID())
                        .orderCode(order.getOrderCode())
                        .customerName(order.getCustomerName())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus() != null ? order.getStatus().name() : null)
                        .statusLabel(getOrderStatusLabel(order.getStatus()))
                        .createdDate(order.getCreatedDate())
                        .build()
                )
                .toList();
    }

    private DashboardResponse.TodayOrderSummary buildTodayOrderSummary(
            LocalDateTime todayStart,
            LocalDateTime tomorrowStart
    ) {
        Long pendingOrders = orderRepository.countByStatusAndCreatedDateBetween(
                OrderStatus.PENDING,
                todayStart,
                tomorrowStart
        );

        Long shippingOrders = orderRepository.countByStatusAndCreatedDateBetween(
                OrderStatus.SHIPPING,
                todayStart,
                tomorrowStart
        );

        BigDecimal todayRevenue = orderRepository.sumRevenueByStatusAndDateRange(
                OrderStatus.DELIVERED,
                todayStart,
                tomorrowStart
        );

        return DashboardResponse.TodayOrderSummary.builder()
                .pendingOrders(pendingOrders)
                .shippingOrders(shippingOrders)
                .todayRevenue(nullToZero(todayRevenue))
                .build();
    }

    private List<DashboardResponse.MembershipOverviewItem> buildMembershipOverview(Long totalUsers) {
        List<MembershipLevel> levels = membershipLevelRepository.findAllVisibleLevels();

        return levels.stream()
                .map(level -> {
                    Long userCount = userRepository.countByLevelId(level.getLevelID());
                    int percent = 0;

                    if (totalUsers != null && totalUsers > 0) {
                        percent = (int) Math.round((userCount * 100.0) / totalUsers);
                    }

                    return DashboardResponse.MembershipOverviewItem.builder()
                            .levelID(level.getLevelID())
                            .levelName(level.getLevelName())
                            .userCount(userCount)
                            .percent(percent)
                            .build();
                })
                .toList();
    }

    private List<DashboardResponse.RecentUpgradedMemberItem> buildRecentUpgradedMembers() {
        return membershipHistoryRepository.findTop5ByOrderByUpdatedAtDesc()
                .stream()
                .map(history -> DashboardResponse.RecentUpgradedMemberItem.builder()
                        .userID(history.getUserID())
                        .fullName(
                                history.getUser() != null
                                        ? history.getUser().getFullName()
                                        : "Khách hàng"
                        )
                        .fromLevel(
                                history.getFromLevel() != null
                                        ? history.getFromLevel().getLevelName()
                                        : "Chưa có hạng"
                        )
                        .toLevel(
                                history.getToLevel() != null
                                        ? history.getToLevel().getLevelName()
                                        : "Không rõ"
                        )
                        .updatedAt(history.getUpdatedAt())
                        .build()
                )
                .toList();
    }

    private List<DashboardResponse.TopSellingProductItem> buildTopSellingProducts(
            LocalDateTime monthStart,
            LocalDateTime nextMonthStart
    ) {
        List<Object[]> rows = orderDetailRepository.getTopSellingProducts(monthStart, nextMonthStart);

        return rows.stream()
                .limit(3)
                .map(row -> DashboardResponse.TopSellingProductItem.builder()
                        .productID(toInteger(row[0]))
                        .productName(toStringValue(row[1]))
                        .soldQuantity(toLong(row[2]))
                        .revenue(toBigDecimal(row[3]))
                        .build()
                )
                .toList();
    }

    private List<DashboardResponse.LowStockProductItem> buildLowStockItems() {
        List<Object[]> rows = productRepository.findLowStockProducts(5);

        return rows.stream()
                .limit(5)
                .map(row -> DashboardResponse.LowStockProductItem.builder()
                        .productID(toInteger(row[0]))
                        .productName(toStringValue(row[1]))
                        .quantity(toInteger(row[2]))
                        .build()
                )
                .toList();
    }

    private List<DashboardResponse.LatestReviewItem> buildLatestReviews(int limit) {
        return feedbackRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .limit(limit)
                .map(feedback -> {
                    String productName = productRepository.findById(feedback.getProductID())
                            .map(Product::getName)
                            .orElse("Không rõ sản phẩm");

                    return DashboardResponse.LatestReviewItem.builder()
                            .feedbackID(feedback.getFeedbackID())
                            .productID(feedback.getProductID())
                            .productName(productName)
                            .rating(feedback.getRating())
                            .comment(feedback.getComment())
                            .createdDate(feedback.getCreatedDate())
                            .build();
                })
                .toList();
    }

    private String getOrderStatusLabel(OrderStatus status) {
        if (status == null) return "Không rõ";

        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao";
            case DELIVERED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    private String getVietnameseDayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        return ((Number) value).intValue();
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        return null;
    }
}