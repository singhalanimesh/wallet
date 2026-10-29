package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class AccountServiceTest {

    @Test
    public void accountCreationTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);
        accountService.createAccount("some-owner");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        assertEquals("some-owner", captor.getValue().getOwnerId());
        assertEquals(0L, captor.getValue().getBalanceCents());
    }

    @Test
    public void accountCreationWithBlankownerIdTest() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(""));
        
    }
}