package com.example.wallet.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient funds for the requested operation.");
    }

    
}
