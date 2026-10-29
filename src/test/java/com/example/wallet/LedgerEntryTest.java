package com.example.wallet;

import org.junit.jupiter.api.Test;

public class LedgerEntryTest {

    @Test
    void testLedgerEntryCreation() {
        // Test creating a LedgerEntry with valid parameters
        LedgerEntry entry = new LedgerEntry(1622738L, EntryType.DEPOSIT, 1000000L, 1200000L);
        assert entry.getAccountId() == 1622738L;
        assert entry.getEntryType() == EntryType.DEPOSIT;
        assert entry.getAmountCents() == 1000000L;
        assert entry.getBalanceAfterCents() == 1200000L;
    }

    @Test
    void testLedgerEntryCreationWithTransferIn() {
        // Test creating a LedgerEntry with valid transfer parameters
        LedgerEntry entry = new LedgerEntry(1622738L, EntryType.TRANSFER_IN, 500000L, java.util.UUID.randomUUID(),
                1500000L);
        assert entry.getAccountId() == 1622738L;
        assert entry.getEntryType() == EntryType.TRANSFER_IN;
        assert entry.getAmountCents() == 500000L;
        assert entry.getBalanceAfterCents() == 1500000L;
        assert entry.getTransferId() != null;
    }

    @Test
    void testLedgerEntrtCreationwithTransferOut() {
        // Test creating a LedgerEntry with valid transfer parameters
        LedgerEntry entry = new LedgerEntry(1622738L, EntryType.TRANSFER_OUT, -500000L, java.util.UUID.randomUUID(),
                500000L);
        assert entry.getAccountId() == 1622738L;
        assert entry.getEntryType() == EntryType.TRANSFER_OUT;
        assert entry.getAmountCents() == -500000L;
        assert entry.getBalanceAfterCents() == 500000L;
        assert entry.getTransferId() != null;
    }

    @Test
    void testLedgerEntryCreationWithEntryTypeNull() {
        // Test creating a LedgerEntry with invalid parameters
        try {
            new LedgerEntry(1622738L, null, 1000000L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Entry type cannot be null");
        }
    }

    @Test
    void testLedgerEntryCreationWithNegativeDepositAmount() {
        try {
            new LedgerEntry(1622738L, EntryType.DEPOSIT, -1000000L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("DEPOSIT entries must have a positive amount");
        }
    }

    @Test
    void testLedgerEntryCreationWithPositiveWithdrawalAmount() {
        try {
            new LedgerEntry(1622738L, EntryType.WITHDRAWAL, 1000000L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("WITHDRAWAL entries must have a negative amount");
        }
    }

    @Test
    void testLedgerEntryCreationWithPositiveFeeAmount() {
        try {
            new LedgerEntry(1622738L, EntryType.FEE, 1000000L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("FEE entries must have a negative amount");
        }
    }

    @Test
    void testLedgerEntryCreationWithZeroAmount() {
        try {
            new LedgerEntry(1622738L, EntryType.DEPOSIT, 0L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Entries must have a non-zero amount");
        }
    }

    @Test
    void testledgerEntryCreationWithTransferIDForNonTransferEntry() {
        try {
            new LedgerEntry(1622738L, EntryType.DEPOSIT, 1000000L, java.util.UUID.randomUUID(), 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Transfer ID can only be set for transfer entries");
        }
    }

    @Test
    void testledgerCreationwithoutTransferIDForTransferEntry() {
        try {
            new LedgerEntry(1622738L, EntryType.TRANSFER_OUT, -1000000L, 1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Transfer entries must have a transfer ID");
        }
    }

    @Test
    void testLedgerEntryCreationWithNegativeBalance() {

        try {
            new LedgerEntry(1622738L, EntryType.WITHDRAWAL, -1000000L, -1200000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Balance after entry cannot be negative");
        }
    }

    @Test
    void testLedgerEntryCreationWithNullTransferID() {

        try {
            new LedgerEntry(1622738L, EntryType.TRANSFER_IN, 500000L, null, 1500000L);
            assert false; // Should not reach here
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Transfer entries must have a transfer ID");
        }
    }
}
