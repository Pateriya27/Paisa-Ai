package com.paisa.service;

import com.paisa.dto.AccountDto;
import com.paisa.entity.Account;
import com.paisa.entity.User;
import com.paisa.repository.AccountRepository;
import com.paisa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    private String getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    
    @Transactional
    public AccountDto createAccount(AccountDto accountDto, String email) {
        String userId = getUserIdFromEmail(email);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = new Account();
        account.setName(accountDto.getName());
        account.setType(accountDto.getType());
        account.setBalance(accountDto.getBalance() != null ? accountDto.getBalance() : java.math.BigDecimal.ZERO);
        account.setIsDefault(accountDto.getIsDefault() != null ? accountDto.getIsDefault() : false);
        account.setUser(user);
        
        if (account.getIsDefault()) {
            accountRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        accountRepository.save(existingDefault);
                    });
        }
        
        account = accountRepository.save(account);
        return convertToDto(account);
    }
    
    public List<AccountDto> getUserAccounts(String email) {
        String userId = getUserIdFromEmail(email);
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public AccountDto getAccountById(String id, String email) {
        String userId = getUserIdFromEmail(email);
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return convertToDto(account);
    }
    
    @Transactional
    public AccountDto updateAccount(String id, AccountDto accountDto, String email) {
        String userId = getUserIdFromEmail(email);
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setName(accountDto.getName());
        account.setType(accountDto.getType());
        
        if (accountDto.getIsDefault() != null && accountDto.getIsDefault() && !account.getIsDefault()) {
            accountRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        accountRepository.save(existingDefault);
                    });
            account.setIsDefault(true);
        }
        
        account = accountRepository.save(account);
        return convertToDto(account);
    }
    
    @Transactional
    public void deleteAccount(String id, String email) {
        String userId = getUserIdFromEmail(email);
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        accountRepository.delete(account);
    }
    
    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setType(account.getType());
        dto.setBalance(account.getBalance());
        dto.setIsDefault(account.getIsDefault());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}

