package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    // Get current authenticated user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create new expense
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User currentUser = getCurrentUser();

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .user(currentUser)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponse(savedExpense);
    }

    // Get expense by ID
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Check if expense belongs to current user
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(expense);
    }

    // Get all expenses for current user with pagination
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserId(currentUser.getId(), pageable);
        return expenses.map(this::mapToResponse);
    }

    // Get expenses by category
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByCategory(ExpenseCategory category, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserIdAndCategory(
                currentUser.getId(),
                category,
                pageable
        );
        return expenses.map(this::mapToResponse);
    }

    // Get expenses by date range
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(
                currentUser.getId(),
                startDate,
                endDate,
                pageable
        );
        return expenses.map(this::mapToResponse);
    }

    // Get expenses by category and date range
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByCategoryAndDateRange(
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndExpenseDateBetween(
                currentUser.getId(),
                category,
                startDate,
                endDate,
                pageable
        );
        return expenses.map(this::mapToResponse);
    }

    // Update expense
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Check if expense belongs to current user
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Update only provided fields
        if (request.getTitle() != null) {
            expense.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToResponse(updatedExpense);
    }

    // Delete expense
    public void deleteExpense(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Check if expense belongs to current user
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        expenseRepository.delete(expense);
    }

    // Get expense summary
    @Transactional(readOnly = true)
    public ExpenseSummary getExpenseSummary() {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserId(
                currentUser.getId(),
                Pageable.unpaged()
        );

        List<Expense> expenseList = expenses.getContent();

        if (expenseList.isEmpty()) {
            return ExpenseSummary.builder()
                    .totalAmount(BigDecimal.ZERO)
                    .totalCount(0L)
                    .averageAmount(BigDecimal.ZERO)
                    .maxAmount(BigDecimal.ZERO)
                    .minAmount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal total = expenseList.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal max = expenseList.stream()
                .map(Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal min = expenseList.stream()
                .map(Expense::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal average = total.divide(
                BigDecimal.valueOf(expenseList.size()),
                2,
                RoundingMode.HALF_UP
        );

        return ExpenseSummary.builder()
                .totalAmount(total)
                .totalCount((long) expenseList.size())
                .averageAmount(average)
                .maxAmount(max)
                .minAmount(min)
                .build();
    }

    // Get expense summary by date range
    @Transactional(readOnly = true)
    public ExpenseSummary getExpenseSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        Page<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetween(
                currentUser.getId(),
                startDate,
                endDate,
                Pageable.unpaged()
        );

        List<Expense> expenseList = expenses.getContent();

        if (expenseList.isEmpty()) {
            return ExpenseSummary.builder()
                    .totalAmount(BigDecimal.ZERO)
                    .totalCount(0L)
                    .averageAmount(BigDecimal.ZERO)
                    .maxAmount(BigDecimal.ZERO)
                    .minAmount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal total = expenseList.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal max = expenseList.stream()
                .map(Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal min = expenseList.stream()
                .map(Expense::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal average = total.divide(
                BigDecimal.valueOf(expenseList.size()),
                2,
                RoundingMode.HALF_UP
        );

        return ExpenseSummary.builder()
                .totalAmount(total)
                .totalCount((long) expenseList.size())
                .averageAmount(average)
                .maxAmount(max)
                .minAmount(min)
                .build();
    }

    // Get expenses by category summary
    @Transactional(readOnly = true)
    public List<CategorySummary> getExpensesByCategory() {
        User currentUser = getCurrentUser();
        List<Object[]> results = expenseRepository.getExpensesByCategoryForUser(currentUser.getId());

        BigDecimal grandTotal = results.stream()
                .map(result -> (BigDecimal) result[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(result -> {
                    ExpenseCategory category = (ExpenseCategory) result[0];
                    BigDecimal total = (BigDecimal) result[1];
                    Double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return CategorySummary.builder()
                            .category(category)
                            .totalAmount(total)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Get expenses by category summary for date range
    @Transactional(readOnly = true)
    public List<CategorySummary> getExpensesByCategoryInDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        User currentUser = getCurrentUser();
        List<Object[]> results = expenseRepository.getExpensesByCategoryForUserInDateRange(
                currentUser.getId(),
                startDate,
                endDate
        );

        BigDecimal grandTotal = results.stream()
                .map(result -> (BigDecimal) result[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(result -> {
                    ExpenseCategory category = (ExpenseCategory) result[0];
                    BigDecimal total = (BigDecimal) result[1];
                    Double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return CategorySummary.builder()
                            .category(category)
                            .totalAmount(total)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Helper method to map Expense to ExpenseResponse
    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
