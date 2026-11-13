package com.example.wallet.response;

import com.example.wallet.EntryType;

public record EntryResponse(Long id, long accountId, EntryType entryType, long amountCents, long balanceAfterCents) {}
