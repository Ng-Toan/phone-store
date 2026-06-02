package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.ChatbotPolicyResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotPolicyService {

    public ChatbotPolicyResponseDTO queryPolicy(String query) {
        String q = query == null ? "" : query.toLowerCase();

        if (q.contains("bảo hành") || q.contains("bao hanh")) {
            return new ChatbotPolicyResponseDTO(
                    "Sản phẩm tại CellCentral được bảo hành theo thời gian bảo hành ghi trên từng sản phẩm. Khi cần bảo hành, bạn nên cung cấp thông tin đơn hàng hoặc hóa đơn để nhân viên kiểm tra nhanh hơn.",
                    List.of("Chính sách bảo hành CellCentral")
            );
        }

        if (q.contains("đổi trả") || q.contains("doi tra") || q.contains("trả hàng") || q.contains("tra hang")) {
            return new ChatbotPolicyResponseDTO(
                    "Với nhu cầu đổi trả, bạn cần liên hệ nhân viên để kiểm tra tình trạng sản phẩm, thời gian mua hàng và điều kiện đổi trả. CellCentral sẽ hỗ trợ dựa trên chính sách hiện hành.",
                    List.of("Chính sách đổi trả CellCentral")
            );
        }

        if (q.contains("giao hàng") || q.contains("ship") || q.contains("vận chuyển") || q.contains("van chuyen")) {
            return new ChatbotPolicyResponseDTO(
                    "CellCentral hỗ trợ giao hàng theo thông tin nhận hàng của bạn. Thời gian giao hàng phụ thuộc vào khu vực, tình trạng sản phẩm và trạng thái xử lý đơn hàng.",
                    List.of("Chính sách giao hàng CellCentral")
            );
        }

        if (q.contains("thanh toán") || q.contains("thanh toan") || q.contains("payment") || q.contains("chuyển khoản") || q.contains("chuyen khoan")) {
            return new ChatbotPolicyResponseDTO(
                    "Bạn có thể thanh toán theo các phương thức được website hỗ trợ khi đặt hàng. Nếu giao dịch online gặp lỗi, bạn nên liên hệ nhân viên để kiểm tra trạng thái thanh toán.",
                    List.of("Hướng dẫn thanh toán CellCentral")
            );
        }

        if (q.contains("mua hàng") || q.contains("mua hang") || q.contains("đặt hàng") || q.contains("dat hang")) {
            return new ChatbotPolicyResponseDTO(
                    "Để mua hàng, bạn chọn sản phẩm, thêm vào giỏ hàng, kiểm tra thông tin nhận hàng và chọn phương thức thanh toán phù hợp. Sau khi đặt hàng, hệ thống sẽ ghi nhận đơn để xử lý.",
                    List.of("Hướng dẫn mua hàng CellCentral")
            );
        }

        return new ChatbotPolicyResponseDTO(
                "Mình chưa tìm thấy chính sách phù hợp với câu hỏi này. Bạn có thể hỏi cụ thể hơn về bảo hành, đổi trả, giao hàng, thanh toán hoặc hướng dẫn mua hàng.",
                List.of("FAQ CellCentral")
        );
    }
}