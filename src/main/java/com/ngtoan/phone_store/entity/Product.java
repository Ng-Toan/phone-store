package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    // Quy ước trạng thái sản phẩm
    public static final int STATUS_NGUNG_BAN = 0;
    public static final int STATUS_DANG_BAN = 1;
    public static final int STATUS_AN_SAN_PHAM = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer productID;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    String name;

    @Size(max = 255)
    String image;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;

    BigDecimal promotionPrice;

    @DecimalMin(value = "0.0", message = "VAT must be >= 0")
    BigDecimal vat;

    @Min(value = 0, message = "Quantity must be >= 0")
    @Builder.Default
    Integer quantity = 0;

    @Min(value = 0, message = "Warranty must be >= 0")
    @Builder.Default
    Integer warranty = 12;

    @Builder.Default
    Boolean isHot = false;

    // 1: Đang bán, 0: Ngừng bán, -1: Ẩn sản phẩm
    @Builder.Default
    Integer status = STATUS_DANG_BAN;

    @Size(max = 500)
    String description;

    @Builder.Default
    Integer viewCount = 0;

    @NotNull(message = "Category is required")
    Integer categoryID;

    Integer brandID;

    Integer supplierID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryID", insertable = false, updatable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brandID", insertable = false, updatable = false)
    Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierID", insertable = false, updatable = false)
    Supplier supplier;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdDate;

    @UpdateTimestamp
    LocalDateTime updatedDate;

    @JsonManagedReference
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    ProductDetail detail;
}