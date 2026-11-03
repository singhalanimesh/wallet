package com.example.wallet;

import java.util.Optional;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public AccountService(AccountRepository accountRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public Account createAccount(String ownerId) {
        return accountRepository.save(new Account(ownerId));
    }

    @Transactional
    public LedgerEntry deposit(long id, String ownerId, long amountCents) {
        Account account = accountRepository.findLockByOwnerIdAndId(ownerId, id)
                .orElseThrow(() -> new AccountNotFoundException());

        long balanceCents = account.deposit(amountCents);
        accountRepository.save(account);
        return ledgerRepository.save(new LedgerEntry(id, EntryType.DEPOSIT, amountCents, balanceCents));
    }

    public AccountResponse getAccountResponse(Account account) {
        return new AccountResponse(account.getId(), account.getBalanceCents());
    }
}