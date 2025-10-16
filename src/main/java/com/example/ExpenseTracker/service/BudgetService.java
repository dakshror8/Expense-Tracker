package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.BudgetResponse;
import com.example.ExpenseTracker.dto.BudgetStatus;
import com.example.ExpenseTracker.dto.CreateBudgetRequest;
import com.example.ExpenseTracker.dto.UpdateBudgetRequest;
import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.BudgetRepository;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    // Get current authenticated user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create new budget
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User currentUser = getCurrentUser();

        // Check if budget already exists for this category, month, and year
        boolean exists = budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(
                currentUser.getId(),
                request.getCategory(),
                request.getMonth(),
                request.getYear()
        );

        if (exists) {
            throw new RuntimeException("Budget already exists for this category and period");
        }

        Budget budget = Budget.builder()
                .category(request.getCategory())
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .user(currentUser)
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        return mapToResponse(savedBudget);
    }

    // Get budget by ID
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        User currentUser = getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check if budget belongs to current user
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(budget);
    }

    // Get all budgets for current user
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        User currentUser = getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserId(currentUser.getId());
        return budgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get budgets by month and year
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByMonthAndYear(Integer month, Integer year) {
        User currentUser = getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                currentUser.getId(),
                month,
                year
        );
        return budgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Update budget
    public BudgetResponse updateBudget(Long id, UpdateBudgetRequest request) {
        User currentUser = getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check if budget belongs to current user
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }

        Budget updatedBudget = budgetRepository.save(budget);
        return mapToResponse(updatedBudget);
    }

    // Delete budget
    public void deleteBudget(Long id) {
        User currentUser = getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check if budget belongs to current user
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        budgetRepository.delete(budget);
    }

    // Get budget status for a specific category, month and year
    @Transactional(readOnly = true)
    public BudgetStatus getBudgetStatus(ExpenseCategory category, Integer month, Integer year) {
        User currentUser = getCurrentUser();

        Budget budget = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                currentUser.getId(),
                category,
                month,
                year
        ).orElseThrow(() -> new RuntimeException("Budget not found for this category and period"));

        // Calculate date range for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get total expenses for this category in this month
        BigDecimal spentAmount = expenseRepository.getTotalExpensesByUserIdAndDateRange(
                currentUser.getId(),
                startDate,
                endDate
        );

        // Filter by category
        spentAmount = expenseRepository.findByUserIdAndCategoryAndExpenseDateBetween(
                        currentUser.getId(),
                        category,
                        startDate,
                        endDate,
                        org.springframework.data.domain.Pageable.unpaged()
                ).stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);
        Double percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;
        boolean isExceeded = spentAmount.compareTo(budget.getAmount()) > 0;

        return BudgetStatus.builder()
                .category(category)
                .budgetAmount(budget.getAmount())
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .percentageUsed(percentageUsed)
                .isExceeded(isExceeded)
                .month(month)
                .year(year)
                .build();
    }

    // Get all budget statuses for a specific month and year
    @Transactional(readOnly = true)
    public List<BudgetStatus> getAllBudgetStatuses(Integer month, Integer year) {
        User currentUser = getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                currentUser.getId(),
                month,
                year
        );

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return budgets.stream()
                .map(budget -> {
                    BigDecimal spentAmount = expenseRepository.findByUserIdAndCategoryAndExpenseDateBetween(
                                    currentUser.getId(),
                                    budget.getCategory(),
                                    startDate,
                                    endDate,
                                    org.springframework.data.domain.Pageable.unpaged()
                            ).stream()
                            .map(expense -> expense.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);
                    Double percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                            ? spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;
                    boolean isExceeded = spentAmount.compareTo(budget.getAmount()) > 0;

                    return BudgetStatus.builder()
                            .category(budget.getCategory())
                            .budgetAmount(budget.getAmount())
                            .spentAmount(spentAmount)
                            .remainingAmount(remainingAmount)
                            .percentageUsed(percentageUsed)
                            .isExceeded(isExceeded)
                            .month(month)
                            .year(year)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Helper method to map Budget to BudgetResponse
    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
