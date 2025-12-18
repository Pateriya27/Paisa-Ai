package com.paisa.dto;

import com.paisa.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionDto {
    private String id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private String category;
    private Boolean isRecurring;
    private Transaction.RecurringInterval recurringInterval;
    private Transaction.TransactionStatus status;
    private String accountId;
    private String accountName;
    private String userId;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

