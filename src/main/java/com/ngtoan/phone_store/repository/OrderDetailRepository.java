package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.OrderDetail;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {}
