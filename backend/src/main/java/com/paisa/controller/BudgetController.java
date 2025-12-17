package com.paisa.controller;

import com.paisa.dto.BudgetDto;
import com.paisa.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {
    
    private final BudgetService budgetService;
    
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }
    
    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdateBudget(@RequestBody BudgetDto budgetDto,
                                                         Authentication authentication) {
        String userId = authentication.getName();
        try {
            BudgetDto budget = budgetService.createOrUpdateBudget(budgetDto, userId);
            return ResponseEntity.ok(budget);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<BudgetDto> getBudget(Authentication authentication) {
        String userId = authentication.getName();
        try {
            BudgetDto budget = budgetService.getBudgetByUserId(userId);
            return ResponseEntity.ok(budget);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteBudget(Authentication authentication) {
        String userId = authentication.getName();
        try {
            budgetService.deleteBudget(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

