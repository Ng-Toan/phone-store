package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.ChatbotProductDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotProductService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ChatbotProductDTO> searchProducts(
            String keyword,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int limit
    ) {
        String sql = """
                SELECT
                    p.ProductID,
                    p.Name AS ProductName,
                    b.Name AS BrandName,
                    c.Name AS CategoryName,
                    p.Image,
                    p.Price,
                    p.PromotionPrice,
                    COALESCE(p.PromotionPrice, p.Price) AS FinalPrice,
                    p.VAT,
                    p.Quantity,
                    p.IsHot,
                    p.Description,
                    pd.RAM,
                    pd.Storage,
                    pd.CPU,
                    pd.Screen,
                    pd.Battery,
                    pd.Camera,
                    pd.OS,
                    pd.ChargingSpeed,
                    pd.Connectivity
                FROM Product p
                LEFT JOIN Brand b ON p.BrandID = b.BrandID
                LEFT JOIN Category c ON p.CategoryID = c.CategoryID
                LEFT JOIN ProductDetail pd ON p.ProductID = pd.ProductID
                WHERE p.Status = 1
                  AND COALESCE(p.Quantity, 0) > 0
                  AND (
                        :keyword = ''
                        OR p.Name LIKE CONCAT('%', :keyword, '%')
                        OR p.Description LIKE CONCAT('%', :keyword, '%')
                        OR b.Name LIKE CONCAT('%', :keyword, '%')
                        OR c.Name LIKE CONCAT('%', :keyword, '%')
                        OR pd.RAM LIKE CONCAT('%', :keyword, '%')
                        OR pd.Storage LIKE CONCAT('%', :keyword, '%')
                        OR pd.CPU LIKE CONCAT('%', :keyword, '%')
                        OR pd.Screen LIKE CONCAT('%', :keyword, '%')
                        OR pd.Battery LIKE CONCAT('%', :keyword, '%')
                        OR pd.Camera LIKE CONCAT('%', :keyword, '%')
                        OR pd.OS LIKE CONCAT('%', :keyword, '%')
                        OR pd.Connectivity LIKE CONCAT('%', :keyword, '%')
                      )
                  AND (
                        :brand = ''
                        OR b.Name LIKE CONCAT('%', :brand, '%')
                      )
                  AND (
                        :minPrice IS NULL
                        OR COALESCE(p.PromotionPrice, p.Price) >= :minPrice
                      )
                  AND (
                        :maxPrice IS NULL
                        OR COALESCE(p.PromotionPrice, p.Price) <= :maxPrice
                      )
                ORDER BY p.IsHot DESC, COALESCE(p.PromotionPrice, p.Price) ASC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("keyword", clean(keyword));
        query.setParameter("brand", clean(brand));
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);
        query.setMaxResults(safeLimit(limit, 8));

        return mapProducts(query.getResultList());
    }

    public List<ChatbotProductDTO> productDetail(Long productId, String keyword, int limit) {
        String sql = """
                SELECT
                    p.ProductID,
                    p.Name AS ProductName,
                    b.Name AS BrandName,
                    c.Name AS CategoryName,
                    p.Image,
                    p.Price,
                    p.PromotionPrice,
                    COALESCE(p.PromotionPrice, p.Price) AS FinalPrice,
                    p.VAT,
                    p.Quantity,
                    p.IsHot,
                    p.Description,
                    pd.RAM,
                    pd.Storage,
                    pd.CPU,
                    pd.Screen,
                    pd.Battery,
                    pd.Camera,
                    pd.OS,
                    pd.ChargingSpeed,
                    pd.Connectivity
                FROM Product p
                LEFT JOIN Brand b ON p.BrandID = b.BrandID
                LEFT JOIN Category c ON p.CategoryID = c.CategoryID
                LEFT JOIN ProductDetail pd ON p.ProductID = pd.ProductID
                WHERE p.Status = 1
                  AND (
                        (:productId IS NOT NULL AND p.ProductID = :productId)
                        OR
                        (:productId IS NULL AND :keyword <> '' AND p.Name LIKE CONCAT('%', :keyword, '%'))
                        OR
                        (:productId IS NULL AND :keyword = '')
                      )
                ORDER BY p.ProductID DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("productId", productId);
        query.setParameter("keyword", clean(keyword));
        query.setMaxResults(safeLimit(limit, 5));

        return mapProducts(query.getResultList());
    }

    public List<ChatbotProductDTO> hotPromotions(int limit) {
        String sql = """
                SELECT
                    p.ProductID,
                    p.Name AS ProductName,
                    b.Name AS BrandName,
                    c.Name AS CategoryName,
                    p.Image,
                    p.Price,
                    p.PromotionPrice,
                    COALESCE(p.PromotionPrice, p.Price) AS FinalPrice,
                    p.VAT,
                    p.Quantity,
                    p.IsHot,
                    p.Description,
                    pd.RAM,
                    pd.Storage,
                    pd.CPU,
                    pd.Screen,
                    pd.Battery,
                    pd.Camera,
                    pd.OS,
                    pd.ChargingSpeed,
                    pd.Connectivity
                FROM Product p
                LEFT JOIN Brand b ON p.BrandID = b.BrandID
                LEFT JOIN Category c ON p.CategoryID = c.CategoryID
                LEFT JOIN ProductDetail pd ON p.ProductID = pd.ProductID
                WHERE p.Status = 1
                  AND COALESCE(p.Quantity, 0) > 0
                  AND (
                        p.IsHot = 1
                        OR (
                            p.PromotionPrice IS NOT NULL
                            AND p.PromotionPrice < p.Price
                        )
                      )
                ORDER BY
                    p.IsHot DESC,
                    CASE
                        WHEN p.PromotionPrice IS NOT NULL AND p.PromotionPrice < p.Price
                        THEN p.Price - p.PromotionPrice
                        ELSE 0
                    END DESC,
                    COALESCE(p.PromotionPrice, p.Price) ASC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setMaxResults(safeLimit(limit, 10));

        return mapProducts(query.getResultList());
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private int safeLimit(int limit, int defaultLimit) {
        if (limit <= 0) {
            return defaultLimit;
        }

        return Math.min(limit, 20);
    }

    private List<ChatbotProductDTO> mapProducts(List<Object[]> rows) {
        List<ChatbotProductDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            ChatbotProductDTO dto = new ChatbotProductDTO();

            dto.setProductID(toLong(row[0]));
            dto.setProductName(toStringValue(row[1]));
            dto.setBrandName(toStringValue(row[2]));
            dto.setCategoryName(toStringValue(row[3]));
            dto.setImage(toStringValue(row[4]));
            dto.setPrice(toBigDecimal(row[5]));
            dto.setPromotionPrice(toBigDecimal(row[6]));
            dto.setFinalPrice(toBigDecimal(row[7]));
            dto.setVat(toBigDecimal(row[8]));
            dto.setQuantity(toInteger(row[9]));
            dto.setIsHot(toBoolean(row[10]));
            dto.setDescription(toStringValue(row[11]));

            dto.setRam(toStringValue(row[12]));
            dto.setStorage(toStringValue(row[13]));
            dto.setCpu(toStringValue(row[14]));
            dto.setScreen(toStringValue(row[15]));
            dto.setBattery(toStringValue(row[16]));
            dto.setCamera(toStringValue(row[17]));
            dto.setOs(toStringValue(row[18]));
            dto.setChargingSpeed(toStringValue(row[19]));
            dto.setConnectivity(toStringValue(row[20]));

            result.add(dto);
        }

        return result;
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }

        return ((Number) value).intValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        return new BigDecimal(value.toString());
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof Number number) {
            return number.intValue() == 1;
        }

        String text = value.toString();

        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }
}