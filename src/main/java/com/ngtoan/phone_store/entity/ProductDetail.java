package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ProductDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer detailID;

    @Column(name = "productID", insertable = false, updatable = false)
    Integer productID;

    String ram;
    String storage;
    String cpu;
    String screen;
    String battery;
    String camera;
    
    @Column(name = "OS")
    String os;

    @Column(name = "ChargingSpeed")
    String chargingSpeed;

    @Column(name = "Connectivity")
    String connectivity;

@JsonIgnore
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "ProductID")
Product product;

}