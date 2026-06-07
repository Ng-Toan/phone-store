package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Import")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImportID")
    Integer importID;

    @Column(name = "SupplierID", nullable = false)
    Integer supplierID;

    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    LocalDateTime createdDate;

    @Builder.Default
    @Column(name = "TotalAmount", nullable = false)
    BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "Note", length = 255)
    String note;

    @Builder.Default
    @Column(name = "Status", nullable = false, length = 20)
    String status = "COMPLETED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierID", insertable = false, updatable = false)
    Supplier supplier;
}
