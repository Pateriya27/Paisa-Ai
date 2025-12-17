package com.paisa.service;

import com.paisa.dto.BudgetDto;
import com.paisa.entity.Budget;
import com.paisa.entity.User;
import com.paisa.repository.BudgetRepository;
import com.paisa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    
    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }
    
    private String getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    
    @Transactional
    public BudgetDto createOrUpdateBudget(BudgetDto budgetDto, String email) {
        String userId = getUserIdFromEmail(email);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Budget> existingBudget = budgetRepository.findByUserId(userId);
        
        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setAmount(budgetDto.getAmount());
        } else {
            budget = new Budget();
            budget.setAmount(budgetDto.getAmount());
            budget.setUser(user);
        }
        
        budget = budgetRepository.save(budget);
        return convertToDto(budget);
    }
    
    public BudgetDto getBudgetByUserId(String email) {
        String userId = getUserIdFromEmail(email);
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        return convertToDto(budget);
    }
    
    @Transactional
    public void deleteBudget(String email) {
        String userId = getUserIdFromEmail(email);
        Budget budget = budgetRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }
    
    private BudgetDto convertToDto(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setLastAlertSent(budget.getLastAlertSent());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        return dto;
    }
}

