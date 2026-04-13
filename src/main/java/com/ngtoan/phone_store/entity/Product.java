package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer productID;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    String name;

    @Builder.Default
    private Integer status = 1;

    @Size(max = 255)
    String image;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal price;

    BigDecimal promotionPrice;

    @DecimalMin(value = "0.0")
    BigDecimal vat;

    @Min(0)
    @Builder.Default
    Integer quantity = 0;

    @Min(0)
    @Builder.Default
    Integer warranty = 12; 

    @Builder.Default
    Boolean isHot = false;

    @Size(max = 500)
    String description;

    @Builder.Default
    Integer viewCount = 0;

    @NotNull(message = "Category is required")
    Integer categoryID;

    Integer brandID;

    Integer supplierID;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdDate;

    @UpdateTimestamp
    LocalDateTime updatedDate;
}