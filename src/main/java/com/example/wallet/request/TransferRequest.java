package com.example.wallet.request;

import jakarta.validation.constraints.Positive;

public record TransferRequest(@Positive long amountCents, @Positive long toAccountId) {}
