package com.paisa.service;

import com.paisa.dto.TransactionDto;
import com.paisa.entity.Account;
import com.paisa.entity.Transaction;
import com.paisa.entity.User;
import com.paisa.repository.AccountRepository;
import com.paisa.repository.TransactionRepository;
import com.paisa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public TransactionService(TransactionRepository transactionRepository,
                             AccountRepository accountRepository,
                             UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    private String getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    
    @Transactional
    public TransactionDto createTransaction(TransactionDto transactionDto, String email) {
        String userId = getUserIdFromEmail(email);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByIdAndUserId(transactionDto.getAccountId(), userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        Transaction transaction = new Transaction();
        transaction.setType(transactionDto.getType());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setDate(transactionDto.getDate());
        transaction.setCategory(transactionDto.getCategory());
        transaction.setReceiptUrl(transactionDto.getReceiptUrl());
        transaction.setIsRecurring(transactionDto.getIsRecurring() != null ? transactionDto.getIsRecurring() : false);
        transaction.setRecurringInterval(transactionDto.getRecurringInterval());
        transaction.setStatus(transactionDto.getStatus() != null ? transactionDto.getStatus() : Transaction.TransactionStatus.COMPLETED);
        transaction.setUser(user);
        transaction.setAccount(account);
        
        transaction = transactionRepository.save(transaction);
        
        updateAccountBalance(account, transaction);
        
        return convertToDto(transaction);
    }
    
    public List<TransactionDto> getUserTransactions(String email) {
        String userId = getUserIdFromEmail(email);
        return transactionRepository.findByUserIdOrderByDateDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<TransactionDto> getAccountTransactions(String accountId, String email) {
        String userId = getUserIdFromEmail(email);
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TransactionDto getTransactionById(String id, String email) {
        String userId = getUserIdFromEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        return convertToDto(transaction);
    }
    
    @Transactional
    public TransactionDto updateTransaction(String id, TransactionDto transactionDto, String email) {
        String userId = getUserIdFromEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        BigDecimal oldAmount = transaction.getAmount();
        Transaction.TransactionType oldType = transaction.getType();
        
        transaction.setType(transactionDto.getType());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setDate(transactionDto.getDate());
        transaction.setCategory(transactionDto.getCategory());
        transaction.setReceiptUrl(transactionDto.getReceiptUrl());
        transaction.setIsRecurring(transactionDto.getIsRecurring());
        transaction.setRecurringInterval(transactionDto.getRecurringInterval());
        transaction.setStatus(transactionDto.getStatus());
        
        transaction = transactionRepository.save(transaction);
        
        revertAccountBalance(transaction.getAccount(), oldType, oldAmount);
        updateAccountBalance(transaction.getAccount(), transaction);
        
        return convertToDto(transaction);
    }
    
    @Transactional
    public void deleteTransaction(String id, String email) {
        String userId = getUserIdFromEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        revertAccountBalance(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        transactionRepository.delete(transaction);
    }
    
    private void updateAccountBalance(Account account, Transaction transaction) {
        if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            } else {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
            accountRepository.save(account);
        }
    }
    
    private void revertAccountBalance(Account account, Transaction.TransactionType type, BigDecimal amount) {
        if (type == Transaction.TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }
        accountRepository.save(account);
    }
    
    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        dto.setCategory(transaction.getCategory());
        dto.setReceiptUrl(transaction.getReceiptUrl());
        dto.setIsRecurring(transaction.getIsRecurring());
        dto.setRecurringInterval(transaction.getRecurringInterval());
        dto.setNextRecurringDate(transaction.getNextRecurringDate());
        dto.setStatus(transaction.getStatus());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setAccountName(transaction.getAccount().getName());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }
}

