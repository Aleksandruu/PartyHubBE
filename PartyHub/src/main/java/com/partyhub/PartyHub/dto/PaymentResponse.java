package com.partyhub.PartyHub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String chargeId;
    private BigDecimal amount;
    private String currency;
    private String description;
}
