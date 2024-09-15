package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.repository.AccountsRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public Account saveAccount(Account account) {
	    return this.accountsRepository.saveAccount(account);
	  }
  
  public String transferMoney(@NotBlank String accountFrom, @NotBlank String accountTo, @NotNull BigDecimal amount) throws InsufficientFundException {
	  String finalResponse = null;
	  Account senderAccount = getAccount(accountFrom);
	  Account receiverAccount = getAccount(accountTo);
	  if(senderAccount != null && receiverAccount != null) {
		  int result = amount.compareTo(senderAccount.getBalance());
		  if(result <= 0) {
			  receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
			  senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
			  saveAccount(receiverAccount);
			  saveAccount(senderAccount);
			  emailNotificationService.notifyAboutTransfer(receiverAccount, "Alert! Money Received, rupees : " + amount + " from " + accountFrom);
			  finalResponse = "Funds transferred successfully from : " + accountFrom + " to : " + accountTo + " of value : " + amount;
		  } else {
			  throw new InsufficientFundException("Transfer amount is greater than Balance available");
		  }
	  } else { 
		  finalResponse = "Invalid account, so tranfer failed";
	  }
	  return finalResponse;
  }
}
