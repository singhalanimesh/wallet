package com.example.wallet;

public record EntryResponse(Long id, long accountId, EntryType entryType, long amountCents, long balanceAfterCents) {}
