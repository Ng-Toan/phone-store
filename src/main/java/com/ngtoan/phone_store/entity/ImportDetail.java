package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "ImportDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImportDetailID")
    Integer importDetailID;

    @Column(name = "ImportID", nullable = false)
    Integer importID;

    @Column(name = "ProductID", nullable = false)
    Integer productID;

    @Column(name = "Quantity", nullable = false)
    Integer quantity;

    @Column(name = "ImportPrice", nullable = false)
    BigDecimal importPrice;

    @Column(name = "SubTotal", insertable = false, updatable = false)
    BigDecimal subTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ImportID", insertable = false, updatable = false)
    ImportReceipt importReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", insertable = false, updatable = false)
    Product product;
}
