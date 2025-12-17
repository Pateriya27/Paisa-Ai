package com.paisa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private String id;
    private BigDecimal amount;
    private LocalDateTime lastAlertSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

