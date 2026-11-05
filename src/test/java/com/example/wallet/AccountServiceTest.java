package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.wallet.exceptions.AccountNotFoundException;
import com.example.wallet.exceptions.SameAccountException;

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

    @Test
    public void transferToSameAccountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        assertThrows(SameAccountException.class, () -> accountService.transfer(1L, 1L, "owner", 1000L));
    }

    @Test
    public void validTransferTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account fromAccount = new Account("owner");
        fromAccount.deposit(2000L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findLockById(1L)).thenReturn(Optional.of(fromAccount));

        Account toAccount = new Account("owner2");
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findLockById(2L)).thenReturn(Optional.of(toAccount));

        accountService.transfer(2L, 1L, "owner", 1100L);

        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
        assertEquals(900L, fromAccount.getBalanceCents());
        assertEquals(1100L, toAccount.getBalanceCents());

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository, times(2)).save(captor.capture());
        List<LedgerEntry> saved = captor.getAllValues();

        LedgerEntry out = saved.stream()
                .filter(e -> e.getEntryType() == EntryType.TRANSFER_OUT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no TRANSFER_OUT entry was saved"));
        LedgerEntry in = saved.stream()
                .filter(e -> e.getEntryType() == EntryType.TRANSFER_IN)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no TRANSFER_IN entry was saved"));

        assertEquals(2, saved.size());

        assertEquals(1L, out.getAccountId());
        assertEquals(-1100L, out.getAmountCents());
        assertEquals(900L, out.getBalanceAfterCents());

        assertEquals(2L, in.getAccountId());
        assertEquals(1100L, in.getAmountCents());
        assertEquals(1100L, in.getBalanceAfterCents());

        assertNotNull(out.getTransferId());
        assertEquals(out.getTransferId(), in.getTransferId());
    }

    @Test
    public void transferFromAccountNotFoundThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        when(accountRepository.findById(2L)).thenReturn(Optional.of(new Account("owner")));

        assertThrows(AccountNotFoundException.class, () -> accountService.transfer(2L, 1L, "owner", 1000L));
    }

    @Test
    public void transferToAccountNotFoundThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(new Account("owner")));
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.transfer(2L, 1L, "owner", 1000L));
    }
}