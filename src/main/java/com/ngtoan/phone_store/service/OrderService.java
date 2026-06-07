package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.CheckoutItemRequest;
import com.ngtoan.phone_store.dto.request.CheckoutRequest;
import com.ngtoan.phone_store.dto.response.OrderAdminResponse;
import com.ngtoan.phone_store.dto.response.OrderDetailResponse;
import com.ngtoan.phone_store.dto.response.OrderResponse;
import com.ngtoan.phone_store.dto.response.PaymentResponse;
import com.ngtoan.phone_store.entity.*;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.OutOfStockException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    // ===== PAYMENT =====
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    // ===== MEMBERSHIP =====
    private final MembershipLevelService membershipLevelService;

    // ===== NOTIFICATION =====
    private final NotificationService notificationService;

    // ===== CHECKOUT =====
    public OrderResponse placeOrder(String username, CheckoutRequest request) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Integer userID = user.getUserId();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("No items selected");
        }

        // ===== VALIDATE PAYMENT METHOD =====
        PaymentMethod method;

        try {
            method = request.getPaymentMethod() == null
                    ? PaymentMethod.COD
                    : PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid payment method");
        }

        BigDecimal subTotal = BigDecimal.ZERO;

        // ===== CHECK STOCK + TÍNH TOTAL =====
        for (CheckoutItemRequest item : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(item.getProductID());

            if (product == null) {
                throw new ResourceNotFoundException(
                        "Product not found with id: " + item.getProductID()
                );
            }

            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }

            if (product.getQuantity() < item.getQuantity()) {
                throw new OutOfStockException(product.getName() + " đã hết hàng");
            }

            BigDecimal price = product.getPromotionPrice() != null
                    ? product.getPromotionPrice()
                    : product.getPrice();

            subTotal = subTotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        // ===== MEMBERSHIP DISCOUNT =====
        // Cập nhật/lấy hạng hiện tại của user dựa trên TotalSpent hiện tại
        MembershipLevel currentLevel =
                membershipLevelService.updateUserMembershipLevel(userID);

        BigDecimal discountPercent = BigDecimal.ZERO;

        if (currentLevel != null && currentLevel.getDiscountPercent() != null) {
        discountPercent = currentLevel.getDiscountPercent();
        }

        BigDecimal discountAmount = subTotal
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        BigDecimal finalTotal = subTotal.subtract(discountAmount);

        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
        finalTotal = BigDecimal.ZERO;
        }

        // ===== CREATE ORDER =====
        Order order = Order.builder()
                .userID(userID)
                .orderCode(generateOrderCode())
                .createdDate(LocalDateTime.now())
                .subTotal(subTotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .totalAmount(finalTotal)
                .status(OrderStatus.PENDING)
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .paymentMethod(method)
                .build();

        orderRepository.save(order);

        // ===== CREATE PAYMENT =====
        Payment payment = paymentService.createPayment(order.getOrderID(), method);

        payment.setAmount(finalTotal);
        paymentRepository.save(payment);

        // ===== CREATE ORDER DETAIL =====
        for (CheckoutItemRequest item : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(item.getProductID());

            BigDecimal price = product.getPromotionPrice() != null
                    ? product.getPromotionPrice()
                    : product.getPrice();

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderDetail detail = OrderDetail.builder()
                    .orderID(order.getOrderID())
                    .productID(product.getProductID())
                    .productName(product.getName())
                    .image(product.getImage())
                    .quantity(item.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            orderDetailRepository.save(detail);
        }

        // ===== CLEAR CART =====
        clearPurchasedItemsFromCart(userID, request);

        // ===== NOTIFICATION: USER ĐẶT HÀNG -> BÁO ADMIN =====
        notificationService.notifyAdmin(
                "Có đơn hàng mới",
                "Khách hàng " + order.getCustomerName()
                        + " vừa đặt đơn hàng " + order.getOrderCode(),
                "NEW_ORDER",
                order.getOrderID()
        );

        // ===== RESPONSE =====
        OrderResponse response = new OrderResponse();

        response.setOrderID(order.getOrderID());
        response.setOrderCode(order.getOrderCode());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setCreatedDate(order.getCreatedDate());

        PaymentResponse paymentResponse = new PaymentResponse();

        paymentResponse.setPaymentID(payment.getPaymentID());
        paymentResponse.setPaymentCode(payment.getPaymentCode());
        paymentResponse.setOrderCode(order.getOrderCode());
        paymentResponse.setMethod(payment.getMethod().name());
        paymentResponse.setStatus(payment.getStatus().name());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setPaymentDate(payment.getPaymentDate());
        paymentResponse.setTransactionCode(payment.getTransactionCode());

        response.setPayment(paymentResponse);

        return response;
    }

    // ===== DEDUCT STOCK =====
    public void deductStock(Integer orderID) {

        List<OrderDetail> details = orderDetailRepository.findByOrderID(orderID);

        for (OrderDetail detail : details) {

            Product product = productRepository.findByIdForUpdate(detail.getProductID());

            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            if (product.getQuantity() < detail.getQuantity()) {
                throw new OutOfStockException(product.getName() + " out of stock");
            }

            product.setQuantity(product.getQuantity() - detail.getQuantity());

            productRepository.save(product);
        }
    }

    // ===== RESTORE STOCK =====
    public void restoreStock(Integer orderID) {

        List<OrderDetail> details = orderDetailRepository.findByOrderID(orderID);

        for (OrderDetail detail : details) {

            Product product = productRepository.findByIdForUpdate(detail.getProductID());

            if (product == null) {
                continue;
            }

            product.setQuantity(product.getQuantity() + detail.getQuantity());

            productRepository.save(product);
        }
    }

    // ===== VALIDATE STATUS FLOW =====
    private void validateOrderStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {

            case PENDING:
                if (
                        newStatus != OrderStatus.CONFIRMED
                        && newStatus != OrderStatus.CANCELLED
                ) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;

            case CONFIRMED:
                if (
                        newStatus != OrderStatus.SHIPPING
                        && newStatus != OrderStatus.CANCELLED
                ) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;

            case SHIPPING:
                if (
                        newStatus != OrderStatus.DELIVERED
                        && newStatus != OrderStatus.CANCELLED
                ) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;

            case DELIVERED:
            case CANCELLED:
                throw new BadRequestException("Cannot change completed order");
        }
    }

    // ===== ADMIN UPDATE ORDER =====
    public OrderAdminResponse updateOrderStatus(
        Integer orderID,
        OrderStatus status
) {

    Order order = orderRepository.findById(orderID)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Order not found with id: " + orderID
                    )
            );

    OrderStatus oldStatus = order.getStatus();

    // ===== VALIDATE FLOW =====
    validateOrderStatusTransition(oldStatus, status);

    Payment payment = paymentRepository.findByOrderID(orderID).orElse(null);

    // ===== ADMIN XÁC NHẬN ĐƠN =====
    if (
            status == OrderStatus.CONFIRMED
            && oldStatus == OrderStatus.PENDING
    ) {

        /*
         * COD: được xác nhận luôn.
         * ONLINE: chỉ được xác nhận khi Payment SUCCESS.
         */
        if (
                payment != null
                && payment.getMethod() != PaymentMethod.COD
                && payment.getStatus() != PaymentStatus.SUCCESS
        ) {
            throw new BadRequestException(
                    "Đơn hàng online chưa thanh toán thành công, không thể xác nhận"
            );
        }

        deductStock(orderID);
    }

    // ===== ADMIN HỦY ĐƠN =====
    if (status == OrderStatus.CANCELLED) {

        /*
         * Nếu đơn đã CONFIRMED hoặc SHIPPING thì trước đó đã trừ kho.
         * Khi hủy phải cộng lại tồn kho.
         */
        if (
                oldStatus == OrderStatus.CONFIRMED
                || oldStatus == OrderStatus.SHIPPING
        ) {
            restoreStock(orderID);
        }

        if (payment != null) {

            /*
             * Nếu đơn online đã thanh toán thành công.
             * Khi admin hủy thì chuyển sang hoàn tiền.
             */
            if (
                    payment.getMethod() != PaymentMethod.COD
                    && payment.getStatus() == PaymentStatus.SUCCESS
            ) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setNote(
                        "Đơn online đã thanh toán nhưng bị hủy, cần hoàn tiền thủ công"
                );
                paymentRepository.save(payment);
            }

            /*
             * Nếu đơn COD đang chờ thanh toán.
             * Khi hủy thì chuyển sang thanh toán thất bại.
             */
            else if (
                    payment.getMethod() == PaymentMethod.COD
                    && payment.getStatus() == PaymentStatus.PENDING
            ) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNote("Đơn COD đã bị hủy");
                paymentRepository.save(payment);
            }

            /*
             * Nếu đơn online chưa thanh toán mà bị hủy.
             * Ví dụ user/admin hủy khi payment còn PENDING.
             */
            else if (
                    payment.getMethod() != PaymentMethod.COD
                    && payment.getStatus() == PaymentStatus.PENDING
            ) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNote("Đơn online đã bị hủy khi chưa thanh toán");
                paymentRepository.save(payment);
            }
        }
    }

    // ===== UPDATE STATUS =====
    order.setStatus(status);

    orderRepository.save(order);

    // ===== NOTIFICATION: ADMIN ĐỔI TRẠNG THÁI -> BÁO USER =====
    if (oldStatus != status) {
        notifyUserByOrderStatus(order, status);
    }

    /*
     * Chỉ sync payment tự động khi đơn KHÔNG bị hủy.
     * Vì trường hợp CANCELLED đã xử lý payment riêng ở trên:
     * - Online SUCCESS -> REFUNDED
     * - COD PENDING -> FAILED
     * - Online PENDING -> FAILED
     */
    if (status != OrderStatus.CANCELLED) {
        paymentService.syncPaymentWithOrder(order);
    }

    // ===== CHỈ CỘNG TOTAL KHI DELIVERED / SYNC LẠI KHI CANCELLED =====
    if (
            status == OrderStatus.DELIVERED
            || status == OrderStatus.CANCELLED
    ) {

        syncUserTotalSpent(order.getUserID());

        membershipLevelService.updateUserMembershipLevel(
                order.getUserID()
        );
    }

    return toAdminResponse(order);
}

    // ===== CLEAR CART =====
    private void clearPurchasedItemsFromCart(
            Integer userID,
            CheckoutRequest request
    ) {

        Cart cart = cartRepository.findByUserID(userID).orElse(null);

        if (cart == null) return;

        for (CheckoutItemRequest item : request.getItems()) {

            cartItemRepository
                    .findByCartIDAndProductID(
                            cart.getCartID(),
                            item.getProductID()
                    )
                    .ifPresent(cartItemRepository::delete);
        }
    }

    // ===== GEN ORDER CODE =====
    private String generateOrderCode() {
        return "OD" + System.currentTimeMillis();
    }

    // ===== ADMIN VIEW =====
    public List<OrderAdminResponse> getAllOrdersForAdmin() {

        return orderRepository.findAll()
                .stream()
                .sorted(
                        (a, b) ->
                                b.getCreatedDate()
                                        .compareTo(a.getCreatedDate())
                )
                .map(this::toAdminResponse)
                .toList();
    }

    public OrderAdminResponse getOrderDetailForAdmin(Integer orderID) {

        Order order = orderRepository.findById(orderID)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + orderID
                        )
                );

        return toAdminResponse(order);
    }

    // ===== USER VIEW =====
    public List<OrderAdminResponse> getMyOrders(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return orderRepository.findByUserID(user.getUserId())
                .stream()
                .sorted(
                        (a, b) ->
                                b.getCreatedDate()
                                        .compareTo(a.getCreatedDate())
                )
                .map(this::toAdminResponse)
                .toList();
    }

    // ===== MAP RESPONSE =====
    private OrderAdminResponse toAdminResponse(Order order) {

        List<OrderDetail> details =
                orderDetailRepository.findByOrderID(order.getOrderID());

        List<OrderDetailResponse> itemResponses =
                details.stream()
                        .map(detail -> {

                            OrderDetailResponse item = new OrderDetailResponse();

                            item.setOrderDetailID(detail.getOrderDetailID());
                            item.setProductID(detail.getProductID());
                            item.setProductName(detail.getProductName());
                            item.setImage(detail.getImage());
                            item.setQuantity(detail.getQuantity());
                            item.setPrice(detail.getPrice());
                            item.setSubtotal(detail.getSubtotal());

                            return item;
                        })
                        .toList();

        OrderAdminResponse response = new OrderAdminResponse();

        response.setOrderID(order.getOrderID());
        response.setUserID(order.getUserID());
        response.setOrderCode(order.getOrderCode());
        response.setCreatedDate(order.getCreatedDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());

        response.setCustomerName(order.getCustomerName());
        response.setPhone(order.getPhone());
        response.setAddress(order.getAddress());
        response.setNote(order.getNote());

        response.setPaymentMethod(order.getPaymentMethod().name());

        response.setItems(itemResponses);

        // ===== PAYMENT =====
        Payment payment =
                paymentRepository.findByOrderID(order.getOrderID()).orElse(null);

        if (payment != null) {

            PaymentResponse paymentResponse = new PaymentResponse();

            paymentResponse.setPaymentID(payment.getPaymentID());
            paymentResponse.setPaymentCode(payment.getPaymentCode());
            paymentResponse.setOrderCode(order.getOrderCode());
            paymentResponse.setMethod(payment.getMethod().name());
            paymentResponse.setStatus(payment.getStatus().name());
            paymentResponse.setAmount(payment.getAmount());
            paymentResponse.setPaymentDate(payment.getPaymentDate());
            paymentResponse.setTransactionCode(payment.getTransactionCode());

            response.setPayment(paymentResponse);

        } else {
            response.setPayment(null);
        }

        return response;
    }

    // ===== SYNC USER TOTAL =====
    private void syncUserTotalSpent(Integer userID) {

        BigDecimal totalSpent =
                orderRepository.calculateTotalSpentByUserID(userID);

        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        User user = userRepository.findById(userID)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userID
                        )
                );

        user.setTotalSpent(totalSpent);

        userRepository.save(user);
    }

    // ===== USER CANCEL ORDER =====
public OrderAdminResponse cancelMyOrder(String username, Integer orderID) {

    User user = userRepository.findByUsername(username);

    if (user == null) {
        throw new ResourceNotFoundException("User not found");
    }

    Order order = orderRepository.findById(orderID)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Order not found with id: " + orderID
                    )
            );

    // Chỉ được hủy đơn của chính mình
    if (order.getUserID() == null || !order.getUserID().equals(user.getUserId())) {
        throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
    }

    // User chỉ được hủy khi đơn còn đang chờ xác nhận
    if (order.getStatus() != OrderStatus.PENDING) {
        throw new BadRequestException(
                "Chỉ có thể hủy đơn khi đơn hàng đang chờ xác nhận"
        );
    }

    Payment payment = paymentRepository.findByOrderID(orderID).orElse(null);

    // Nếu đơn online đã thanh toán thành công thì chuyển sang REFUNDED
    if (
            payment != null
            && payment.getMethod() != PaymentMethod.COD
            && payment.getStatus() == PaymentStatus.SUCCESS
    ) {
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setNote(
                "Khách hàng đã hủy đơn sau khi thanh toán, cần hoàn tiền thủ công"
        );
        paymentRepository.save(payment);
    }

    // Nếu COD hoặc online chưa thanh toán thì payment FAILED
    if (
            payment != null
            && payment.getStatus() == PaymentStatus.PENDING
    ) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setNote("Khách hàng đã hủy đơn");
        paymentRepository.save(payment);
    }

    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);

    // ===== NOTIFICATION: USER HỦY ĐƠN -> BÁO ADMIN =====
    notificationService.notifyAdmin(
            "Khách hàng đã hủy đơn",
            "Khách hàng " + order.getCustomerName()
                    + " đã hủy đơn hàng " + order.getOrderCode(),
            "USER_CANCELLED_ORDER",
            order.getOrderID()
    );

    // Đồng bộ lại tổng chi tiêu và hạng cho chắc
    syncUserTotalSpent(order.getUserID());

    membershipLevelService.updateUserMembershipLevel(
            order.getUserID()
    );

    return toAdminResponse(order);
}
    // ===== NOTIFICATION HELPER =====
    private void notifyUserByOrderStatus(Order order, OrderStatus status) {

        if (status == OrderStatus.CONFIRMED) {
            notificationService.notifyUser(
                    order.getUserID(),
                    "Đơn hàng đã được xác nhận",
                    "Đơn hàng " + order.getOrderCode()
                            + " của bạn đã được admin xác nhận.",
                    "ORDER_CONFIRMED",
                    order.getOrderID()
            );
            return;
        }

        if (status == OrderStatus.SHIPPING) {
            notificationService.notifyUser(
                    order.getUserID(),
                    "Đơn hàng đang được giao",
                    "Đơn hàng " + order.getOrderCode()
                            + " đang được giao đến bạn.",
                    "ORDER_SHIPPING",
                    order.getOrderID()
            );
            return;
        }

        if (status == OrderStatus.DELIVERED) {
            notificationService.notifyUser(
                    order.getUserID(),
                    "Giao hàng thành công",
                    "Đơn hàng " + order.getOrderCode()
                            + " đã được giao thành công.",
                    "ORDER_DELIVERED",
                    order.getOrderID()
            );
            return;
        }

        if (status == OrderStatus.CANCELLED) {
            notificationService.notifyUser(
                    order.getUserID(),
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng " + order.getOrderCode()
                            + " đã bị hủy.",
                    "ORDER_CANCELLED",
                    order.getOrderID()
            );
        }
    }

}