package com.finance.repository;

import com.finance.model.Category;
import com.finance.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Used to filter categories by INCOME or EXPENSE in the frontend dropdown
    List<Category> findByType(TransactionType type);
}