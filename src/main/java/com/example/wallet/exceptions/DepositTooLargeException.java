package com.example.wallet.exceptions;

public class DepositTooLargeException extends RuntimeException {

    public DepositTooLargeException() {
        super("Deposit amount exceeds the maximum allowed limit.");
    }

}
