package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.ImportDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, Integer> {
    List<ImportDetail> findByImportID(Integer importID);
}
