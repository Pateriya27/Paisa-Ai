package com.paisa.service;

import com.paisa.dto.AdminAccountDto;
import com.paisa.dto.AdminTransactionDto;
import com.paisa.dto.AdminUserDto;
import com.paisa.entity.Account;
import com.paisa.entity.Transaction;
import com.paisa.entity.User;
import com.paisa.repository.AccountRepository;
import com.paisa.repository.TransactionRepository;
import com.paisa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertUser)
                .collect(Collectors.toList());
    }

    public List<AdminAccountDto> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::convertAccount)
                .collect(Collectors.toList());
    }

    public List<AdminTransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::convertTransaction)
                .collect(Collectors.toList());
    }

    private AdminUserDto convertUser(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private AdminAccountDto convertAccount(Account account) {
        return new AdminAccountDto(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getIsDefault(),
                account.getUser() != null ? account.getUser().getId() : null,
                account.getUser() != null ? account.getUser().getEmail() : null,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private AdminTransactionDto convertTransaction(Transaction transaction) {
        return new AdminTransactionDto(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getCategory(),
                transaction.getIsRecurring(),
                transaction.getRecurringInterval(),
                transaction.getStatus(),
                transaction.getAccount() != null ? transaction.getAccount().getId() : null,
                transaction.getAccount() != null ? transaction.getAccount().getName() : null,
                transaction.getUser() != null ? transaction.getUser().getId() : null,
                transaction.getUser() != null ? transaction.getUser().getEmail() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}

