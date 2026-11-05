package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.wallet.exceptions.InsufficientFundsException;

public class AccountTest {

    @Test
    public void checkBalanceUpdateTest() {
        Account account = new Account("Jess1");
        long updatedBalance = account.deposit(1000);
        assertEquals(updatedBalance, account.getBalanceCents());
        account.deposit(500);
        assertEquals(1500, account.getBalanceCents());
        account.withdrawal(400);
        assertEquals(1100, account.getBalanceCents());
        account.withdrawal(1100);
        assertEquals(0, account.getBalanceCents());
    }

    @Test
    public void depositZeroAmountTest() {
        Account account = new Account("Bob1");
        assertThrows(IllegalArgumentException.class, () -> account.deposit(0));
    }

    @Test
    public void depositNegativeAmountTest() {
        Account account = new Account("Jess2");
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-100));
    }

    @Test
    public void checkBalanceOverflowTest() {
        Account account = new Account("Bob2");
        account.deposit(Long.MAX_VALUE - 100);
        assertThrows(IllegalArgumentException.class, () -> account.deposit(101L));
        assertEquals(Long.MAX_VALUE - 100, account.getBalanceCents());
    }

    @Test
    public void withdrawalAmountMoreThanBalanceTest() {
        Account account = new Account("Jess3");
        assertThrows(InsufficientFundsException.class, () -> account.withdrawal(1));
        account.deposit(1000);
        assertThrows(InsufficientFundsException.class, () -> account.withdrawal(1500));
        assertEquals(1000, account.getBalanceCents());
    }

    @Test
    public void withdrawalNegativeAmountTest() {
        Account account = new Account("Bob3");
        assertThrows(IllegalArgumentException.class, () -> account.withdrawal(-100));
    }

    @Test 
    public void withdrawalZeroAmountTest() {
        Account account = new Account("Jess4");
        assertThrows(IllegalArgumentException.class, () -> account.withdrawal(0));
    }

}
