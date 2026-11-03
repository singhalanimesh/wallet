package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class AccountServiceTest {

    @Test
    public void accountCreationTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);
        accountService.createAccount("some-owner");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        assertEquals("some-owner", captor.getValue().getOwnerId());
        assertEquals(0L, captor.getValue().getBalanceCents());
    }

    @Test
    public void accountCreationWithBlankownerIdTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(""));
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    public void depositAccountNotFoundTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);
        assertThrows(AccountNotFoundException.class, () -> accountService.deposit(1L, "owner1", 1000L));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void deositZeroAmoutDoesntUpdateBalanceAndSaveLedgerEntry() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account account = new Account("owner");
        when(accountRepository.findLockByOwnerIdAndId("owner", 1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class, () -> accountService.deposit(1L, "owner", 0L));
        assertEquals(0L, account.getBalanceCents());
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void depositUpdatesBalanceAndWritesLedgerEntry() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account account = new Account("owner");
        account.deposit(500);
        when(accountRepository.findLockByOwnerIdAndId("owner", 1L)).thenReturn(Optional.of(account));

        accountService.deposit(1L, "owner", 1000L);
        assertEquals(1500L, account.getBalanceCents());
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository).save(captor.capture());
        verify(accountRepository).save(account);
        assertEquals(1L, captor.getValue().getAccountId());
        assertEquals(EntryType.DEPOSIT, captor.getValue().getEntryType());
        assertEquals(1000L, captor.getValue().getAmountCents());
        assertEquals(1500L, captor.getValue().getBalanceAfterCents());
    }
}