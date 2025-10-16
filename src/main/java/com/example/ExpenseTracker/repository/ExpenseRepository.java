package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find expenses by user
    Page<Expense> findByUserId(Long userId, Pageable pageable);

    // Find by user and category
    Page<Expense> findByUserIdAndCategory(Long userId, ExpenseCategory category, Pageable pageable);

    // Find by user and date range
    Page<Expense> findByUserIdAndExpenseDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Find by user, category and date range
    Page<Expense> findByUserIdAndCategoryAndExpenseDateBetween(
            Long userId,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Calculate total expenses for user
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpensesByUserId(@Param("userId") Long userId);

    // Calculate total expenses by category for user
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.category = :category")
    BigDecimal getTotalExpensesByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category
    );

    // Calculate total expenses for user in date range
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Get expenses grouped by category for user
    @Query("SELECT e.category as category, COALESCE(SUM(e.amount), 0) as total " +
            "FROM Expense e WHERE e.user.id = :userId " +
            "GROUP BY e.category")
    List<Object[]> getExpensesByCategoryForUser(@Param("userId") Long userId);

    // Get expenses grouped by category for user in date range
    @Query("SELECT e.category as category, COALESCE(SUM(e.amount), 0) as total " +
            "FROM Expense e WHERE e.user.id = :userId " +
            "AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category")
    List<Object[]> getExpensesByCategoryForUserInDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
