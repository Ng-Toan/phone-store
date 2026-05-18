package com.ngtoan.phone_store.repository;

import com.ngtoan.phone_store.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findAllByOrderByCreatedDateDesc();

    List<Feedback> findByProductIDOrderByCreatedDateDesc(Integer productID);

    List<Feedback> findByProductIDAndRatingOrderByCreatedDateDesc(Integer productID, Integer rating);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.productID = :productID AND f.rating IS NOT NULL")
    Double getAverageRatingByProductID(@Param("productID") Integer productID);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.productID = :productID AND f.rating = :rating")
    Long countByProductIDAndRating(@Param("productID") Integer productID, @Param("rating") Integer rating);

    List<Feedback> findByUserID(Integer userID);

    @Query(value = """
                SELECT TOP 1
                    f.FeedbackID,
                    f.ProductID,
                    p.Name,
                    f.Rating,
                    f.Comment,
                    f.CreatedDate
                FROM Feedback f
                LEFT JOIN Product p ON f.ProductID = p.ProductID
                ORDER BY f.CreatedDate DESC
            """, nativeQuery = true)
    Object findLatestReviewWithProductName();

    List<Feedback> findTop2ByOrderByCreatedDateDesc();
}