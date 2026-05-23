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

    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.isDeleted = false OR m.isDeleted IS NULL
            ORDER BY m.minSpent ASC
            """)
    List<MembershipLevel> findAllVisibleLevels();

    @Query("""
            SELECT m
            FROM MembershipLevel m
            WHERE m.levelID = :levelID
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    Optional<MembershipLevel> findVisibleById(Integer levelID);

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE LOWER(m.levelName) = LOWER(:levelName)
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByLevelNameIgnoreCase(String levelName);

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

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MembershipLevel m
            WHERE m.minSpent = :minSpent
              AND (m.isDeleted = false OR m.isDeleted IS NULL)
            """)
    boolean existsVisibleByMinSpent(BigDecimal minSpent);

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

    @Query(
            value = """
                    SELECT *
                    FROM `MembershipLevel`
                    WHERE `MinSpent` <= :totalSpent
                      AND (`IsDeleted` = 0 OR `IsDeleted` IS NULL)
                    ORDER BY `MinSpent` DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<MembershipLevel> findTopByMinSpentLessThanEqualOrderByMinSpentDesc(
            BigDecimal totalSpent
    );

    @Query(
            value = """
                    SELECT *
                    FROM `MembershipLevel`
                    WHERE `IsDeleted` = 0 OR `IsDeleted` IS NULL
                    ORDER BY `MinSpent` ASC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<MembershipLevel> findTopByOrderByMinSpentAsc();
}