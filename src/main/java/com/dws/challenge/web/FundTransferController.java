package com.dws.challenge.web;

import com.dws.challenge.domain.FundTransferRequest;
import com.dws.challenge.service.AccountsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transfer")
@Slf4j
public class FundTransferController {

    private final AccountsService accountsService;

    @Autowired
    public FundTransferController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transferMoney(@RequestBody @Valid FundTransferRequest fundTransferRequest) {
        log.info("Transfer amount from Account Id {}", fundTransferRequest.getAccountFrom());
        log.info("Transfer amount to Account Id {}", fundTransferRequest.getAccountTo());
        log.info("Amount to transfer between accounts {}", fundTransferRequest.getAmount());
        boolean success = this.accountsService.transferMoney(fundTransferRequest.getAccountFrom(), fundTransferRequest.getAccountTo(), fundTransferRequest.getAmount());
        if (success) {
            return ResponseEntity.ok("Transfer Successful");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transfer Failed");
        }
    }
}
