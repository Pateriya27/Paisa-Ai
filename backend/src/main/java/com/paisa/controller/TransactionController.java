package com.paisa.controller;

import com.paisa.dto.TransactionDto;
import com.paisa.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto transactionDto,
                                                            Authentication authentication) {
        String userId = authentication.getName();
        try {
            TransactionDto created = transactionService.createTransaction(transactionDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getUserTransactions(Authentication authentication) {
        String userId = authentication.getName();
        List<TransactionDto> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDto>> getAccountTransactions(@PathVariable String accountId,
                                                                      Authentication authentication) {
        String userId = authentication.getName();
        try {
            List<TransactionDto> transactions = transactionService.getAccountTransactions(accountId, userId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String id,
                                                        Authentication authentication) {
        String userId = authentication.getName();
        try {
            TransactionDto transaction = transactionService.getTransactionById(id, userId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(@PathVariable String id,
                                                           @RequestBody TransactionDto transactionDto,
                                                           Authentication authentication) {
        String userId = authentication.getName();
        try {
            TransactionDto updated = transactionService.updateTransaction(id, transactionDto, userId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id,
                                                Authentication authentication) {
        String userId = authentication.getName();
        try {
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

