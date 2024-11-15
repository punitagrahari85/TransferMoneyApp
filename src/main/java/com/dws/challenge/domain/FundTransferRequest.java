package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundTransferRequest {

    @NotNull
    @NotEmpty
    private final String accountFrom;

    @NotNull
    @NotEmpty
    private final String accountTo;

    @NotNull
    @Min(value = 1, message = "Amount must be greater than zero")
    private final BigDecimal amount;

    @JsonCreator
    public FundTransferRequest(@JsonProperty("accountFrom") String accountFrom,
                               @JsonProperty("accountTo") String accountTo,
                               @JsonProperty("amount") BigDecimal amount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }
}
