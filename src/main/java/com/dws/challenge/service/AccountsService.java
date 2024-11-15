package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  @Autowired
  NotificationService notificationService;

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  /**
   * Method to perform amount transfer
   * @param fromAccountId
   * @param toAccountId
   * @param amount
   * @return boolean result
   */
  public synchronized boolean transferMoney(String fromAccountId, String toAccountId, BigDecimal amount) {
    Account fromAccount = accountsRepository.getAccount(fromAccountId);
    Account toAccount = accountsRepository.getAccount(toAccountId);

    if (fromAccount == null || toAccount == null) {
      throw new IllegalArgumentException("One or both account(s) not found.");
    }

    if (fromAccount.getBalance().compareTo(amount) < 0) {
      throw new InsufficientBalanceException("Insufficient balance in the source account.");
    }

    //Step to perform the amount transfer
    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
    toAccount.setBalance(toAccount.getBalance().add(amount));

    //Step to send the notification to both the account holders.
    notificationService.notifyAboutTransfer(fromAccount, "Amount '" + amount + "' has been successfully transferred to the account : " + toAccountId);
    notificationService.notifyAboutTransfer(toAccount, "Amount '" + amount + "' received from the account : " + fromAccountId);

    return true;
  }
}
