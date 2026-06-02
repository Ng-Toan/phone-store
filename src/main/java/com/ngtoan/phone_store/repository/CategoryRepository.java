package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}