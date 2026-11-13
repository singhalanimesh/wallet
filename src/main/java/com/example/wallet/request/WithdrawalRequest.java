package com.example.wallet.request;

import jakarta.validation.constraints.Positive;

public record WithdrawalRequest(@Positive long amountCents) {
}