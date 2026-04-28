package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserID(Integer userID);
}
