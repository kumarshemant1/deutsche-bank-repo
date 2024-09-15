package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransferDTO;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  
  @InjectMocks
  AccountsService accountsServiceMock;
  
  @Mock
  AccountsRepositoryInMemory accountsRepositoryMock;
  
  @Mock
  EmailNotificationService emailNotificationServiceMock;

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
  public void transferMoneySuccessTest() throws InsufficientFundException {
	  String accountFrom = "993344";
	  String accountTo = "110022";
	  BigDecimal amount = BigDecimal.valueOf(40022.0);
	  FundTransferDTO dto = new FundTransferDTO();
	  dto.setAccountFrom(accountFrom);
	  dto.setAccountTo(accountTo);
	  dto.setAmount(amount);
	  
	  Account account1 = Account.builder().accountId(accountFrom).balance(BigDecimal.valueOf(0.0)).build();
	  Account account2 = Account.builder().accountId(accountTo).balance(BigDecimal.valueOf(40022.0)).build();
	  
	  String finalResponse = "Funds transferred successfully from : " + accountFrom + " to : " + accountTo + " of value : " + amount;
	 
	  Mockito.when(accountsRepositoryMock.saveAccount(account1)).thenReturn(account1);
	  Mockito.when(accountsRepositoryMock.saveAccount(account2)).thenReturn(account2);
	  
	  Mockito.doNothing().when(emailNotificationServiceMock).notifyAboutTransfer(account2, "Alert! Money Received, rupees : " + amount + " from " + accountFrom);
	  
	  String result = accountsServiceMock.transferMoney(accountFrom, accountTo, amount);
	  
	  assertNotNull(result);
  }
}
