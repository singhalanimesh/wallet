package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.example.wallet.exceptions.AccountNotFoundException;
import com.example.wallet.exceptions.InsufficientFundsException;
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
    public void withdrawalAccountNotFoundTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);
        assertThrows(AccountNotFoundException.class, () -> accountService.withdrawal(1L, "owner1", 1000L));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void withdrawalTooMuchFromAccountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account account = new Account("owner");
        account.deposit(500L);
        when(accountRepository.findLockByOwnerIdAndId("owner", 1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> accountService.withdrawal(1L, "owner", 900L));
        verify(accountRepository, never()).save(any(Account.class));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        assertEquals(500L, account.getBalanceCents());
    }

    @Test
    public void withdrawalUpdatesBalanceAndWritesLedgerEntry() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account account = new Account("owner");
        account.deposit(5000L);
        when(accountRepository.findLockByOwnerIdAndId("owner", 1L)).thenReturn(Optional.of(account));

        accountService.withdrawal(1L, "owner", 1000L);
        assertEquals(4000L, account.getBalanceCents());
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository).save(captor.capture());
        verify(accountRepository).save(account);
        assertEquals(1L, captor.getValue().getAccountId());
        assertEquals(EntryType.WITHDRAWAL, captor.getValue().getEntryType());
        assertEquals(-1000L, captor.getValue().getAmountCents());
        assertEquals(4000L, captor.getValue().getBalanceAfterCents());
    }

    @Test
    public void withdrawalZeroOrNegativeAmountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawal(1L, "owner", 0L));
        assertThrows(IllegalArgumentException.class, () -> accountService.withdrawal(1L, "owner", -100L));

        verify(accountRepository, never()).findLockByOwnerIdAndId(any(), any());
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

@Test
public void withdrawalWithExactZeroLeftTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account account = new Account("owner");
        account.deposit(1000L);
        account.deposit(500L);
        when(accountRepository.findLockByOwnerIdAndId("owner", 1L)).thenReturn(Optional.of(account));

        accountService.withdrawal(1L, "owner", 1500L);
        assertEquals(0L, account.getBalanceCents());
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getAccountId());
        assertEquals(EntryType.WITHDRAWAL, captor.getValue().getEntryType());
        assertEquals(-1500L, captor.getValue().getAmountCents());
        assertEquals(0L, captor.getValue().getBalanceAfterCents());

        verify(accountRepository).save(account);
}

    @Test
    public void transferToSameAccountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        assertThrows(SameAccountException.class, () -> accountService.transfer(1L, 1L, "owner", 1000L));
        verify(accountRepository, never()).save(any(Account.class));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));

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

        assertThrows(AccountNotFoundException.class, () -> accountService.transfer(2L, 1L, "owner", 1000L));
        verify(accountRepository, never()).findLockById(any());
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void transferToAccountNotFoundThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(new Account("owner")));
        when(accountRepository.findLockById(1L)).thenReturn(Optional.of(new Account("owner")));
        when(accountRepository.findLockById(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.transfer(2L, 1L, "owner", 1000L));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void transferTooMuchFromAccountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account fromAccount = new Account("owner");
        fromAccount.deposit(500L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findLockById(1L)).thenReturn(Optional.of(fromAccount));

        Account toAccount = new Account("owner2");
        toAccount.deposit(1000L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findLockById(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientFundsException.class, () -> accountService.transfer(2L, 1L, "owner", 1000L));
        verify(accountRepository, never()).save(any(Account.class));
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        assertEquals(500L, fromAccount.getBalanceCents());
        assertEquals(1000L, toAccount.getBalanceCents());
    }

    @Test
    public void transferFromAccountNotOwnedByOwnerIdThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account fromAccount = new Account("owner");
        fromAccount.deposit(500L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));

        assertThrows(AccountNotFoundException.class, () -> accountService.transfer(2L, 1L, "not-owner", 1000L));
        verify(accountRepository, never()).findLockById(any());
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void transferZeroOrNegativeAmountThrowsException() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        assertThrows(IllegalArgumentException.class, () -> accountService.transfer(2L, 1L, "owner", 0L));
        assertThrows(IllegalArgumentException.class, () -> accountService.transfer(2L, 1L, "owner", -100L));

        verify(accountRepository, never()).findLockById(any());
        verify(ledgerRepository, never()).save(any(LedgerEntry.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void transferCorrectLockOrderTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account fromAccount = new Account("owner1");
        fromAccount.deposit(2000L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findLockById(1L)).thenReturn(Optional.of(fromAccount));

        Account toAccount = new Account("owner2");
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findLockById(2L)).thenReturn(Optional.of(toAccount));

        accountService.transfer(2L, 1L, "owner1", 1100L);

        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findLockById(1L); // this call must come first
        inOrder.verify(accountRepository).findLockById(2L); // and this one after it

    }

    @Test
    public void transferCorrectLockOrder2Test() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        AccountService accountService = new AccountService(accountRepository, ledgerRepository);

        Account fromAccount = new Account("owner2");
        fromAccount.deposit(2100L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findLockById(2L)).thenReturn(Optional.of(fromAccount));

        Account toAccount = new Account("owner1");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findLockById(1L)).thenReturn(Optional.of(toAccount));

        accountService.transfer(1L, 2L, "owner2", 900L);

        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findLockById(1L); // this call must come first
        inOrder.verify(accountRepository).findLockById(2L); // and this one after it
    }

}