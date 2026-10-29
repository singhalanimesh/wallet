alter table ledger_entries drop constraint entry_type_valid;

alter table ledger_entries add constraint entry_type_valid
    check (entry_type in ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'FEE', 'REVERSAL'));