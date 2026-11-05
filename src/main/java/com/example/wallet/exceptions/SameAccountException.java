package com.example.wallet.exceptions;

public  class SameAccountException extends RuntimeException {
    public SameAccountException() {
        super("Cannot transfer to the same account");
    }
}
