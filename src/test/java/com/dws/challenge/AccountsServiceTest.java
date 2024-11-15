package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    void transferMoneySuccess() {
        Account accountFrom = new Account("Id-001");
        accountFrom.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountFrom);

        Account accountTo = new Account("Id-011");
        accountTo.setBalance(new BigDecimal(300));
        this.accountsService.createAccount(accountTo);

        this.accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(300));

        Assertions.assertEquals(0, this.accountsService.getAccount("Id-001").getBalance().compareTo(BigDecimal.valueOf(700)));
        Assertions.assertEquals(0, this.accountsService.getAccount("Id-011").getBalance().compareTo(BigDecimal.valueOf(600)));
    }

    @Test
    void transferMoneyNegativeAmount() {
        Account accountFrom = new Account("Id-111");
        accountFrom.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountFrom);

        Account accountTo = new Account("Id-222");
        accountTo.setBalance(new BigDecimal(300));
        this.accountsService.createAccount(accountTo);

        try {
            this.accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(-300));
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Amount must be greater than zero.");
        }

    }

    @Test
    void transferMoneyInsufficientFunds() {
        Account accountFrom = new Account("Id-333");
        accountFrom.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountFrom);

        Account accountTo = new Account("Id-444");
        accountTo.setBalance(new BigDecimal(300));
        this.accountsService.createAccount(accountTo);
        try {
            this.accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(1200));
        } catch (InsufficientBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient balance in the source account.");
        }
    }

    @Test
    void transferMoneyInvalidAccounts() {
        Account accountFrom = new Account("Id-555");
        accountFrom.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountFrom);

        Account accountTo = new Account("Id-666");
        accountTo.setBalance(new BigDecimal(300));
        this.accountsService.createAccount(accountTo);
        try {
            this.accountsService.transferMoney("Id-2121", accountTo.getAccountId(), BigDecimal.valueOf(300));
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("One or both account(s) not found.");
        }
    }
}
