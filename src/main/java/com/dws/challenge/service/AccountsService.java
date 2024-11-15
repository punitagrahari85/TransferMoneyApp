package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

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

  private final Map<String, Lock> lockAccountsMap = new HashMap<>();

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
  public boolean transferMoney(String fromAccountId, String toAccountId, BigDecimal amount) {
    Account fromAccount = accountsRepository.getAccount(fromAccountId);
    Account toAccount = accountsRepository.getAccount(toAccountId);

    if (fromAccount == null || toAccount == null) {
      throw new IllegalArgumentException("One or both account(s) not found.");
    }

    //Logic to lock both accounts in a consistent order to avoid deadlocks
    Account firstLockAccount = fromAccountId.compareTo(toAccountId) < 0 ? fromAccount : toAccount;
    Account secondLockAccount = fromAccountId.compareTo(toAccountId) < 0 ? toAccount : fromAccount;

    synchronized (firstLockAccount) {
      synchronized (secondLockAccount) {
        //Step to perform the amount transfer
        if (fromAccount.withdraw(amount)) {
          toAccount.deposit(amount);
        } else {
          // Negative balance scenario after the withdrawal
          throw new InsufficientBalanceException("Insufficient balance in the source account.");
        }
      }
    }

    //Step to send the notification to both the account holders.
    notificationService.notifyAboutTransfer(fromAccount, "Amount '" + amount + "' has been successfully transferred to the account : " + toAccountId);
    notificationService.notifyAboutTransfer(toAccount, "Amount '" + amount + "' received from the account : " + fromAccountId);

    return true;
  }

}
