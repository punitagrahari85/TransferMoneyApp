package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class FundTransferControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-111\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-222\",\"balance\":500}")).andExpect(status().isCreated());
  }

  @Test
  void transferAmountSuccess() throws Exception {
    this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-111\",\"accountTo\":\"Id-222\",\"amount\":300}")).andExpect(status().isOk());

    Account accountFrom = accountsService.getAccount("Id-111");
    assertThat(accountFrom.getBalance()).isEqualByComparingTo("700");
    Account accountTo = accountsService.getAccount("Id-222");
    assertThat(accountTo.getBalance()).isEqualByComparingTo("800");
  }

  @Test
  void transferAmountInsufficientFunds() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-111\",\"accountTo\":\"Id-222\",\"amount\":1100}")).andExpect(status().isBadRequest()).andReturn();
     Assertions.assertEquals("Insufficient balance in the source account.", mvcResult.getResponse().getContentAsString());
  }

  @Test
  void transferAmountNegativeAmount() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-111\",\"accountTo\":\"Id-222\",\"amount\":-110}")).andExpect(status().isBadRequest()).andReturn();
    Assertions.assertEquals("{\"amount\":\"Amount must be greater than zero\"}", mvcResult.getResponse().getContentAsString());
  }

  @Test
  void transferAmountEmptyAccountFrom() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"\",\"accountTo\":\"Id-222\",\"amount\":110}")).andExpect(status().isBadRequest()).andReturn();
    Assertions.assertEquals("{\"accountFrom\":\"must not be empty\"}", mvcResult.getResponse().getContentAsString());
  }

  @Test
  void transferAmountEmptyAccountTo() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"id-111\",\"accountTo\":\"\",\"amount\":110}")).andExpect(status().isBadRequest()).andReturn();
    Assertions.assertEquals("{\"accountTo\":\"must not be empty\"}", mvcResult.getResponse().getContentAsString());
  }

 @Test
  void transferAmountInvalidAccount() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-444\",\"accountTo\":\"Id-222\",\"amount\":110}")).andExpect(status().isBadRequest()).andReturn();
   Assertions.assertEquals("One or both account(s) not found.", mvcResult.getResponse().getContentAsString());
  }

  @Test
  void transferAmountMultipleTransaction() throws Exception {
    this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-111\",\"accountTo\":\"Id-222\",\"amount\":300}")).andExpect(status().isOk());

    this.mockMvc.perform(post("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFrom\":\"Id-111\",\"accountTo\":\"Id-222\",\"amount\":300}")).andExpect(status().isOk());

    Account accountFrom = accountsService.getAccount("Id-111");
    assertThat(accountFrom.getBalance()).isEqualByComparingTo("400");
    Account accountTo = accountsService.getAccount("Id-222");
    assertThat(accountTo.getBalance()).isEqualByComparingTo("1100");
  }
}
