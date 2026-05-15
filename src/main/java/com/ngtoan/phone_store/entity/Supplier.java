package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SupplierID")
    Integer supplierID;

    @Column(name = "Name", nullable = false, length = 100)
    String name;

    @Size(max = 20)
    @Column(name = "Phone", length = 20)
    String phone;

    @Size(max = 100)
    @Column(name = "Email", length = 100)
    String email;

    @Size(max = 255)
    @Column(name = "Address", length = 255)
    String address;

    @Builder.Default
    @Column(name = "Status", nullable = false, length = 20)
    String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "UpdatedDate")
    LocalDateTime updatedDate;
}
