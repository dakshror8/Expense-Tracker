package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Find budgets by user
    List<Budget> findByUserId(Long userId);

    // Find budget by user, category, month, and year
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            Long userId,
            ExpenseCategory category,
            Integer month,
            Integer year
    );

    // Find budgets by user and month/year
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    // Check if budget exists
    boolean existsByUserIdAndCategoryAndMonthAndYear(
            Long userId,
            ExpenseCategory category,
            Integer month,
            Integer year
    );
}
