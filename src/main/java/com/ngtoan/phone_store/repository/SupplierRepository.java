package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

       long countByStatus(String status);

    List<Supplier> findByStatus(String status);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Supplier s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCaseCustom(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Supplier s WHERE LOWER(s.name) = LOWER(:name) AND s.supplierID <> :supplierID")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name,
                                           @Param("supplierID") Integer supplierID);
                                           
}
