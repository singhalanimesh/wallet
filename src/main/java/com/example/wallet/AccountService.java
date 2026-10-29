package com.example.wallet;

import org.springframework.stereotype.Service;


@Service
public class AccountService{
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String ownerId) {
        return accountRepository.save(new Account(ownerId));
    }
}