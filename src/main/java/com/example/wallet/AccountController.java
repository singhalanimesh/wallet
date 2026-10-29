package com.example.wallet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController 
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public Account createAccount(@Valid @RequestParams String ownerId) {
        return accountService.createAccount(ownerId);
    }
    
}
