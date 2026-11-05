package com.example.wallet;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long accountId;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    private long amountCents;

    private long balanceAfterCents;

    private UUID transferId;

    private Instant createdAt = Instant.now();

    protected LedgerEntry() {
    }

    public LedgerEntry(long accountId, EntryType entryType, long amountCents, long balanceAfterCents) // for non-transfer entries
    {
        validateParams(entryType, amountCents, null, balanceAfterCents, false);
        this.accountId = accountId;
        this.entryType = entryType;
        this.amountCents = amountCents;
        this.balanceAfterCents = balanceAfterCents;
    }

    public LedgerEntry(long accountId, EntryType entryType, long amountCents, UUID transferId, long balanceAfterCents) // for transfer entries
    {
        validateParams(entryType, amountCents, transferId, balanceAfterCents, true);
        this.accountId = accountId;
        this.entryType = entryType;
        this.amountCents = amountCents;
        this.balanceAfterCents = balanceAfterCents;
        this.transferId = transferId;
    }

    private void validateParams(EntryType entryType, long amountCents, UUID transferId, long balanceAfterCents, boolean transfer) 
    {
        if (entryType == null) // entry type cannot be null
            throw new IllegalArgumentException("Entry type cannot be null");

        if (entryType == EntryType.TRANSFER_IN || entryType == EntryType.TRANSFER_OUT) 
            {
                if (!transfer || transferId == null) // transfer entries must have a transfer ID (!a || (a && b))
                throw new IllegalArgumentException("Transfer entries must have a transfer ID");}
        else
            {
                if (transfer) // non-transfer entries cannot have a transfer ID
                throw new IllegalArgumentException("Transfer ID can only be set for transfer entries");}

        if (balanceAfterCents < 0) // balance after entry cannot be negative
            throw new IllegalArgumentException("Balance after entry cannot be negative");

        if (amountCents == 0) // entries must have a non-zero amount
            throw new IllegalArgumentException("Entries must have a non-zero amount");
        else if (amountCents < 0 && (entryType == EntryType.DEPOSIT || entryType == EntryType.TRANSFER_IN)) // deposit or transfer in entries must have a positive amount
            throw new IllegalArgumentException(entryType + " entries must have a positive amount");
        else if (amountCents > 0 && (entryType == EntryType.WITHDRAWAL || entryType == EntryType.FEE || entryType == EntryType.TRANSFER_OUT)) // Transfer out, withdrawal or fee entries must have a negative amount
            throw new IllegalArgumentException(entryType + " entries must have a negative amount");
    }

    public Long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public long getBalanceAfterCents() {
        return balanceAfterCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
