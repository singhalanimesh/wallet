package com.example.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AccountTest {

    @Test
    public void checkBalanceUpdateTest() {
        Account account = new Account("Jess1");
        long updatedBalance = account.deposit(1000);
        assertEquals(updatedBalance, account.getBalanceCents());
        account.deposit(500);
        assertEquals(1500, account.getBalanceCents());
        // account.withdraw(500);
        // assert account.getBalanceCents() == 500;
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
    public void checkBalanceOverflowTest () {
        Account account = new Account("Bob2");
        account.deposit(Long.MAX_VALUE - 100);
        assertThrows(IllegalArgumentException.class, () -> account.deposit(101L));
        assertEquals(Long.MAX_VALUE - 100, account.getBalanceCents());
    }

}
