package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.response.ChatbotPolicyResponseDTO;
import com.ngtoan.phone_store.dto.response.ChatbotProductDTO;
import com.ngtoan.phone_store.service.ChatbotPolicyService;
import com.ngtoan.phone_store.service.ChatbotProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotProductService chatbotProductService;
    private final ChatbotPolicyService chatbotPolicyService;

    public ChatbotController(ChatbotProductService chatbotProductService,
                             ChatbotPolicyService chatbotPolicyService) {
        this.chatbotProductService = chatbotProductService;
        this.chatbotPolicyService = chatbotPolicyService;
    }

    @GetMapping("/products/search")
    public List<ChatbotProductDTO> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "8") int limit
    ) {
        BigDecimal min = parsePrice(minPrice);
        BigDecimal max = parsePrice(maxPrice);

        return chatbotProductService.searchProducts(keyword, brand, min, max, limit);
    }

    @GetMapping("/products/detail")
    public List<ChatbotProductDTO> productDetail(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return chatbotProductService.productDetail(productId, keyword, limit);
    }

    @GetMapping("/products/hot-promotions")
    public List<ChatbotProductDTO> hotPromotions(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return chatbotProductService.hotPromotions(limit);
    }

    @GetMapping("/policies/query")
    public ChatbotPolicyResponseDTO queryPolicy(
            @RequestParam(required = false) String query
    ) {
        return chatbotPolicyService.queryPolicy(query);
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}