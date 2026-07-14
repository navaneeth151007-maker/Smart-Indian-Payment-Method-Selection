package com.paymentrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TransferRequest(
        @JsonProperty("sender_name") String senderName,
        @JsonProperty("acc_no") String accNo,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("receiver") ReceiverDetails receiver
) {
}
