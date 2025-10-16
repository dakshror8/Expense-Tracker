package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.BudgetResponse;
import com.example.ExpenseTracker.dto.BudgetStatus;
import com.example.ExpenseTracker.dto.CreateBudgetRequest;
import com.example.ExpenseTracker.dto.UpdateBudgetRequest;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import com.example.ExpenseTracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // Create new budget
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse response = budgetService.createBudget(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get budget by ID
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        BudgetResponse response = budgetService.getBudgetById(id);
        return ResponseEntity.ok(response);
    }

    // Get all budgets
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        List<BudgetResponse> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    // Get budgets by month and year
    @GetMapping("/period")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonthAndYear(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        List<BudgetResponse> budgets = budgetService.getBudgetsByMonthAndYear(month, year);
        return ResponseEntity.ok(budgets);
    }

    // Update budget
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete budget
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    // Get budget status for specific category
    @GetMapping("/status")
    public ResponseEntity<BudgetStatus> getBudgetStatus(
            @RequestParam ExpenseCategory category,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        BudgetStatus status = budgetService.getBudgetStatus(category, month, year);
        return ResponseEntity.ok(status);
    }

    // Get all budget statuses for a month
    @GetMapping("/status/all")
    public ResponseEntity<List<BudgetStatus>> getAllBudgetStatuses(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        List<BudgetStatus> statuses = budgetService.getAllBudgetStatuses(month, year);
        return ResponseEntity.ok(statuses);
    }
}
