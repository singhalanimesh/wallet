package com.example.wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.exceptions.AccountNotFoundException;
import com.example.wallet.exceptions.SameAccountException;

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

    @Transactional
    public LedgerEntry withdrawal(long id, String ownerId, long amountCents) {
        Account account = accountRepository.findLockByOwnerIdAndId(ownerId, id)
                .orElseThrow(() -> new AccountNotFoundException());
        long balanceCents = account.withdrawal(amountCents);
        accountRepository.save(account);
        return ledgerRepository.save(new LedgerEntry(id, EntryType.WITHDRAWAL, -amountCents, balanceCents));
    }

    @Transactional
    public List<LedgerEntry> transfer(long toAccountId, long fromAccountId, String fromOwnerId, long amountCents) {
        if (toAccountId == fromAccountId)
            throw new SameAccountException();
        Account toAccount;
        Account fromAccount;
        if (!accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException()).getOwnerId().equals(fromOwnerId)) {
            throw new AccountNotFoundException();
        }
        if (toAccountId < fromAccountId) {
            toAccount = accountRepository.findLockById(toAccountId)
                    .orElseThrow(() -> new AccountNotFoundException());
            fromAccount = accountRepository.findLockById(fromAccountId)
                    .orElseThrow(() -> new AccountNotFoundException());
        } else {
            fromAccount = accountRepository.findLockById(fromAccountId)
                    .orElseThrow(() -> new AccountNotFoundException());
            toAccount = accountRepository.findLockById(toAccountId)
                    .orElseThrow(() -> new AccountNotFoundException());
        }

        UUID transferId = UUID.randomUUID();

        long fromBalanceCents = fromAccount.withdrawal(amountCents);
        long toBalanceCents = toAccount.deposit(amountCents);

        List<LedgerEntry> entries = new ArrayList<>();
        entries.add(ledgerRepository
                .save(new LedgerEntry(fromAccountId, EntryType.TRANSFER_OUT, -amountCents, transferId,
                        fromBalanceCents)));
        entries.add(ledgerRepository
                .save(new LedgerEntry(toAccountId, EntryType.TRANSFER_IN, amountCents, transferId, toBalanceCents)));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        return entries;
    }
}