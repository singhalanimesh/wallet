package com.example.wallet;

import jakarta.validation.constraints.Positive;

public record DepositRequest(@Positive long amountCents) {
}
