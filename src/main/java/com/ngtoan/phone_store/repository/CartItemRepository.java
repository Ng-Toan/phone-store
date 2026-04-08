package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartIDAndProductID(Integer cartID, Integer productID);

    List<CartItem> findByCartID(Integer cartID);

    void deleteAllByCartID(Integer cartId);

}