package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByFullNameContainingIgnoreCase(String name);

    List<User> findByLevelId(Integer levelId);

    boolean existsByLevelId(Integer levelId);

    long countByLevelId(Integer levelId);

    // Lấy tất cả user chưa bị xóa mềm
    @Query("""
            SELECT u
            FROM User u
            WHERE u.deleted = false OR u.deleted IS NULL
            ORDER BY u.userId DESC
            """)
    List<User> findAllVisibleUsers();

    // Tìm user theo tên, chỉ lấy user chưa bị xóa mềm
    @Query("""
            SELECT u
            FROM User u
            WHERE (u.deleted = false OR u.deleted IS NULL)
              AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY u.userId DESC
            """)
    List<User> searchVisibleUsersByFullName(String keyword);

    // Tìm kiếm rộng hơn cho admin: username, fullName, email, phone
    @Query("""
            SELECT u
            FROM User u
            WHERE (u.deleted = false OR u.deleted IS NULL)
              AND (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY u.userId DESC
            """)
    List<User> searchVisibleUsers(String keyword);
}