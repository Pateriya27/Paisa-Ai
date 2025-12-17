package com.paisa.service;

import com.paisa.dto.AccountDto;
import com.paisa.dto.DashboardSummaryDto;
import com.paisa.dto.TransactionDto;
import com.paisa.entity.Transaction;
import com.paisa.repository.AccountRepository;
import com.paisa.repository.BudgetRepository;
import com.paisa.repository.TransactionRepository;
import com.paisa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AccountService accountService;
    
    private final UserRepository userRepository;
    
    public DashboardService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           BudgetRepository budgetRepository,
                           AccountService accountService,
                           UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.accountService = accountService;
        this.userRepository = userRepository;
    }
    
    private String getUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
    
    public DashboardSummaryDto getDashboardSummary(String email) {
        String userId = getUserIdFromEmail(email);
        DashboardSummaryDto summary = new DashboardSummaryDto();
        
        List<AccountDto> accounts = accountService.getUserAccounts(email);
        summary.setAccounts(accounts);
        
        BigDecimal totalBalance = accounts.stream()
                .map(AccountDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalBalance(totalBalance);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        List<Transaction> monthlyTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, now);
        
        BigDecimal monthlyIncome = monthlyTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setMonthlyIncome(monthlyIncome);
        
        BigDecimal monthlyExpense = monthlyTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setMonthlyExpense(monthlyExpense);
        
        budgetRepository.findByUserId(userId).ifPresent(budget -> {
            summary.setBudgetAmount(budget.getAmount());
            summary.setBudgetSpent(monthlyExpense);
        });
        
        List<TransactionDto> recentTransactions = transactionRepository
                .findByUserIdOrderByDateDesc(userId).stream()
                .limit(10)
                .map(t -> {
                    TransactionDto dto = new TransactionDto();
                    dto.setId(t.getId());
                    dto.setType(t.getType());
                    dto.setAmount(t.getAmount());
                    dto.setDescription(t.getDescription());
                    dto.setDate(t.getDate());
                    dto.setCategory(t.getCategory());
                    dto.setAccountId(t.getAccount().getId());
                    dto.setAccountName(t.getAccount().getName());
                    return dto;
                })
                .collect(Collectors.toList());
        summary.setRecentTransactions(recentTransactions);
        
        Map<String, BigDecimal> expensesByCategory = monthlyTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
        summary.setExpensesByCategory(expensesByCategory);
        
        return summary;
    }
}

