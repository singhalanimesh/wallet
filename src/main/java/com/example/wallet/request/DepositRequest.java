package com.example.wallet.request;

import jakarta.validation.constraints.Positive;

public record DepositRequest(@Positive long amountCents) {
}
