package com.paisa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private BigDecimal totalBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal budgetAmount;
    private BigDecimal budgetSpent;
    private List<AccountDto> accounts;
    private List<TransactionDto> recentTransactions;
    private Map<String, BigDecimal> expensesByCategory;
}

