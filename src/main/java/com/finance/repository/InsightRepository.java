package com.finance.repository;

import com.finance.model.Insight;
import com.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    // Fetch insights for a user ordered by newest first
    List<Insight> findByUserOrderByCreatedAtDesc(User user);

    // Delete all old insights before regenerating new ones
    // @Modifying tells Spring this query changes data (not a SELECT)
    @Modifying
    @Query("DELETE FROM Insight i WHERE i.user = :user")
    void deleteByUser(@Param("user") User user);
}