package com.ngtoan.phone_store.scheduler;

import com.ngtoan.phone_store.entity.*;
import com.ngtoan.phone_store.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 60000) // mỗi 1 phút
    @Transactional
    public void cancelExpiredPayments() {

        LocalDateTime expiredTime =
                LocalDateTime.now().minusMinutes(3);

        List<Payment> expiredPayments =
        paymentRepository.findExpiredPendingPayments(
                PaymentStatus.PENDING,
                expiredTime
        );

        for (Payment payment : expiredPayments) {

            // bỏ qua COD
            if (payment.getMethod() == PaymentMethod.COD) {
                continue;
            }

            // update payment
            payment.setStatus(PaymentStatus.FAILED);

            // lấy order
            Order order = orderRepository.findById(payment.getOrderID())
                    .orElseThrow();

            // update order
            order.setStatus(OrderStatus.CANCELLED);

            // ===== CỘNG LẠI STOCK =====
            List<OrderDetail> details =
                    orderDetailRepository.findByOrderID(order.getOrderID());

            for (OrderDetail detail : details) {

                Product product =
                        productRepository.findByIdForUpdate(detail.getProductID());

                product.setQuantity(
                        product.getQuantity() + detail.getQuantity()
                );

                productRepository.save(product);
            }

            paymentRepository.save(payment);
            orderRepository.save(order);
        }
    }
}