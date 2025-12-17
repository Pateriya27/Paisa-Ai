package com.paisa.service;

import com.paisa.entity.Budget;
import com.paisa.entity.Transaction;
import com.paisa.repository.BudgetRepository;
import com.paisa.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(BudgetAlertService.class);
    
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    
    public BudgetAlertService(BudgetRepository budgetRepository, 
                             TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Scheduled(cron = "0 0 9 1 * ?") // Run at 9 AM on the 1st day of every month
    @Transactional
    public void sendMonthlyBudgetAlerts() {
        logger.info("Starting monthly budget alert job");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime previousMonth = startOfMonth.minusMonths(1);
        LocalDateTime previousMonthStart = previousMonth.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        List<Budget> budgets = budgetRepository.findAll();
        
        for (Budget budget : budgets) {
            try {
                String userId = budget.getUser().getId();
                
                List<Transaction> monthlyExpenses = transactionRepository
                        .findExpensesByUserIdAndDateRange(userId, previousMonthStart, startOfMonth);
                
                BigDecimal totalSpent = monthlyExpenses.stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalSpent.compareTo(budget.getAmount()) > 0) {
                    logger.warn("Budget exceeded for user {}: Spent {} vs Budget {}", 
                            userId, totalSpent, budget.getAmount());
                    // In production, send email notification here
                    budget.setLastAlertSent(now);
                    budgetRepository.save(budget);
                }
            } catch (Exception e) {
                logger.error("Error processing budget alert for user {}", budget.getUser().getId(), e);
            }
        }
        
        logger.info("Completed monthly budget alert job");
    }
}

