package com.example.wallet.response;

import java.util.UUID;

import com.example.wallet.EntryType;

public record TransferResponse(Long id, long accountId, EntryType entryType, long amountCents, long balanceAfterCents, UUID transferId) { }
