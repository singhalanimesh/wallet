package com.example.wallet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.request.DepositRequest;
import com.example.wallet.request.TransferRequest;
import com.example.wallet.request.WithdrawalRequest;
import com.example.wallet.response.AccountResponse;
import com.example.wallet.response.EntryResponse;
import com.example.wallet.response.TransferResponse;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@RequestParam String ownerId) {
        Account account = accountService.createAccount(ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccountResponse(account.getId(), account.getBalanceCents()));
    }

    @PostMapping("/accounts/{id}/deposits")
    public ResponseEntity<EntryResponse> deposit(@PathVariable long id, @RequestParam String ownerId, @Valid @RequestBody DepositRequest depositRequest) {
        LedgerEntry entry = accountService.deposit(id, ownerId, depositRequest.amountCents());
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntryResponse(entry.getId(), entry.getAccountId(), entry.getEntryType(), entry.getAmountCents(), entry.getBalanceAfterCents()));
    }

    @PostMapping("/accounts/{id}/withdrawals")
    public ResponseEntity<EntryResponse> withdraw(@PathVariable long id, @RequestParam String ownerId, @Valid @RequestBody WithdrawalRequest withdrawalRequest) {
        LedgerEntry entry = accountService.withdrawal(id, ownerId, withdrawalRequest.amountCents());
        return ResponseEntity.status(HttpStatus.CREATED).body(new EntryResponse(entry.getId(), entry.getAccountId(), entry.getEntryType(), entry.getAmountCents(), entry.getBalanceAfterCents()));
    }

    @PostMapping("/accounts/{id}/transfers")
    public ResponseEntity<TransferResponse> transfer(@PathVariable long id, @Valid @RequestBody TransferRequest transferRequest, @RequestParam String ownerId) {
        LedgerEntry entry = accountService.transfer(transferRequest.toAccountId(), id, ownerId, transferRequest.amountCents()).stream().filter(e -> e.getEntryType() == EntryType.TRANSFER_OUT).findFirst().orElseThrow(() -> new RuntimeException("Transfer entry not found"));
        return ResponseEntity.status(HttpStatus.CREATED).body(new TransferResponse(entry.getId(), entry.getAccountId(), entry.getEntryType(), entry.getAmountCents(), entry.getBalanceAfterCents(), entry.getTransferId()));
    }
}
