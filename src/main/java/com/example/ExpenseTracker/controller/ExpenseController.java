package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.entity.ExpenseCategory;
import com.example.ExpenseTracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // Create new expense
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(response);
    }

    // Get all expenses with pagination and sorting
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExpenseResponse> expenses = expenseService.getAllExpenses(pageable);
        return ResponseEntity.ok(expenses);
    }

    // Get expenses by category
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ExpenseResponse>> getExpensesByCategory(
            @PathVariable ExpenseCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByCategory(category, pageable);
        return ResponseEntity.ok(expenses);
    }

    // Get expenses by date range
    @GetMapping("/date-range")
    public ResponseEntity<Page<ExpenseResponse>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(
                startDate,
                endDate,
                pageable
        );
        return ResponseEntity.ok(expenses);
    }

    // Get expenses by category and date range
    @GetMapping("/filter")
    public ResponseEntity<Page<ExpenseResponse>> getExpensesByCategoryAndDateRange(
            @RequestParam ExpenseCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByCategoryAndDateRange(
                category,
                startDate,
                endDate,
                pageable
        );
        return ResponseEntity.ok(expenses);
    }

    // Update expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // Get expense summary
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummary> getExpenseSummary() {
        ExpenseSummary summary = expenseService.getExpenseSummary();
        return ResponseEntity.ok(summary);
    }

    // Get expense summary by date range
    @GetMapping("/summary/date-range")
    public ResponseEntity<ExpenseSummary> getExpenseSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ExpenseSummary summary = expenseService.getExpenseSummaryByDateRange(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    // Get expenses by category summary
    @GetMapping("/summary/by-category")
    public ResponseEntity<List<CategorySummary>> getExpensesByCategory() {
        List<CategorySummary> summary = expenseService.getExpensesByCategory();
        return ResponseEntity.ok(summary);
    }

    // Get expenses by category summary for date range
    @GetMapping("/summary/by-category/date-range")
    public ResponseEntity<List<CategorySummary>> getExpensesByCategoryInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<CategorySummary> summary = expenseService.getExpensesByCategoryInDateRange(
                startDate,
                endDate
        );
        return ResponseEntity.ok(summary);
    }
}
