package com.paisa.controller;

import com.paisa.dto.AccountDto;
import com.paisa.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountController {
    
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto, 
                                                    Authentication authentication) {
        String userId = authentication.getName();
        AccountDto created = accountService.createAccount(accountDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<AccountDto>> getUserAccounts(Authentication authentication) {
        String userId = authentication.getName();
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id, 
                                                 Authentication authentication) {
        String userId = authentication.getName();
        try {
            AccountDto account = accountService.getAccountById(id, userId);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable String id,
                                                    @RequestBody AccountDto accountDto,
                                                    Authentication authentication) {
        String userId = authentication.getName();
        try {
            AccountDto updated = accountService.updateAccount(id, accountDto, userId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id,
                                              Authentication authentication) {
        String userId = authentication.getName();
        try {
            accountService.deleteAccount(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

