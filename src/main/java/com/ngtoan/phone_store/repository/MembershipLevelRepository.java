package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MembershipLevelRepository extends JpaRepository<MembershipLevel, Integer> {

    boolean existsByLevelNameIgnoreCase(String levelName);

    boolean existsByLevelNameIgnoreCaseAndLevelIDNot(String levelName, Integer levelID);

    boolean existsByMinSpent(BigDecimal minSpent);

    boolean existsByMinSpentAndLevelIDNot(BigDecimal minSpent, Integer levelID);

    // Chỉ lấy các hạng chưa bị xóa mềm
    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.isDeleted = false OR m.isDeleted IS NULL
            ORDER BY m.minSpent ASC
            """)
    List<MembershipLevel> findAllVisibleLevels();

    // Lấy chi tiết hạng chưa bị xóa mềm
    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.levelID = :levelID
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    Optional<MembershipLevel> findVisibleById(Integer levelID);

    // Check trùng tên nhưng bỏ qua hạng đã xóa mềm
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE LOWER(m.levelName) = LOWER(:levelName)
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByLevelNameIgnoreCase(String levelName);

    // Check trùng tên khi sửa nhưng bỏ qua chính nó và bỏ qua hạng đã xóa mềm
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE LOWER(m.levelName) = LOWER(:levelName)
              AND m.levelID <> :levelID
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByLevelNameIgnoreCaseAndLevelIDNot(
            String levelName,
            Integer levelID
    );

    // Check trùng mức chi tiêu nhưng bỏ qua hạng đã xóa mềm
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE m.minSpent = :minSpent
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByMinSpent(BigDecimal minSpent);

    // Check trùng mức chi tiêu khi sửa nhưng bỏ qua chính nó và bỏ qua hạng đã xóa mềm
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE m.minSpent = :minSpent
              AND m.levelID <> :levelID
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByMinSpentAndLevelIDNot(
            BigDecimal minSpent,
            Integer levelID
    );

    // Tìm hạng phù hợp với tổng chi tiêu, chỉ tính hạng chưa xóa
    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.minSpent <= :totalSpent
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            ORDER BY m.minSpent DESC
            LIMIT 1
            """)
    Optional<MembershipLevel> findTopByMinSpentLessThanEqualOrderByMinSpentDesc(
            BigDecimal totalSpent
    );

    // Tìm hạng thấp nhất, chỉ tính hạng chưa xóa
    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.isDeleted = false OR m.isDeleted IS NULL
            ORDER BY m.minSpent ASC
            LIMIT 1
            """)
    Optional<MembershipLevel> findTopByOrderByMinSpentAsc();
}